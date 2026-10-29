package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AuditPageResponse(
        List<Entry> items,
        int page,
        int size,
        long total,
        @JsonProperty("total_pages") int totalPages) {
    public record Entry(
            Long id,
            @JsonProperty("society_id") UUID societyId,
            @JsonProperty("society_name") String societyName,
            @JsonProperty("actor_user_id") UUID actorUserId,
            @JsonProperty("actor_name") String actorName,
            String action,
            @JsonProperty("entity_type") String entityType,
            @JsonProperty("entity_id") String entityId,
            boolean sensitive,
            Map<String, ?> details,
            @JsonProperty("occurred_at") OffsetDateTime occurredAt) {}
}
