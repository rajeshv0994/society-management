package com.societyapp.application.command;

import com.societyapp.application.model.activity.ActivityModels;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.query.ActivityQueryCoordinator;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.math.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class ActivityLifecycleCoordinator extends ActivityWorkflowSupport {
    public ActivityLifecycleCoordinator(
            SocietyActivityRepository activities,
            ActivityParticipationRepository participations,
            ActivityGalleryPhotoRepository photos,
            ActivityEventRepository events,
            UserAccountJpaRepository users,
            StaffPermissionRepository permissions,
            SocietyPaymentSettingRepository paymentSettings,
            NotificationOutboxRepository notifications,
            com.societyapp.application.port.out.DocumentStorage storage,
            ActivityQueryCoordinator queries) {
        super(
                activities,
                participations,
                photos,
                events,
                users,
                permissions,
                paymentSettings,
                notifications,
                storage,
                queries);
    }

    public ActivityModels.ActivityView create(
            UUID societyId,
            UUID actorId,
            String role,
            String title,
            String category,
            String description,
            String venue,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            OffsetDateTime registrationOpensAt,
            OffsetDateTime registrationClosesAt,
            Integer capacity,
            BigDecimal feePerPerson,
            boolean approvalRequired,
            int cancellationHours,
            String audience,
            String organizerName,
            String organizerMobile,
            String instructions,
            boolean publishNow) {
        requireAdmin(role);
        validateSchedule(startsAt, endsAt, registrationOpensAt, registrationClosesAt, capacity);
        var item = new SocietyActivity();
        item.setSocietyId(societyId);
        item.setActivityCode(activityCode());
        item.setCreatedBy(actorId);
        item.setCreatedAt(OffsetDateTime.now());
        apply(
                item,
                title,
                category,
                description,
                venue,
                startsAt,
                endsAt,
                registrationOpensAt,
                registrationClosesAt,
                capacity,
                feePerPerson,
                approvalRequired,
                cancellationHours,
                audience,
                organizerName,
                organizerMobile,
                instructions);
        item.setStatus(publishNow ? ActivityStatus.PUBLISHED.code() : ActivityStatus.DRAFT.code());
        if (publishNow) item.setPublishedAt(OffsetDateTime.now());
        item = activities.save(item);
        addEvent(item, null, actorId, role, publishNow ? "CREATED_AND_PUBLISHED" : "CREATED", null);
        if (publishNow) notifyResidents(societyId, item, "ACTIVITY_PUBLISHED");
        return queries.activityView(item, actorId);
    }

    public ActivityModels.ActivityView update(
            UUID societyId,
            UUID actorId,
            String role,
            UUID activityId,
            String title,
            String category,
            String description,
            String venue,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            OffsetDateTime registrationOpensAt,
            OffsetDateTime registrationClosesAt,
            Integer capacity,
            BigDecimal feePerPerson,
            boolean approvalRequired,
            int cancellationHours,
            String audience,
            String organizerName,
            String organizerMobile,
            String instructions) {
        requireAdmin(role);
        var item = requireActivity(societyId, activityId);
        if (!Set.of(ActivityStatus.DRAFT.code(), ActivityStatus.PUBLISHED.code())
                .contains(item.getStatus())) {
            throw new IllegalStateException("Only draft or published activities can be edited");
        }
        validateSchedule(startsAt, endsAt, registrationOpensAt, registrationClosesAt, capacity);
        int registered = registeredCount(activityId);
        if (capacity != null && capacity < registered) {
            throw new IllegalArgumentException(
                    "Capacity cannot be below " + registered + " registered participants");
        }
        if (participations.findByActivityId(activityId).stream()
                        .anyMatch(row -> CAPACITY_STATUSES.contains(row.getStatus()))
                && item.getFeePerPerson().compareTo(feePerPerson) != 0) {
            throw new IllegalStateException("Fee cannot change after registrations exist");
        }
        apply(
                item,
                title,
                category,
                description,
                venue,
                startsAt,
                endsAt,
                registrationOpensAt,
                registrationClosesAt,
                capacity,
                feePerPerson,
                approvalRequired,
                cancellationHours,
                audience,
                organizerName,
                organizerMobile,
                instructions);
        item = activities.save(item);
        addEvent(item, null, actorId, role, "UPDATED", null);
        return queries.activityView(item, actorId);
    }

    public ActivityModels.ActivityView activityAction(
            UUID societyId,
            UUID actorId,
            String role,
            UUID activityId,
            String action,
            String reason) {
        requireAdmin(role);
        var item = requireActivity(societyId, activityId);
        var requestedAction = ActivityAction.from(action);
        switch (requestedAction) {
            case PUBLISH -> {
                requireActivityStatus(item, ActivityStatus.DRAFT);
                if (!item.getStartsAt().isAfter(OffsetDateTime.now())) {
                    throw new IllegalStateException("A past activity cannot be published");
                }
                item.setStatus(ActivityStatus.PUBLISHED.code());
                item.setPublishedAt(OffsetDateTime.now());
                notifyResidents(societyId, item, "ACTIVITY_PUBLISHED");
            }
            case CANCEL -> {
                if (!Set.of(ActivityStatus.DRAFT.code(), ActivityStatus.PUBLISHED.code())
                        .contains(item.getStatus())) {
                    throw new IllegalStateException("Activity cannot be cancelled");
                }
                requireReason(reason, "Cancellation reason is required");
                item.setStatus(ActivityStatus.CANCELLED.code());
                item.setCancellationReason(reason.trim());
                for (var row : participations.findByActivityId(activityId)) {
                    if (!CAPACITY_STATUSES.contains(row.getStatus())) continue;
                    row.setStatus(ParticipationStatus.CANCELLED.code());
                    row.setCancelledAt(OffsetDateTime.now());
                    if (PaymentStatus.PAID.matches(row.getPaymentStatus())) {
                        row.setPaymentStatus(PaymentStatus.REFUND_PENDING.code());
                    }
                    row.setUpdatedAt(OffsetDateTime.now());
                    participations.save(row);
                    notifyUser(row.getParticipantId(), item, "ACTIVITY_CANCELLED");
                }
            }
            case COMPLETE -> {
                requireActivityStatus(item, ActivityStatus.PUBLISHED);
                if (OffsetDateTime.now().isBefore(item.getStartsAt())) {
                    throw new IllegalStateException("Activity has not started yet");
                }
                item.setStatus(ActivityStatus.COMPLETED.code());
                item.setCompletedAt(OffsetDateTime.now());
            }
            case ARCHIVE -> {
                if (!Set.of(ActivityStatus.COMPLETED.code(), ActivityStatus.CANCELLED.code())
                        .contains(item.getStatus())) {
                    throw new IllegalStateException(
                            "Only completed or cancelled activities can be archived");
                }
                item.setStatus(ActivityStatus.ARCHIVED.code());
            }
        }
        item.setUpdatedAt(OffsetDateTime.now());
        item = activities.save(item);
        addEvent(item, null, actorId, role, requestedAction.code(), reason);
        return queries.activityView(item, actorId);
    }

    private void apply(
            SocietyActivity item,
            String title,
            String category,
            String description,
            String venue,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            OffsetDateTime registrationOpensAt,
            OffsetDateTime registrationClosesAt,
            Integer capacity,
            BigDecimal feePerPerson,
            boolean approvalRequired,
            int cancellationHours,
            String audience,
            String organizerName,
            String organizerMobile,
            String instructions) {
        item.setTitle(title.trim());
        item.setCategory(category);
        item.setDescription(description.trim());
        item.setVenue(venue.trim());
        item.setStartsAt(startsAt);
        item.setEndsAt(endsAt);
        item.setRegistrationOpensAt(registrationOpensAt);
        item.setRegistrationClosesAt(registrationClosesAt);
        item.setCapacity(capacity);
        item.setFeePerPerson(feePerPerson);
        item.setApprovalRequired(approvalRequired);
        item.setCancellationHours(cancellationHours);
        item.setAudience(audience);
        item.setOrganizerName(blankToNull(organizerName));
        item.setOrganizerMobile(blankToNull(organizerMobile));
        item.setInstructions(blankToNull(instructions));
        item.setUpdatedAt(OffsetDateTime.now());
    }

    private void validateSchedule(
            OffsetDateTime start,
            OffsetDateTime end,
            OffsetDateTime registrationOpen,
            OffsetDateTime registrationClose,
            Integer capacity) {
        if (!end.isAfter(start))
            throw new IllegalArgumentException("Activity end must be after start");
        if (!start.isAfter(OffsetDateTime.now()))
            throw new IllegalArgumentException("Activity must start in the future");
        if (registrationClose.isBefore(registrationOpen)) {
            throw new IllegalArgumentException("Registration close must be after opening");
        }
        if (registrationClose.isAfter(start)) {
            throw new IllegalArgumentException(
                    "Registration must close before the activity starts");
        }
        if (capacity != null && capacity < 1)
            throw new IllegalArgumentException("Capacity must be positive");
    }

    private String activityCode() {
        return "ACT-"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"))
                + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
