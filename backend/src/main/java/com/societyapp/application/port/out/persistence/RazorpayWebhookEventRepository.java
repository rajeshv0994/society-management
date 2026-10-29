package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.RazorpayWebhookEvent;

public interface RazorpayWebhookEventRepository
        extends CrudRepositoryPort<RazorpayWebhookEvent, String> {
    int claim(String eventId, String eventType);

    void markProcessed(String eventId);
}
