package com.societyapp.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class StaffTaskUpdate extends BaseUuidEntity {
    private UUID taskId;
    private UUID actorUserId;
    private String eventType;
    private String fromStatus;
    private String toStatus;
    private String note;
    private OffsetDateTime createdAt;

    @java.lang.SuppressWarnings("all")
    public UUID getTaskId() {
        return this.taskId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getActorUserId() {
        return this.actorUserId;
    }

    @java.lang.SuppressWarnings("all")
    public String getEventType() {
        return this.eventType;
    }

    @java.lang.SuppressWarnings("all")
    public String getFromStatus() {
        return this.fromStatus;
    }

    @java.lang.SuppressWarnings("all")
    public String getToStatus() {
        return this.toStatus;
    }

    @java.lang.SuppressWarnings("all")
    public String getNote() {
        return this.note;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setTaskId(final UUID taskId) {
        this.taskId = taskId;
    }

    @java.lang.SuppressWarnings("all")
    public void setActorUserId(final UUID actorUserId) {
        this.actorUserId = actorUserId;
    }

    @java.lang.SuppressWarnings("all")
    public void setEventType(final String eventType) {
        this.eventType = eventType;
    }

    @java.lang.SuppressWarnings("all")
    public void setFromStatus(final String fromStatus) {
        this.fromStatus = fromStatus;
    }

    @java.lang.SuppressWarnings("all")
    public void setToStatus(final String toStatus) {
        this.toStatus = toStatus;
    }

    @java.lang.SuppressWarnings("all")
    public void setNote(final String note) {
        this.note = note;
    }

    @java.lang.SuppressWarnings("all")
    public void setCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public StaffTaskUpdate() {}
}
