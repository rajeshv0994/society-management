package com.societyapp.application.command;

import com.societyapp.application.exception.AuthorizationException;
import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.port.out.DocumentStorage;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.query.ActivityQueryCoordinator;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.time.OffsetDateTime;
import java.util.*;

abstract class ActivityWorkflowSupport {
    protected static final Set<String> CAPACITY_STATUSES =
            Set.of(
                    PaymentStatus.PENDING_PAYMENT.code(),
                    ParticipationStatus.PENDING_APPROVAL.code(),
                    ParticipationStatus.CONFIRMED.code(),
                    ParticipationStatus.CHECKED_IN.code(),
                    ParticipationStatus.ATTENDED.code());
    protected static final Set<PaymentMethod> ONLINE_METHODS =
            Set.of(PaymentMethod.UPI, PaymentMethod.CARD, PaymentMethod.NET_BANKING);

    protected final SocietyActivityRepository activities;
    protected final ActivityParticipationRepository participations;
    protected final ActivityGalleryPhotoRepository photos;
    protected final ActivityEventRepository events;
    protected final UserAccountJpaRepository users;
    protected final StaffPermissionRepository permissions;
    protected final SocietyPaymentSettingRepository paymentSettings;
    protected final NotificationOutboxRepository notifications;
    protected final DocumentStorage storage;
    protected final ActivityQueryCoordinator queries;

    protected ActivityWorkflowSupport(
            SocietyActivityRepository activities,
            ActivityParticipationRepository participations,
            ActivityGalleryPhotoRepository photos,
            ActivityEventRepository events,
            UserAccountJpaRepository users,
            StaffPermissionRepository permissions,
            SocietyPaymentSettingRepository paymentSettings,
            NotificationOutboxRepository notifications,
            DocumentStorage storage,
            ActivityQueryCoordinator queries) {
        this.activities = activities;
        this.participations = participations;
        this.photos = photos;
        this.events = events;
        this.users = users;
        this.permissions = permissions;
        this.paymentSettings = paymentSettings;
        this.notifications = notifications;
        this.storage = storage;
        this.queries = queries;
    }

    protected int registeredCount(UUID activityId) {
        return participations.findByActivityId(activityId).stream()
                .filter(row -> CAPACITY_STATUSES.contains(row.getStatus()))
                .mapToInt(ActivityParticipation::getParticipantCount)
                .sum();
    }

    protected void requireOwnerOrManager(ActivityParticipation row, UUID actorId, String role) {
        if (UserRole.RESIDENT.matches(role)) {
            if (!actorId.equals(row.getParticipantId())) {
                throw new AuthorizationException("You can only manage your own registration");
            }
        } else {
            requireManager(actorId, role);
        }
    }

    protected void requireManager(UUID actorId, String role) {
        if (UserRole.SOCIETY_ADMIN.matches(role)) return;
        if (UserRole.STAFF.matches(role)
                && permissions.existsByStaffUserIdAndPermissionCode(
                        actorId, StaffPermissionCode.ACTIVITIES_GALLERIES.code())) return;
        throw new AuthorizationException("Activities and galleries permission is required");
    }

    protected void requireAdmin(String role) {
        if (!UserRole.SOCIETY_ADMIN.matches(role)) {
            throw new AuthorizationException("Society administrator access is required");
        }
    }

    protected SocietyActivity requireActivity(UUID societyId, UUID id) {
        return activities
                .findByIdAndSocietyId(id, societyId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));
    }

    protected ActivityParticipation requireParticipation(UUID societyId, UUID id) {
        return participations
                .findByIdAndSocietyId(id, societyId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Activity registration not found"));
    }

    protected void requireActivityStatus(SocietyActivity item, ActivityStatus status) {
        if (!status.matches(item.getStatus())) {
            throw new IllegalStateException(
                    "Activity must be " + status.code().toLowerCase(Locale.ROOT));
        }
    }

    protected void requireParticipationStatus(
            ActivityParticipation item, ParticipationStatus status) {
        if (!status.matches(item.getStatus())) {
            throw new IllegalStateException(
                    "Registration must be " + status.code().toLowerCase(Locale.ROOT));
        }
    }

    protected void requireReason(String reason, String message) {
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException(message);
    }

    protected void notifyResidents(UUID societyId, SocietyActivity item, String template) {
        users.findBySocietyIdAndRoleOrderByFullName(societyId, UserRole.RESIDENT.code()).stream()
                .filter(user -> AccountStatus.ACTIVE.matches(user.getStatus()))
                .forEach(user -> notify(user, item, template));
    }

    protected void notifyUser(UUID userId, SocietyActivity item, String template) {
        users.findById(userId).ifPresent(user -> notify(user, item, template));
    }

    private void notify(UserAccount user, SocietyActivity item, String template) {
        if (user.getEmail() == null || user.getEmail().isBlank()) return;
        var message = new NotificationOutbox();
        message.setSocietyId(item.getSocietyId());
        message.setChannel(com.societyapp.domain.type.NotificationChannel.EMAIL.code());
        message.setDestination(user.getEmail());
        message.setTemplateCode(template);
        message.setPayload(
                "{\"activityCode\":\""
                        + item.getActivityCode()
                        + "\",\"title\":\""
                        + item.getTitle().replace("\"", "")
                        + "\"}");
        message.setStatus(NotificationStatus.PENDING.code());
        message.setAttempts(0);
        message.setCreatedAt(OffsetDateTime.now());
        notifications.save(message);
    }

    protected void addEvent(
            SocietyActivity activity,
            ActivityParticipation participation,
            UUID actorId,
            String role,
            String type,
            String note) {
        var event = new ActivityEvent();
        event.setSocietyId(activity.getSocietyId());
        event.setActivityId(activity.getId());
        event.setParticipationId(participation == null ? null : participation.getId());
        event.setEventType(type);
        event.setNote(blankToNull(note));
        event.setActorId(actorId);
        event.setActorRole(role);
        event.setOccurredAt(OffsetDateTime.now());
        events.save(event);
    }

    protected String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
