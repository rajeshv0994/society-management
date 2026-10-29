package com.societyapp.application.usecase;

import com.societyapp.application.port.in.NotificationOutboxDispatchUseCase;
import com.societyapp.application.port.out.NotificationEventPublisher;
import com.societyapp.application.port.out.NotificationEventPublisher.NotificationEvent;
import com.societyapp.application.port.out.persistence.NotificationOutboxRepository;
import com.societyapp.domain.model.NotificationOutbox;
import com.societyapp.domain.type.NotificationStatus;
import java.time.OffsetDateTime;

public class NotificationOutboxDispatchService implements NotificationOutboxDispatchUseCase {
    private static final int MAX_ERROR_LENGTH = 2000;

    private final NotificationOutboxRepository notifications;
    private final NotificationEventPublisher publisher;

    public NotificationOutboxDispatchService(
            NotificationOutboxRepository notifications, NotificationEventPublisher publisher) {
        this.notifications = notifications;
        this.publisher = publisher;
    }

    @Override
    public DispatchResult dispatchBatch(int batchSize, int maxAttempts) {
        if (batchSize < 1 || batchSize > 500) {
            throw new IllegalArgumentException("Kafka batch size must be between 1 and 500");
        }
        if (maxAttempts < 1 || maxAttempts > 100) {
            throw new IllegalArgumentException("Kafka max attempts must be between 1 and 100");
        }

        var pending = notifications.findPendingForDispatch(batchSize);
        int published = 0;
        int failed = 0;
        for (var notification : pending) {
            try {
                publisher.publish(toEvent(notification));
                notification.setStatus(NotificationStatus.SENT.code());
                notification.setSentAt(OffsetDateTime.now());
                notification.setLastError(null);
                published++;
            } catch (RuntimeException exception) {
                int attempts = notification.getAttempts() + 1;
                notification.setAttempts(attempts);
                notification.setLastError(errorMessage(exception));
                if (attempts >= maxAttempts) {
                    notification.setStatus(NotificationStatus.FAILED.code());
                }
                failed++;
            }
            notifications.save(notification);
        }
        return new DispatchResult(pending.size(), published, failed);
    }

    private NotificationEvent toEvent(NotificationOutbox notification) {
        return new NotificationEvent(
                notification.getId(),
                notification.getSocietyId(),
                notification.getChannel(),
                notification.getDestination(),
                notification.getTemplateCode(),
                notification.getPayload(),
                notification.getCreatedAt());
    }

    private String errorMessage(RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) message = exception.getClass().getSimpleName();
        return message.length() <= MAX_ERROR_LENGTH
                ? message
                : message.substring(0, MAX_ERROR_LENGTH);
    }
}
