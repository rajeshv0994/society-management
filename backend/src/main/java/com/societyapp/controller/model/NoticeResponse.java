package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class NoticeResponse {
    private NoticeResponse() {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Notice(
            UUID id,
            String title,
            String body,
            String audience,
            String priority,
            boolean pinned,
            @JsonProperty("requires_acknowledgement") boolean requiresAcknowledgement,
            String status,
            @JsonProperty("published_at") OffsetDateTime publishedAt,
            @JsonProperty("expires_at") OffsetDateTime expiresAt,
            @JsonProperty("updated_at") OffsetDateTime updatedAt,
            boolean expired,
            boolean read,
            @JsonProperty("read_at") OffsetDateTime readAt,
            boolean acknowledged,
            @JsonProperty("acknowledged_at") OffsetDateTime acknowledgedAt,
            @JsonProperty("published_by") UUID publishedBy,
            @JsonProperty("published_by_name") String publishedByName,
            @JsonProperty("read_count") Long readCount,
            @JsonProperty("acknowledgement_count") Long acknowledgementCount) {}

    public record Options(
            @JsonProperty("can_manage") boolean canManage,
            List<String> audiences,
            List<String> priorities,
            List<String> statuses) {}

    public record Status(UUID id, String status) {}

    public record Read(UUID id, boolean read) {}

    public record Acknowledgement(UUID id, boolean acknowledged) {}
}
