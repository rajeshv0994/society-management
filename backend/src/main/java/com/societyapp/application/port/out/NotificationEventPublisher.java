package com.societyapp.application.port.out;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface NotificationEventPublisher {
    void publish(NotificationEvent event);

    record NotificationEvent(
            UUID eventId,
            UUID societyId,
            String channel,
            String destination,
            String templateCode,
            String payload,
            OffsetDateTime createdAt) {}
}
