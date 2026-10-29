package com.societyapp.application.model.activity;

import com.societyapp.application.model.BinaryContent;
import com.societyapp.domain.type.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class ActivityModels {
    private ActivityModels() {}

    public record Actor(UUID userId, UserRole role) {}

    public record ActivityCommand(
            String title,
            ActivityCategory category,
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
            ActivityAudience audience,
            String organizerName,
            String organizerMobile,
            String instructions,
            boolean publishNow) {}

    public record JoinCommand(
            int participantCount, String participantNames, String contactMobile, String notes) {}

    public record ActionCommand(ActivityAction action, String reason) {}

    public record ParticipationActionCommand(ParticipationAction action, String reason) {}

    public record PhotoActionCommand(PhotoAction action, String reason) {}

    public record ParticipationView(
            UUID id,
            UUID activityId,
            String activityTitle,
            String activityCode,
            OffsetDateTime startsAt,
            UUID participantId,
            String participantName,
            String participantEmail,
            int participantCount,
            String participantNames,
            String contactMobile,
            String notes,
            BigDecimal amount,
            PaymentStatus paymentStatus,
            PaymentMethod paymentMethod,
            String paymentReference,
            ParticipationStatus status,
            String rejectionReason,
            OffsetDateTime checkedInAt,
            OffsetDateTime attendedAt,
            OffsetDateTime createdAt) {}

    public record ActivityView(
            UUID id,
            String activityCode,
            String title,
            ActivityCategory category,
            String description,
            String venue,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            OffsetDateTime registrationOpensAt,
            OffsetDateTime registrationClosesAt,
            Integer capacity,
            int registeredCount,
            Integer availablePlaces,
            BigDecimal feePerPerson,
            boolean paid,
            boolean approvalRequired,
            int cancellationHours,
            ActivityAudience audience,
            String organizerName,
            String organizerMobile,
            String instructions,
            ActivityStatus status,
            String cancellationReason,
            OffsetDateTime createdAt,
            ParticipationView myParticipation) {}

    public record PhotoView(
            UUID id,
            UUID activityId,
            String activityTitle,
            UUID submittedBy,
            String submittedByName,
            String caption,
            ModerationStatus moderationStatus,
            boolean featured,
            String rejectionReason,
            OffsetDateTime createdAt,
            String photoUrl) {}

    public record EventView(
            UUID id,
            String eventType,
            String note,
            UserRole actorRole,
            OffsetDateTime occurredAt,
            String actorName) {}

    public record Workspace(
            List<ActivityView> activities,
            List<ParticipationView> participations,
            List<PhotoView> photos,
            int totalActivities,
            long upcoming,
            long myRegistrations,
            long pendingPayments,
            long pendingApprovals,
            long pendingPhotos) {}

    public record PhotoContent(BinaryContent content, String contentType) {}
}
