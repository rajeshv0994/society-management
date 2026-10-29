package com.societyapp.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class VisitorPhoto extends BaseUuidEntity {
    private UUID visitorId;
    private String storageKey;
    private String contentType;
    private OffsetDateTime createdAt;

    @java.lang.SuppressWarnings("all")
    public UUID getVisitorId() {
        return this.visitorId;
    }

    @java.lang.SuppressWarnings("all")
    public String getStorageKey() {
        return this.storageKey;
    }

    @java.lang.SuppressWarnings("all")
    public String getContentType() {
        return this.contentType;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setVisitorId(final UUID visitorId) {
        this.visitorId = visitorId;
    }

    @java.lang.SuppressWarnings("all")
    public void setStorageKey(final String storageKey) {
        this.storageKey = storageKey;
    }

    @java.lang.SuppressWarnings("all")
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    @java.lang.SuppressWarnings("all")
    public void setCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public VisitorPhoto() {}
}
