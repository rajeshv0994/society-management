package com.societyapp.application.query;

import com.societyapp.application.exception.*;
import com.societyapp.application.model.activity.ActivityModels;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.time.OffsetDateTime;
import java.util.*;

public final class ActivityQueryCoordinator {
    private static final Set<String> CAPACITY_STATUSES =
            Set.of(
                    PaymentStatus.PENDING_PAYMENT.code(),
                    ParticipationStatus.PENDING_APPROVAL.code(),
                    ParticipationStatus.CONFIRMED.code(),
                    ParticipationStatus.CHECKED_IN.code(),
                    ParticipationStatus.ATTENDED.code());
    private final SocietyActivityRepository activities;
    private final ActivityParticipationRepository participations;
    private final ActivityGalleryPhotoRepository photos;
    private final ActivityEventRepository events;
    private final UserAccountJpaRepository users;
    private final StaffPermissionRepository permissions;

    public ActivityQueryCoordinator(
            SocietyActivityRepository activities,
            ActivityParticipationRepository participations,
            ActivityGalleryPhotoRepository photos,
            ActivityEventRepository events,
            UserAccountJpaRepository users,
            StaffPermissionRepository permissions) {
        this.activities = activities;
        this.participations = participations;
        this.photos = photos;
        this.events = events;
        this.users = users;
        this.permissions = permissions;
    }

    public ActivityModels.Workspace workspace(UUID societyId, ActivityModels.Actor actor) {
        if (actor.role() == UserRole.STAFF) requireManager(actor);
        var activityRows =
                activities.findBySocietyIdOrderByStartsAtDesc(societyId).stream()
                        .filter(
                                item ->
                                        actor.role() != UserRole.RESIDENT
                                                || Set.of(
                                                                ActivityStatus.PUBLISHED.code(),
                                                                ActivityStatus.COMPLETED.code(),
                                                                ActivityStatus.CANCELLED.code())
                                                        .contains(item.getStatus()))
                        .toList();
        var participationRows =
                actor.role() == UserRole.RESIDENT
                        ? participations.findBySocietyIdAndParticipantIdOrderByCreatedAtDesc(
                                societyId, actor.userId())
                        : participations.findBySocietyIdOrderByCreatedAtDesc(societyId);
        var photoRows =
                photos.findBySocietyIdOrderByCreatedAtDesc(societyId).stream()
                        .filter(
                                item ->
                                        actor.role() != UserRole.RESIDENT
                                                || ModerationStatus.PUBLISHED.matches(
                                                        item.getModerationStatus())
                                                || actor.userId().equals(item.getSubmittedBy()))
                        .toList();
        return new ActivityModels.Workspace(
                activityRows.stream().map(item -> activityView(item, actor.userId())).toList(),
                participationRows.stream().map(this::participationView).toList(),
                photoRows.stream().map(this::photoView).toList(),
                activityRows.size(),
                activityRows.stream()
                        .filter(item -> ActivityStatus.PUBLISHED.matches(item.getStatus()))
                        .filter(item -> item.getStartsAt().isAfter(OffsetDateTime.now()))
                        .count(),
                participationRows.stream()
                        .filter(item -> actor.userId().equals(item.getParticipantId()))
                        .filter(item -> CAPACITY_STATUSES.contains(item.getStatus()))
                        .count(),
                participationRows.stream()
                        .filter(item -> PaymentStatus.PENDING.matches(item.getPaymentStatus()))
                        .count(),
                participationRows.stream()
                        .filter(
                                item ->
                                        ParticipationStatus.PENDING_APPROVAL.matches(
                                                item.getStatus()))
                        .count(),
                photoRows.stream()
                        .filter(
                                item ->
                                        ModerationStatus.PENDING.matches(
                                                item.getModerationStatus()))
                        .count());
    }

    public List<ActivityModels.EventView> history(
            UUID societyId, ActivityModels.Actor actor, UUID activityId) {
        if (actor.role() == UserRole.STAFF) requireManager(actor);
        var activity = requireActivity(societyId, activityId);
        if (actor.role() == UserRole.RESIDENT
                && !Set.of(
                                ActivityStatus.PUBLISHED.code(),
                                ActivityStatus.COMPLETED.code(),
                                ActivityStatus.CANCELLED.code())
                        .contains(activity.getStatus())) {
            throw new ResourceNotFoundException("Activity not found");
        }
        return events.findByActivityIdOrderByOccurredAt(activityId).stream()
                .map(this::eventView)
                .toList();
    }

