package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PlatformNotificationResponse(
        UUID id,
        @JsonProperty("society_id") UUID societyId,
        @JsonProperty("society_name") String societyName,
        String channel,
        String destination,
        @JsonProperty("template_code") String templateCode,
        String payload,
        String status,
        int attempts,
        @JsonProperty("created_at") OffsetDateTime createdAt,
        @JsonProperty("sent_at") OffsetDateTime sentAt,
        @JsonProperty("last_error") String lastError) {}
