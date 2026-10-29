package com.societyapp.application.command;

import com.societyapp.application.exception.*;
import com.societyapp.application.model.activity.ActivityModels;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.query.ActivityQueryCoordinator;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.math.*;
import java.time.*;
import java.util.*;

public final class ActivityParticipationCoordinator extends ActivityWorkflowSupport {
    public ActivityParticipationCoordinator(
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

    public ActivityModels.ParticipationView join(
            UUID societyId,
            UUID actorId,
            String role,
            UUID activityId,
            int count,
            String names,
            String mobile,
            String notes) {
        if (!UserRole.RESIDENT.matches(role)) {
            throw new AuthorizationException("Only members can register for activities");
        }
        var item = requireActivity(societyId, activityId);
        requireActivityStatus(item, ActivityStatus.PUBLISHED);
        var now = OffsetDateTime.now();
        if (now.isBefore(item.getRegistrationOpensAt())
                || now.isAfter(item.getRegistrationClosesAt())) {
            throw new IllegalStateException("Activity registration is not open");
        }
        if (participations.findByActivityIdAndParticipantId(activityId, actorId).isPresent()) {
            throw new ConflictException("You already registered for this activity");
        }
        int registered = registeredCount(activityId);
        if (item.getCapacity() != null && registered + count > item.getCapacity()) {
            throw new ConflictException(
                    "Only " + Math.max(0, item.getCapacity() - registered) + " places remain");
        }
        var row = new ActivityParticipation();
        row.setSocietyId(societyId);
        row.setActivityId(activityId);
        row.setParticipantId(actorId);
        row.setParticipantCount(count);
        row.setParticipantNames(blankToNull(names));
        row.setContactMobile(blankToNull(mobile));
        row.setNotes(blankToNull(notes));
        row.setAmount(item.getFeePerPerson().multiply(BigDecimal.valueOf(count)));
        row.setPaymentStatus(
                row.getAmount().signum() == 0
                        ? PaymentStatus.FREE.code()
                        : PaymentStatus.PENDING.code());
        row.setStatus(
                row.getAmount().signum() > 0
                        ? PaymentStatus.PENDING_PAYMENT.code()
                        : item.isApprovalRequired()
                                ? ParticipationStatus.PENDING_APPROVAL.code()
                                : ParticipationStatus.CONFIRMED.code());
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        row = participations.save(row);
        addEvent(item, row, actorId, role, "REGISTERED", notes);
        notifyUser(actorId, item, "ACTIVITY_REGISTRATION_CREATED");
        return queries.participationView(row);
    }

    public ActivityModels.ParticipationView checkout(
            UUID societyId, UUID actorId, String role, UUID participationId, String method) {
        var requestedMethod = PaymentMethod.from(method);
        if (!ONLINE_METHODS.contains(requestedMethod)) {
            throw new IllegalArgumentException("Invalid Razorpay payment method");
        }
        var row = requireParticipation(societyId, participationId);
        requireOwnerOrManager(row, actorId, role);
        requirePendingPayment(row);
        var setting =
                paymentSettings
                        .findBySocietyId(societyId)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Society online payment account is not configured"));
        if (!setting.isTestMode()) {
            throw new IllegalStateException("Live Razorpay activity payments are not enabled yet");
        }
        row.setPaymentStatus(PaymentStatus.PAID.code());
        row.setPaymentMethod(requestedMethod.razorpayCode());
        row.setPaymentReference(
                "pay_activity_test_"
                        + UUID.randomUUID().toString().replace("-", "").substring(0, 18));
        row.setPaidAt(OffsetDateTime.now());
        moveAfterPayment(row);
        row.setUpdatedAt(OffsetDateTime.now());
        row = participations.save(row);
        var activity = requireActivity(societyId, row.getActivityId());
        addEvent(activity, row, actorId, role, "PAYMENT_COMPLETED", requestedMethod.code());
        return queries.participationView(row);
    }

    public ActivityModels.ParticipationView recordCash(
            UUID societyId, UUID actorId, String role, UUID participationId) {
        requireManager(actorId, role);
        var row = requireParticipation(societyId, participationId);
        requirePendingPayment(row);
        row.setPaymentStatus(PaymentStatus.PAID.code());
        row.setPaymentMethod(PaymentMethod.CASH.code());
        row.setPaymentReference(
                "ACT-CASH-"
                        + UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 12)
                                .toUpperCase());
        row.setPaidAt(OffsetDateTime.now());
        moveAfterPayment(row);
        row.setUpdatedAt(OffsetDateTime.now());
        row = participations.save(row);
        addEvent(
                requireActivity(societyId, row.getActivityId()),
                row,
                actorId,
                role,
                "CASH_RECORDED",
                null);
        return queries.participationView(row);
    }

