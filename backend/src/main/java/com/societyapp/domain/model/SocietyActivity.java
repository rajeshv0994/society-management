package com.societyapp.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class SocietyActivity extends BaseUuidEntity {
    private UUID societyId;
    private String activityCode;
    private String title;
    private String category;
    private String description;
    private String venue;
    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;
    private OffsetDateTime registrationOpensAt;
    private OffsetDateTime registrationClosesAt;
    private Integer capacity;
    private BigDecimal feePerPerson;
    private boolean approvalRequired;
    private int cancellationHours;
    private String audience;
    private String organizerName;
    private String organizerMobile;
    private String instructions;
    private String status;
    private String cancellationReason;
    private UUID createdBy;
    private OffsetDateTime publishedAt;
    private OffsetDateTime completedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public String getActivityCode() {
        return this.activityCode;
    }

    @java.lang.SuppressWarnings("all")
    public String getTitle() {
        return this.title;
    }

    @java.lang.SuppressWarnings("all")
    public String getCategory() {
        return this.category;
    }

    @java.lang.SuppressWarnings("all")
    public String getDescription() {
        return this.description;
    }

    @java.lang.SuppressWarnings("all")
    public String getVenue() {
        return this.venue;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getStartsAt() {
        return this.startsAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getEndsAt() {
        return this.endsAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getRegistrationOpensAt() {
        return this.registrationOpensAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getRegistrationClosesAt() {
        return this.registrationClosesAt;
    }

    @java.lang.SuppressWarnings("all")
    public Integer getCapacity() {
        return this.capacity;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getFeePerPerson() {
        return this.feePerPerson;
    }

    @java.lang.SuppressWarnings("all")
    public boolean isApprovalRequired() {
        return this.approvalRequired;
    }

    @java.lang.SuppressWarnings("all")
    public int getCancellationHours() {
        return this.cancellationHours;
    }

    @java.lang.SuppressWarnings("all")
    public String getAudience() {
        return this.audience;
    }

    @java.lang.SuppressWarnings("all")
    public String getOrganizerName() {
        return this.organizerName;
    }

    @java.lang.SuppressWarnings("all")
    public String getOrganizerMobile() {
        return this.organizerMobile;
    }

    @java.lang.SuppressWarnings("all")
    public String getInstructions() {
        return this.instructions;
    }

    @java.lang.SuppressWarnings("all")
    public String getStatus() {
        return this.status;
    }

    @java.lang.SuppressWarnings("all")
    public String getCancellationReason() {
        return this.cancellationReason;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getCreatedBy() {
        return this.createdBy;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getPublishedAt() {
        return this.publishedAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCompletedAt() {
        return this.completedAt;
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
    public void setSocietyId(final UUID societyId) {
        this.societyId = societyId;
    }

    @java.lang.SuppressWarnings("all")
    public void setActivityCode(final String activityCode) {
        this.activityCode = activityCode;
    }

    @java.lang.SuppressWarnings("all")
    public void setTitle(final String title) {
        this.title = title;
    }

    @java.lang.SuppressWarnings("all")
    public void setCategory(final String category) {
        this.category = category;
    }

    @java.lang.SuppressWarnings("all")
    public void setDescription(final String description) {
        this.description = description;
    }

    @java.lang.SuppressWarnings("all")
    public void setVenue(final String venue) {
        this.venue = venue;
    }

    @java.lang.SuppressWarnings("all")
    public void setStartsAt(final OffsetDateTime startsAt) {
        this.startsAt = startsAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setEndsAt(final OffsetDateTime endsAt) {
        this.endsAt = endsAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setRegistrationOpensAt(final OffsetDateTime registrationOpensAt) {
        this.registrationOpensAt = registrationOpensAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setRegistrationClosesAt(final OffsetDateTime registrationClosesAt) {
        this.registrationClosesAt = registrationClosesAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setCapacity(final Integer capacity) {
        this.capacity = capacity;
    }

    @java.lang.SuppressWarnings("all")
    public void setFeePerPerson(final BigDecimal feePerPerson) {
        this.feePerPerson = feePerPerson;
    }

    @java.lang.SuppressWarnings("all")
    public void setApprovalRequired(final boolean approvalRequired) {
        this.approvalRequired = approvalRequired;
    }

    @java.lang.SuppressWarnings("all")
    public void setCancellationHours(final int cancellationHours) {
        this.cancellationHours = cancellationHours;
    }

    @java.lang.SuppressWarnings("all")
    public void setAudience(final String audience) {
        this.audience = audience;
    }

    @java.lang.SuppressWarnings("all")
    public void setOrganizerName(final String organizerName) {
        this.organizerName = organizerName;
    }

    @java.lang.SuppressWarnings("all")
    public void setOrganizerMobile(final String organizerMobile) {
        this.organizerMobile = organizerMobile;
    }

    @java.lang.SuppressWarnings("all")
    public void setInstructions(final String instructions) {
        this.instructions = instructions;
    }

    @java.lang.SuppressWarnings("all")
    public void setStatus(final String status) {
        this.status = status;
    }

    @java.lang.SuppressWarnings("all")
    public void setCancellationReason(final String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    @java.lang.SuppressWarnings("all")
    public void setCreatedBy(final UUID createdBy) {
        this.createdBy = createdBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setPublishedAt(final OffsetDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setCompletedAt(final OffsetDateTime completedAt) {
        this.completedAt = completedAt;
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
    public SocietyActivity() {}
}
