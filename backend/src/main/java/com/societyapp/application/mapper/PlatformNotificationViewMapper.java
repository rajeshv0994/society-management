package com.societyapp.application.mapper;

import com.societyapp.application.model.platform.PlatformNotificationView;
import com.societyapp.domain.model.NotificationOutbox;
import com.societyapp.domain.type.NotificationChannel;
import com.societyapp.domain.type.NotificationStatus;

public class PlatformNotificationViewMapper {
    public PlatformNotificationView toView(NotificationOutbox notification, String societyName) {
        return new PlatformNotificationView(
                notification.getId(),
                notification.getSocietyId(),
                societyName,
                NotificationChannel.from(notification.getChannel()),
                notification.getDestination(),
                notification.getTemplateCode(),
                notification.getPayload(),
                NotificationStatus.from(notification.getStatus()),
                notification.getAttempts(),
                notification.getCreatedAt(),
                notification.getSentAt(),
                notification.getLastError());
    }
}
