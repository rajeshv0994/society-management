package com.societyapp.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class VerificationChallenge extends BaseUuidEntity {
    private UUID userId;
    private String channel;
    private String destination;
    private String codeHash;
    private OffsetDateTime expiresAt;
    private int attempts;
    private OffsetDateTime verifiedAt;
    private OffsetDateTime createdAt;

    @java.lang.SuppressWarnings("all")
    public UUID getUserId() {
        return this.userId;
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
    public String getCodeHash() {
        return this.codeHash;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getExpiresAt() {
        return this.expiresAt;
    }

    @java.lang.SuppressWarnings("all")
    public int getAttempts() {
        return this.attempts;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getVerifiedAt() {
        return this.verifiedAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setUserId(final UUID userId) {
        this.userId = userId;
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
    public void setCodeHash(final String codeHash) {
        this.codeHash = codeHash;
    }

    @java.lang.SuppressWarnings("all")
    public void setExpiresAt(final OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setAttempts(final int attempts) {
        this.attempts = attempts;
    }

    @java.lang.SuppressWarnings("all")
    public void setVerifiedAt(final OffsetDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public VerificationChallenge() {}
}
