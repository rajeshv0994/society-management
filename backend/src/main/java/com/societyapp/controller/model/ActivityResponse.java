package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class ActivityResponse {
    private ActivityResponse() {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Participation(
            UUID id,
            @JsonProperty("activity_id") UUID activityId,
            @JsonProperty("activity_title") String activityTitle,
            @JsonProperty("activity_code") String activityCode,
            @JsonProperty("starts_at") OffsetDateTime startsAt,
            @JsonProperty("participant_id") UUID participantId,
            @JsonProperty("participant_name") String participantName,
            @JsonProperty("participant_email") String participantEmail,
            @JsonProperty("participant_count") int participantCount,
            @JsonProperty("participant_names") String participantNames,
            @JsonProperty("contact_mobile") String contactMobile,
            String notes,
            BigDecimal amount,
            @JsonProperty("payment_status") String paymentStatus,
            @JsonProperty("payment_method") String paymentMethod,
            @JsonProperty("payment_reference") String paymentReference,
            String status,
            @JsonProperty("rejection_reason") String rejectionReason,
            @JsonProperty("checked_in_at") OffsetDateTime checkedInAt,
            @JsonProperty("attended_at") OffsetDateTime attendedAt,
            @JsonProperty("created_at") OffsetDateTime createdAt) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Activity(
            UUID id,
            @JsonProperty("activity_code") String activityCode,
            String title,
            String category,
            String description,
            String venue,
            @JsonProperty("starts_at") OffsetDateTime startsAt,
            @JsonProperty("ends_at") OffsetDateTime endsAt,
            @JsonProperty("registration_opens_at") OffsetDateTime registrationOpensAt,
            @JsonProperty("registration_closes_at") OffsetDateTime registrationClosesAt,
            Integer capacity,
            @JsonProperty("registered_count") int registeredCount,
            @JsonProperty("available_places") Integer availablePlaces,
            @JsonProperty("fee_per_person") BigDecimal feePerPerson,
            @JsonProperty("is_paid") boolean paid,
            @JsonProperty("approval_required") boolean approvalRequired,
            @JsonProperty("cancellation_hours") int cancellationHours,
            String audience,
            @JsonProperty("organizer_name") String organizerName,
            @JsonProperty("organizer_mobile") String organizerMobile,
            String instructions,
            String status,
            @JsonProperty("cancellation_reason") String cancellationReason,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("my_participation") Participation myParticipation) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Photo(
            UUID id,
            @JsonProperty("activity_id") UUID activityId,
            @JsonProperty("activity_title") String activityTitle,
            @JsonProperty("submitted_by") UUID submittedBy,
            @JsonProperty("submitted_by_name") String submittedByName,
            String caption,
            @JsonProperty("moderation_status") String moderationStatus,
            boolean featured,
            @JsonProperty("rejection_reason") String rejectionReason,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("photo_url") String photoUrl) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Event(
            UUID id,
            @JsonProperty("event_type") String eventType,
            String note,
            @JsonProperty("actor_role") String actorRole,
            @JsonProperty("occurred_at") OffsetDateTime occurredAt,
            @JsonProperty("actor_name") String actorName) {}

    public record Workspace(
            List<Activity> activities,
            List<Participation> participations,
            List<Photo> photos,
            @JsonProperty("total_activities") int totalActivities,
            long upcoming,
            @JsonProperty("my_registrations") long myRegistrations,
            @JsonProperty("pending_payments") long pendingPayments,
            @JsonProperty("pending_approvals") long pendingApprovals,
            @JsonProperty("pending_photos") long pendingPhotos) {}
}