    public ActivityModels.ActivityView activityView(SocietyActivity item, UUID actorId) {
        int registered = registeredCount(item.getId());
        return new ActivityModels.ActivityView(
                item.getId(),
                item.getActivityCode(),
                item.getTitle(),
                ActivityCategory.from(item.getCategory()),
                item.getDescription(),
                item.getVenue(),
                item.getStartsAt(),
                item.getEndsAt(),
                item.getRegistrationOpensAt(),
                item.getRegistrationClosesAt(),
                item.getCapacity(),
                registered,
                item.getCapacity() == null ? null : Math.max(0, item.getCapacity() - registered),
                item.getFeePerPerson(),
                item.getFeePerPerson().signum() > 0,
                item.isApprovalRequired(),
                item.getCancellationHours(),
                ActivityAudience.from(item.getAudience()),
                item.getOrganizerName(),
                item.getOrganizerMobile(),
                item.getInstructions(),
                ActivityStatus.from(item.getStatus()),
                item.getCancellationReason(),
                item.getCreatedAt(),
                participations
                        .findByActivityIdAndParticipantId(item.getId(), actorId)
                        .map(this::participationView)
                        .orElse(null));
    }

    public ActivityModels.ParticipationView participationView(ActivityParticipation row) {
        var activity = activities.findById(row.getActivityId()).orElse(null);
        var user = users.findById(row.getParticipantId()).orElse(null);
        return new ActivityModels.ParticipationView(
                row.getId(),
                row.getActivityId(),
                activity == null ? null : activity.getTitle(),
                activity == null ? null : activity.getActivityCode(),
                activity == null ? null : activity.getStartsAt(),
                row.getParticipantId(),
                user == null ? null : user.getFullName(),
                user == null ? null : user.getEmail(),
                row.getParticipantCount(),
                row.getParticipantNames(),
                row.getContactMobile(),
                row.getNotes(),
                row.getAmount(),
                PaymentStatus.from(row.getPaymentStatus()),
                paymentMethod(row.getPaymentMethod()),
                row.getPaymentReference(),
                ParticipationStatus.from(row.getStatus()),
                row.getRejectionReason(),
                row.getCheckedInAt(),
                row.getAttendedAt(),
                row.getCreatedAt());
    }

    public ActivityModels.PhotoView photoView(ActivityGalleryPhoto photo) {
        return new ActivityModels.PhotoView(
                photo.getId(),
                photo.getActivityId(),
                activities
                        .findById(photo.getActivityId())
                        .map(SocietyActivity::getTitle)
                        .orElse(null),
                photo.getSubmittedBy(),
                users.findById(photo.getSubmittedBy()).map(UserAccount::getFullName).orElse(null),
                photo.getCaption(),
                ModerationStatus.from(photo.getModerationStatus()),
                photo.isFeatured(),
                photo.getRejectionReason(),
                photo.getCreatedAt(),
                "/api/activities/photos/" + photo.getId());
    }

    private ActivityModels.EventView eventView(ActivityEvent event) {
        return new ActivityModels.EventView(
                event.getId(),
                event.getEventType(),
                event.getNote(),
                UserRole.from(event.getActorRole()),
                event.getOccurredAt(),
                users.findById(event.getActorId()).map(UserAccount::getFullName).orElse(null));
    }

    private int registeredCount(UUID activityId) {
        return participations.findByActivityId(activityId).stream()
                .filter(row -> CAPACITY_STATUSES.contains(row.getStatus()))
                .mapToInt(ActivityParticipation::getParticipantCount)
                .sum();
    }

    private void requireManager(ActivityModels.Actor actor) {
        if (actor.role() == UserRole.SOCIETY_ADMIN) return;
        if (actor.role() == UserRole.STAFF
                && permissions.existsByStaffUserIdAndPermissionCode(
                        actor.userId(), StaffPermissionCode.ACTIVITIES_GALLERIES.code())) return;
        throw new AuthorizationException("Activities and galleries permission is required");
    }

    private SocietyActivity requireActivity(UUID societyId, UUID id) {
        return activities
                .findByIdAndSocietyId(id, societyId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));
    }

    private PaymentMethod paymentMethod(String value) {
        if (value == null || value.isBlank()) return null;
        return PaymentMethod.from(value.replaceFirst("^RAZORPAY_", ""));
    }
}
