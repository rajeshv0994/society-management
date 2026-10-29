package com.societyapp.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class NotificationOutbox extends BaseUuidEntity {
    private UUID societyId;
    private String channel;
    private String destination;
    private String templateCode;
    private String payload;
    private String status;
    private int attempts;
    private OffsetDateTime createdAt;
    private OffsetDateTime sentAt;
    private String lastError;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public String getChannel() {
        return this.channel;
    }

    @java.lang.SuppressWarnings("all")
    public String getDestination() {
        return this.destination;
    }

    @java.lang.SuppressWarnings("all")
    public String getTemplateCode() {
        return this.templateCode;
    }

    @java.lang.SuppressWarnings("all")
    public String getPayload() {
        return this.payload;
    }

    @java.lang.SuppressWarnings("all")
    public String getStatus() {
        return this.status;
    }

    @java.lang.SuppressWarnings("all")
    public int getAttempts() {
        return this.attempts;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getSentAt() {
        return this.sentAt;
    }

    @java.lang.SuppressWarnings("all")
    public String getLastError() {
        return this.lastError;
    }

    @java.lang.SuppressWarnings("all")
    public void setSocietyId(final UUID societyId) {
        this.societyId = societyId;
    }

    @java.lang.SuppressWarnings("all")
    public void setChannel(final String channel) {
        this.channel = channel;
    }

    @java.lang.SuppressWarnings("all")
    public void setDestination(final String destination) {
        this.destination = destination;
    }

    @java.lang.SuppressWarnings("all")
    public void setTemplateCode(final String templateCode) {
        this.templateCode = templateCode;
    }

    @java.lang.SuppressWarnings("all")
    public void setPayload(final String payload) {
        this.payload = payload;
    }

    @java.lang.SuppressWarnings("all")
    public void setStatus(final String status) {
        this.status = status;
    }

    @java.lang.SuppressWarnings("all")
    public void setAttempts(final int attempts) {
        this.attempts = attempts;
    }

    @java.lang.SuppressWarnings("all")
    public void setCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setSentAt(final OffsetDateTime sentAt) {
        this.sentAt = sentAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setLastError(final String lastError) {
        this.lastError = lastError;
    }

    @java.lang.SuppressWarnings("all")
    public NotificationOutbox() {}
}
