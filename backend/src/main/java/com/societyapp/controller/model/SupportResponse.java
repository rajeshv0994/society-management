package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class SupportResponse {
    private SupportResponse() {}

    public record Ticket(
            UUID id,
            @JsonProperty("society_id") UUID societyId,
            @JsonProperty("created_by") UUID createdBy,
            String subject,
            String description,
            String priority,
            String status,
            @JsonProperty("assigned_to") UUID assignedTo,
            String resolution,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("updated_at") OffsetDateTime updatedAt,
            @JsonProperty("closed_at") OffsetDateTime closedAt,
            @JsonProperty("created_by_name") String createdByName,
            @JsonProperty("message_count") int messageCount) {}

    public record Message(
            UUID id,
            String message,
            @JsonProperty("message_type") String messageType,
            @JsonProperty("attachment_key") String attachmentKey,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("read_at") OffsetDateTime readAt,
            @JsonProperty("sender_id") UUID senderId,
            @JsonProperty("sender_name") String senderName,
            @JsonProperty("sender_role") String senderRole) {}

    public record Created(UUID ticketId, String status) {}

    public record Sent(UUID messageId, boolean created) {}
}
