package com.societyapp.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ActivityEvent extends BaseUuidEntity {
    private UUID societyId;
    private UUID activityId;
    private UUID participationId;
    private String eventType;
    private String note;
    private UUID actorId;
    private String actorRole;
    private OffsetDateTime occurredAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getActivityId() {
        return this.activityId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getParticipationId() {
        return this.participationId;
    }

    @java.lang.SuppressWarnings("all")
    public String getEventType() {
        return this.eventType;
    }

    @java.lang.SuppressWarnings("all")
    public String getNote() {
        return this.note;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getActorId() {
        return this.actorId;
    }

    @java.lang.SuppressWarnings("all")
    public String getActorRole() {
        return this.actorRole;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getOccurredAt() {
        return this.occurredAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setSocietyId(final UUID societyId) {
        this.societyId = societyId;
    }

    @java.lang.SuppressWarnings("all")
    public void setActivityId(final UUID activityId) {
        this.activityId = activityId;
    }

    @java.lang.SuppressWarnings("all")
    public void setParticipationId(final UUID participationId) {
        this.participationId = participationId;
    }

    @java.lang.SuppressWarnings("all")
    public void setEventType(final String eventType) {
        this.eventType = eventType;
    }

    @java.lang.SuppressWarnings("all")
    public void setNote(final String note) {
        this.note = note;
    }

    @java.lang.SuppressWarnings("all")
    public void setActorId(final UUID actorId) {
        this.actorId = actorId;
    }

    @java.lang.SuppressWarnings("all")
    public void setActorRole(final String actorRole) {
        this.actorRole = actorRole;
    }

    @java.lang.SuppressWarnings("all")
    public void setOccurredAt(final OffsetDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    @java.lang.SuppressWarnings("all")
    public ActivityEvent() {}
}
