package com.societyapp.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class SupportTicketMessage extends BaseUuidEntity {
    private UUID ticketId;
    private UUID senderId;
    private String message;
    private String messageType;
    private String attachmentKey;
    private OffsetDateTime createdAt;
    private OffsetDateTime readAt;

    @java.lang.SuppressWarnings("all")
    public UUID getTicketId() {
        return this.ticketId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getSenderId() {
        return this.senderId;
    }

    @java.lang.SuppressWarnings("all")
    public String getMessage() {
        return this.message;
    }

    @java.lang.SuppressWarnings("all")
    public String getMessageType() {
        return this.messageType;
    }

    @java.lang.SuppressWarnings("all")
    public String getAttachmentKey() {
        return this.attachmentKey;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getReadAt() {
        return this.readAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setTicketId(final UUID ticketId) {
        this.ticketId = ticketId;
    }

    @java.lang.SuppressWarnings("all")
    public void setSenderId(final UUID senderId) {
        this.senderId = senderId;
    }

    @java.lang.SuppressWarnings("all")
    public void setMessage(final String message) {
        this.message = message;
    }

    @java.lang.SuppressWarnings("all")
    public void setMessageType(final String messageType) {
        this.messageType = messageType;
    }

    @java.lang.SuppressWarnings("all")
    public void setAttachmentKey(final String attachmentKey) {
        this.attachmentKey = attachmentKey;
    }

    @java.lang.SuppressWarnings("all")
    public void setCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setReadAt(final OffsetDateTime readAt) {
        this.readAt = readAt;
    }

    @java.lang.SuppressWarnings("all")
    public SupportTicketMessage() {}
}
