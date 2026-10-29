package com.societyapp.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class RequirementRequest extends BaseUuidEntity {
    private UUID societyId;
    private String societyName;
    private String contactPerson;
    private String email;
    private String mobile;
    private String city;
    private Integer estimatedFlats;
    private Integer estimatedStaff;
    private String requestedModules;
    private String requirementDetails;
    private BigDecimal budgetMonthly;
    private String status;
    private String adminNotes;
    private UUID assignedTo;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime closedAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public String getSocietyName() {
        return this.societyName;
    }

    @java.lang.SuppressWarnings("all")
    public String getContactPerson() {
        return this.contactPerson;
    }

    @java.lang.SuppressWarnings("all")
    public String getEmail() {
        return this.email;
    }

    @java.lang.SuppressWarnings("all")
    public String getMobile() {
        return this.mobile;
    }

    @java.lang.SuppressWarnings("all")
    public String getCity() {
        return this.city;
    }

    @java.lang.SuppressWarnings("all")
    public Integer getEstimatedFlats() {
        return this.estimatedFlats;
    }

    @java.lang.SuppressWarnings("all")
    public Integer getEstimatedStaff() {
        return this.estimatedStaff;
    }

    @java.lang.SuppressWarnings("all")
    public String getRequestedModules() {
        return this.requestedModules;
    }

    @java.lang.SuppressWarnings("all")
    public String getRequirementDetails() {
        return this.requirementDetails;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getBudgetMonthly() {
        return this.budgetMonthly;
    }

    @java.lang.SuppressWarnings("all")
    public String getStatus() {
        return this.status;
    }

    @java.lang.SuppressWarnings("all")
    public String getAdminNotes() {
        return this.adminNotes;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getAssignedTo() {
        return this.assignedTo;
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
    public void setSocietyName(final String societyName) {
        this.societyName = societyName;
    }

    @java.lang.SuppressWarnings("all")
    public void setContactPerson(final String contactPerson) {
        this.contactPerson = contactPerson;
    }

    @java.lang.SuppressWarnings("all")
    public void setEmail(final String email) {
        this.email = email;
    }

    @java.lang.SuppressWarnings("all")
    public void setMobile(final String mobile) {
        this.mobile = mobile;
    }

    @java.lang.SuppressWarnings("all")
    public void setCity(final String city) {
        this.city = city;
    }

    @java.lang.SuppressWarnings("all")
    public void setEstimatedFlats(final Integer estimatedFlats) {
        this.estimatedFlats = estimatedFlats;
    }

    @java.lang.SuppressWarnings("all")
    public void setEstimatedStaff(final Integer estimatedStaff) {
        this.estimatedStaff = estimatedStaff;
    }

    @java.lang.SuppressWarnings("all")
    public void setRequestedModules(final String requestedModules) {
        this.requestedModules = requestedModules;
    }

    @java.lang.SuppressWarnings("all")
    public void setRequirementDetails(final String requirementDetails) {
        this.requirementDetails = requirementDetails;
    }

    @java.lang.SuppressWarnings("all")
    public void setBudgetMonthly(final BigDecimal budgetMonthly) {
        this.budgetMonthly = budgetMonthly;
    }

    @java.lang.SuppressWarnings("all")
    public void setStatus(final String status) {
        this.status = status;
    }

    @java.lang.SuppressWarnings("all")
    public void setAdminNotes(final String adminNotes) {
        this.adminNotes = adminNotes;
    }

    @java.lang.SuppressWarnings("all")
    public void setAssignedTo(final UUID assignedTo) {
        this.assignedTo = assignedTo;
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
    public RequirementRequest() {}
}
