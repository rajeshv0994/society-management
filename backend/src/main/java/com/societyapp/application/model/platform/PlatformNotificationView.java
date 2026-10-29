package com.societyapp.application.model.platform;

import com.societyapp.domain.type.NotificationChannel;
import com.societyapp.domain.type.NotificationStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PlatformNotificationView(
        UUID id,
        UUID societyId,
        String societyName,
        NotificationChannel channel,
        String destination,
        String templateCode,
        String payload,
        NotificationStatus status,
        int attempts,
        OffsetDateTime createdAt,
        OffsetDateTime sentAt,
        String lastError) {}
