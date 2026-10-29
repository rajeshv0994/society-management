package com.societyapp.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class SetupCode extends BaseUuidEntity {
    private UUID societyId;
    private String codeHash;
    private String codeHint;
    private OffsetDateTime expiresAt;
    private int maxActivations;
    private int activationCount;
    private String status;
    private OffsetDateTime createdAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public String getCodeHash() {
        return this.codeHash;
    }

    @java.lang.SuppressWarnings("all")
    public String getCodeHint() {
        return this.codeHint;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getExpiresAt() {
        return this.expiresAt;
    }

    @java.lang.SuppressWarnings("all")
    public int getMaxActivations() {
        return this.maxActivations;
    }

    @java.lang.SuppressWarnings("all")
    public int getActivationCount() {
        return this.activationCount;
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
    public void setSocietyId(final UUID societyId) {
        this.societyId = societyId;
    }

    @java.lang.SuppressWarnings("all")
    public void setCodeHash(final String codeHash) {
        this.codeHash = codeHash;
    }

    @java.lang.SuppressWarnings("all")
    public void setCodeHint(final String codeHint) {
        this.codeHint = codeHint;
    }

    @java.lang.SuppressWarnings("all")
    public void setExpiresAt(final OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setMaxActivations(final int maxActivations) {
        this.maxActivations = maxActivations;
    }

    @java.lang.SuppressWarnings("all")
    public void setActivationCount(final int activationCount) {
        this.activationCount = activationCount;
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
    public SetupCode() {}
}
