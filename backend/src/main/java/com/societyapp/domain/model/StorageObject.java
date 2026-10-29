package com.societyapp.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class StorageObject extends BaseUuidEntity {
    private UUID societyId;
    private String storageKey;
    private String category;
    private String originalFilename;
    private String contentType;
    private long sizeBytes;
    private String sourceType;
    private UUID sourceId;
    private String displayName;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime deletedAt;
    private UUID deletedBy;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public String getStorageKey() {
        return this.storageKey;
    }

    @java.lang.SuppressWarnings("all")
    public String getCategory() {
        return this.category;
    }

    @java.lang.SuppressWarnings("all")
    public String getOriginalFilename() {
        return this.originalFilename;
    }

    @java.lang.SuppressWarnings("all")
    public String getContentType() {
        return this.contentType;
    }

    @java.lang.SuppressWarnings("all")
    public long getSizeBytes() {
        return this.sizeBytes;
    }

    @java.lang.SuppressWarnings("all")
    public String getSourceType() {
        return this.sourceType;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getSourceId() {
        return this.sourceId;
    }

    @java.lang.SuppressWarnings("all")
    public String getDisplayName() {
        return this.displayName;
    }

    @java.lang.SuppressWarnings("all")
    public String getStatus() {
        return this.status;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getDeletedAt() {
        return this.deletedAt;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getDeletedBy() {
        return this.deletedBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setSocietyId(final UUID societyId) {
        this.societyId = societyId;
    }

    @java.lang.SuppressWarnings("all")
    public void setStorageKey(final String storageKey) {
        this.storageKey = storageKey;
    }

    @java.lang.SuppressWarnings("all")
    public void setCategory(final String category) {
        this.category = category;
    }

    @java.lang.SuppressWarnings("all")
    public void setOriginalFilename(final String originalFilename) {
        this.originalFilename = originalFilename;
    }

    @java.lang.SuppressWarnings("all")
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    @java.lang.SuppressWarnings("all")
    public void setSizeBytes(final long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    @java.lang.SuppressWarnings("all")
    public void setSourceType(final String sourceType) {
        this.sourceType = sourceType;
    }

    @java.lang.SuppressWarnings("all")
    public void setSourceId(final UUID sourceId) {
        this.sourceId = sourceId;
    }

    @java.lang.SuppressWarnings("all")
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    @java.lang.SuppressWarnings("all")
    public void setStatus(final String status) {
        this.status = status;
    }

    @java.lang.SuppressWarnings("all")
    public void setCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setDeletedAt(final OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setDeletedBy(final UUID deletedBy) {
        this.deletedBy = deletedBy;
    }

    @java.lang.SuppressWarnings("all")
    public StorageObject() {}
}
