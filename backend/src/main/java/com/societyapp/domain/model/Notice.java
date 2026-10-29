package com.societyapp.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Notice extends BaseUuidEntity {
    private UUID societyId;
    private String title;
    private String body;
    private String audience;
    private UUID publishedBy;
    private OffsetDateTime publishedAt;
    private OffsetDateTime expiresAt;
    private String status;
    private String priority;
    private boolean pinned;
    private boolean requiresAcknowledgement;
    private OffsetDateTime updatedAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public String getTitle() {
        return this.title;
    }

    @java.lang.SuppressWarnings("all")
    public String getBody() {
        return this.body;
    }

    @java.lang.SuppressWarnings("all")
    public String getAudience() {
        return this.audience;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getPublishedBy() {
        return this.publishedBy;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getPublishedAt() {
        return this.publishedAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getExpiresAt() {
        return this.expiresAt;
    }

    @java.lang.SuppressWarnings("all")
    public String getStatus() {
        return this.status;
    }

    @java.lang.SuppressWarnings("all")
    public String getPriority() {
        return this.priority;
    }

    @java.lang.SuppressWarnings("all")
    public boolean isPinned() {
        return this.pinned;
    }

    @java.lang.SuppressWarnings("all")
    public boolean isRequiresAcknowledgement() {
        return this.requiresAcknowledgement;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setSocietyId(final UUID societyId) {
        this.societyId = societyId;
    }

    @java.lang.SuppressWarnings("all")
    public void setTitle(final String title) {
        this.title = title;
    }

    @java.lang.SuppressWarnings("all")
    public void setBody(final String body) {
        this.body = body;
    }

    @java.lang.SuppressWarnings("all")
    public void setAudience(final String audience) {
        this.audience = audience;
    }

    @java.lang.SuppressWarnings("all")
    public void setPublishedBy(final UUID publishedBy) {
        this.publishedBy = publishedBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setPublishedAt(final OffsetDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setExpiresAt(final OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setStatus(final String status) {
        this.status = status;
    }

    @java.lang.SuppressWarnings("all")
    public void setPriority(final String priority) {
        this.priority = priority;
    }

    @java.lang.SuppressWarnings("all")
    public void setPinned(final boolean pinned) {
        this.pinned = pinned;
    }

    @java.lang.SuppressWarnings("all")
    public void setRequiresAcknowledgement(final boolean requiresAcknowledgement) {
        this.requiresAcknowledgement = requiresAcknowledgement;
    }

    @java.lang.SuppressWarnings("all")
    public void setUpdatedAt(final OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @java.lang.SuppressWarnings("all")
    public Notice() {}
}
