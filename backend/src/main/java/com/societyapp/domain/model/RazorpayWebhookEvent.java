package com.societyapp.domain.model;

import java.time.OffsetDateTime;

public class RazorpayWebhookEvent {
    private String eventId;
    private String eventType;
    private OffsetDateTime receivedAt;
    private OffsetDateTime processedAt;

    @java.lang.SuppressWarnings("all")
    public String getEventId() {
        return this.eventId;
    }

    @java.lang.SuppressWarnings("all")
    public String getEventType() {
        return this.eventType;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getReceivedAt() {
        return this.receivedAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getProcessedAt() {
        return this.processedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setEventId(final String eventId) {
        this.eventId = eventId;
    }

    @java.lang.SuppressWarnings("all")
    public void setEventType(final String eventType) {
        this.eventType = eventType;
    }

    @java.lang.SuppressWarnings("all")
    public void setReceivedAt(final OffsetDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setProcessedAt(final OffsetDateTime processedAt) {
        this.processedAt = processedAt;
    }

    @java.lang.SuppressWarnings("all")
    public RazorpayWebhookEvent() {}
}