    public ActivityModels.ParticipationView participationAction(
            UUID societyId,
            UUID actorId,
            String role,
            UUID participationId,
            String action,
            String reason) {
        var row = requireParticipation(societyId, participationId);
        var activity = requireActivity(societyId, row.getActivityId());
        var requestedAction = ParticipationAction.from(action);
        if (requestedAction == ParticipationAction.CANCEL) {
            requireOwnerOrManager(row, actorId, role);
            if (!Set.of(
                            PaymentStatus.PENDING_PAYMENT.code(),
                            ParticipationStatus.PENDING_APPROVAL.code(),
                            ParticipationStatus.CONFIRMED.code())
                    .contains(row.getStatus())) {
                throw new IllegalStateException("Registration cannot be cancelled");
            }
            if (UserRole.RESIDENT.matches(role)
                    && OffsetDateTime.now()
                            .plusHours(activity.getCancellationHours())
                            .isAfter(activity.getStartsAt())) {
                throw new IllegalStateException(
                        "Cancellation requires "
                                + activity.getCancellationHours()
                                + " hours notice");
            }
            row.setStatus(ParticipationStatus.CANCELLED.code());
            row.setCancelledAt(OffsetDateTime.now());
            if (PaymentStatus.PAID.matches(row.getPaymentStatus()))
                row.setPaymentStatus(PaymentStatus.REFUND_PENDING.code());
        } else {
            requireManager(actorId, role);
            switch (requestedAction) {
                case APPROVE -> {
                    requireParticipationStatus(row, ParticipationStatus.PENDING_APPROVAL);
                    row.setStatus(ParticipationStatus.CONFIRMED.code());
                    row.setReviewedBy(actorId);
                    row.setReviewedAt(OffsetDateTime.now());
                }
                case REJECT -> {
                    requireParticipationStatus(row, ParticipationStatus.PENDING_APPROVAL);
                    requireReason(reason, "Rejection reason is required");
                    row.setStatus(ParticipationStatus.REJECTED.code());
                    row.setRejectionReason(reason.trim());
                    row.setReviewedBy(actorId);
                    row.setReviewedAt(OffsetDateTime.now());
                    if (PaymentStatus.PAID.matches(row.getPaymentStatus()))
                        row.setPaymentStatus(PaymentStatus.REFUND_PENDING.code());
                }
                case CHECK_IN -> {
                    requireParticipationStatus(row, ParticipationStatus.CONFIRMED);
                    if (OffsetDateTime.now().isBefore(activity.getStartsAt().minusHours(12))) {
                        throw new IllegalStateException(
                                "Check-in opens 12 hours before the activity");
                    }
                    if (OffsetDateTime.now().isAfter(activity.getEndsAt())) {
                        throw new IllegalStateException("Activity check-in has closed");
                    }
                    row.setStatus(ParticipationStatus.CHECKED_IN.code());
                    row.setCheckedInAt(OffsetDateTime.now());
                }
                case ATTENDED -> {
                    if (!Set.of(
                                    ParticipationStatus.CONFIRMED.code(),
                                    ParticipationStatus.CHECKED_IN.code())
                            .contains(row.getStatus())) {
                        throw new IllegalStateException(
                                "Participant is not confirmed or checked in");
                    }
                    if (OffsetDateTime.now().isBefore(activity.getStartsAt())) {
                        throw new IllegalStateException(
                                "Attendance can be recorded after the activity starts");
                    }
                    row.setStatus(ParticipationStatus.ATTENDED.code());
                    row.setAttendedAt(OffsetDateTime.now());
                }
                case NO_SHOW -> {
                    requireParticipationStatus(row, ParticipationStatus.CONFIRMED);
                    if (OffsetDateTime.now().isBefore(activity.getEndsAt())) {
                        throw new IllegalStateException(
                                "No-show can be recorded after the activity ends");
                    }
                    row.setStatus(ParticipationStatus.NO_SHOW.code());
                }
                case REFUND -> {
                    if (!PaymentStatus.REFUND_PENDING.matches(row.getPaymentStatus())) {
                        throw new IllegalStateException("Registration has no pending refund");
                    }
                    row.setPaymentStatus(PaymentStatus.REFUNDED.code());
                }
                case CANCEL -> throw new IllegalStateException("Cancellation was already handled");
            }
        }
        row.setUpdatedAt(OffsetDateTime.now());
        row = participations.save(row);
        addEvent(activity, row, actorId, role, requestedAction.code(), reason);
        notifyUser(row.getParticipantId(), activity, "ACTIVITY_REGISTRATION_UPDATED");
        return queries.participationView(row);
    }

    private void moveAfterPayment(ActivityParticipation row) {
        var activity = activities.findById(row.getActivityId()).orElseThrow();
        row.setStatus(
                activity.isApprovalRequired()
                        ? ParticipationStatus.PENDING_APPROVAL.code()
                        : ParticipationStatus.CONFIRMED.code());
    }

    private void requirePendingPayment(ActivityParticipation row) {
        if (!PaymentStatus.PENDING.matches(row.getPaymentStatus()))
            throw new IllegalStateException("Registration has no pending payment");
    }
}
