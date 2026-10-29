package com.societyapp.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class StaffTask extends BaseUuidEntity {
    private UUID societyId;
    private String title;
    private String description;
    private UUID assignedTo;
    private UUID assignedBy;
    private OffsetDateTime dueAt;
    private String priority;
    private String status;
    private String completionNote;
    private OffsetDateTime createdAt;
    private OffsetDateTime completedAt;
    private OffsetDateTime startedAt;
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
    public String getDescription() {
        return this.description;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getAssignedTo() {
        return this.assignedTo;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getAssignedBy() {
        return this.assignedBy;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getDueAt() {
        return this.dueAt;
    }

    @java.lang.SuppressWarnings("all")
    public String getPriority() {
        return this.priority;
    }

    @java.lang.SuppressWarnings("all")
    public String getStatus() {
        return this.status;
    }

    @java.lang.SuppressWarnings("all")
    public String getCompletionNote() {
        return this.completionNote;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCompletedAt() {
        return this.completedAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getStartedAt() {
        return this.startedAt;
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
    public void setDescription(final String description) {
        this.description = description;
    }

    @java.lang.SuppressWarnings("all")
    public void setAssignedTo(final UUID assignedTo) {
        this.assignedTo = assignedTo;
    }

    @java.lang.SuppressWarnings("all")
    public void setAssignedBy(final UUID assignedBy) {
        this.assignedBy = assignedBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setDueAt(final OffsetDateTime dueAt) {
        this.dueAt = dueAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setPriority(final String priority) {
        this.priority = priority;
    }

    @java.lang.SuppressWarnings("all")
    public void setStatus(final String status) {
        this.status = status;
    }

    @java.lang.SuppressWarnings("all")
    public void setCompletionNote(final String completionNote) {
        this.completionNote = completionNote;
    }

    @java.lang.SuppressWarnings("all")
    public void setCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setCompletedAt(final OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setStartedAt(final OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setUpdatedAt(final OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @java.lang.SuppressWarnings("all")
    public StaffTask() {}
}
