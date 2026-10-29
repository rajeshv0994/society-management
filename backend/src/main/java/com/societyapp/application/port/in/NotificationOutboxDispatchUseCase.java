package com.societyapp.application.port.in;

public interface NotificationOutboxDispatchUseCase {
    DispatchResult dispatchBatch(int batchSize, int maxAttempts);

    record DispatchResult(int selected, int published, int failed) {}
}
