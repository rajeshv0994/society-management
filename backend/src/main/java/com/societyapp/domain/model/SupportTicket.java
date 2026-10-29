package com.societyapp.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class SupportTicket extends BaseUuidEntity {
    private UUID societyId;
    private UUID createdBy;
    private String subject;
    private String description;
    private String priority;
    private String status;
    private UUID assignedTo;
    private String resolution;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime closedAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getCreatedBy() {
        return this.createdBy;
    }

    @java.lang.SuppressWarnings("all")
    public String getSubject() {
        return this.subject;
    }

    @java.lang.SuppressWarnings("all")
    public String getDescription() {
        return this.description;
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
    public UUID getAssignedTo() {
        return this.assignedTo;
    }

    @java.lang.SuppressWarnings("all")
    public String getResolution() {
        return this.resolution;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getClosedAt() {
        return this.closedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setSocietyId(final UUID societyId) {
        this.societyId = societyId;
    }

    @java.lang.SuppressWarnings("all")
    public void setCreatedBy(final UUID createdBy) {
        this.createdBy = createdBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setSubject(final String subject) {
        this.subject = subject;
    }

    @java.lang.SuppressWarnings("all")
    public void setDescription(final String description) {
        this.description = description;
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
    public void setAssignedTo(final UUID assignedTo) {
        this.assignedTo = assignedTo;
    }

    @java.lang.SuppressWarnings("all")
    public void setResolution(final String resolution) {
        this.resolution = resolution;
    }

    @java.lang.SuppressWarnings("all")
    public void setCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setUpdatedAt(final OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setClosedAt(final OffsetDateTime closedAt) {
        this.closedAt = closedAt;
    }

    @java.lang.SuppressWarnings("all")
    public SupportTicket() {}
}
