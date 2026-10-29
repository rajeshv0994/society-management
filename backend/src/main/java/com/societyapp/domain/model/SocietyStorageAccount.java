package com.societyapp.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class SocietyStorageAccount {
    private UUID societyId;
    private long purchasedBytes;
    private OffsetDateTime updatedAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public long getPurchasedBytes() {
        return this.purchasedBytes;
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
    public void setPurchasedBytes(final long purchasedBytes) {
        this.purchasedBytes = purchasedBytes;
    }

    @java.lang.SuppressWarnings("all")
    public void setUpdatedAt(final OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @java.lang.SuppressWarnings("all")
    public SocietyStorageAccount() {}
}
