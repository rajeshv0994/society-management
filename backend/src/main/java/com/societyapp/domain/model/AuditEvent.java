package com.societyapp.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class AuditEvent {
    private Long id;
    private UUID societyId;
    private UUID actorUserId;
    private String action;
    private String entityType;
    private String entityId;
    private boolean sensitive;
    private String details;
    private OffsetDateTime occurredAt;

    @java.lang.SuppressWarnings("all")
    public Long getId() {
        return this.id;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getActorUserId() {
        return this.actorUserId;
    }

    @java.lang.SuppressWarnings("all")
    public String getAction() {
        return this.action;
    }

    @java.lang.SuppressWarnings("all")
    public String getEntityType() {
        return this.entityType;
    }

    @java.lang.SuppressWarnings("all")
    public String getEntityId() {
        return this.entityId;
    }

    @java.lang.SuppressWarnings("all")
    public boolean isSensitive() {
        return this.sensitive;
    }

    @java.lang.SuppressWarnings("all")
    public String getDetails() {
        return this.details;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getOccurredAt() {
        return this.occurredAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setId(final Long id) {
        this.id = id;
    }

    @java.lang.SuppressWarnings("all")
    public void setSocietyId(final UUID societyId) {
        this.societyId = societyId;
    }

    @java.lang.SuppressWarnings("all")
    public void setActorUserId(final UUID actorUserId) {
        this.actorUserId = actorUserId;
    }

    @java.lang.SuppressWarnings("all")
    public void setAction(final String action) {
        this.action = action;
    }

    @java.lang.SuppressWarnings("all")
    public void setEntityType(final String entityType) {
        this.entityType = entityType;
    }

    @java.lang.SuppressWarnings("all")
    public void setEntityId(final String entityId) {
        this.entityId = entityId;
    }

    @java.lang.SuppressWarnings("all")
    public void setSensitive(final boolean sensitive) {
        this.sensitive = sensitive;
    }

    @java.lang.SuppressWarnings("all")
    public void setDetails(final String details) {
        this.details = details;
    }

    @java.lang.SuppressWarnings("all")
    public void setOccurredAt(final OffsetDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    @java.lang.SuppressWarnings("all")
    public AuditEvent() {}
}
