package com.societyapp.infrastructure.messaging;

import com.societyapp.application.port.in.NotificationOutboxDispatchUseCase;
import org.springframework.scheduling.annotation.Scheduled;

public class NotificationOutboxScheduler {
    private final NotificationOutboxDispatchUseCase dispatcher;
    private final int batchSize;
    private final int maxAttempts;

    public NotificationOutboxScheduler(
            NotificationOutboxDispatchUseCase dispatcher, int batchSize, int maxAttempts) {
        if (batchSize < 1 || batchSize > 500) {
            throw new IllegalArgumentException("Kafka batch size must be between 1 and 500");
        }
        if (maxAttempts < 1 || maxAttempts > 100) {
            throw new IllegalArgumentException("Kafka max attempts must be between 1 and 100");
        }
        this.dispatcher = dispatcher;
        this.batchSize = batchSize;
        this.maxAttempts = maxAttempts;
    }

    @Scheduled(fixedDelayString = "${app.kafka.dispatch-delay-ms:5000}")
    public void dispatch() {
        dispatcher.dispatchBatch(batchSize, maxAttempts);
    }
}
