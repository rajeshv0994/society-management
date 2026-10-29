package com.societyapp.domain.model;

import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

public class SocietyCustomPlan extends BaseUuidEntity {
    private UUID societyId;
    private UUID requirementId;
    private String planName;
    private String description;
    private String enabledModules;
    private Integer maxFlats;
    private Integer maxStaff;
    private Integer maxStorageGb;
    private BigDecimal priceMonthly;
    private BigDecimal pricePerSpace;
    private BigDecimal pricePerStaff;
    private BigDecimal setupFee;
    private String billingCycle;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private String status;
    private UUID createdBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime activatedAt;
    private OffsetDateTime cancelledAt;
    private UUID cancelledBy;
    private String cancellationReason;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getRequirementId() {
        return this.requirementId;
    }

    @java.lang.SuppressWarnings("all")
    public String getPlanName() {
        return this.planName;
    }

    @java.lang.SuppressWarnings("all")
    public String getDescription() {
        return this.description;
    }

    @java.lang.SuppressWarnings("all")
    public String getEnabledModules() {
        return this.enabledModules;
    }

    @java.lang.SuppressWarnings("all")
    public Integer getMaxFlats() {
        return this.maxFlats;
    }

    @java.lang.SuppressWarnings("all")
    public Integer getMaxStaff() {
        return this.maxStaff;
    }

    @java.lang.SuppressWarnings("all")
    public Integer getMaxStorageGb() {
        return this.maxStorageGb;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getPriceMonthly() {
        return this.priceMonthly;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getPricePerSpace() {
        return this.pricePerSpace;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getPricePerStaff() {
        return this.pricePerStaff;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getSetupFee() {
        return this.setupFee;
    }

    @java.lang.SuppressWarnings("all")
    public String getBillingCycle() {
        return this.billingCycle;
    }

    @java.lang.SuppressWarnings("all")
    public LocalDate getValidFrom() {
        return this.validFrom;
    }

    @java.lang.SuppressWarnings("all")
    public LocalDate getValidUntil() {
        return this.validUntil;
    }

    @java.lang.SuppressWarnings("all")
    public String getStatus() {
        return this.status;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getCreatedBy() {
        return this.createdBy;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getActivatedAt() {
        return this.activatedAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCancelledAt() {
        return this.cancelledAt;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getCancelledBy() {
        return this.cancelledBy;
    }

    @java.lang.SuppressWarnings("all")
    public String getCancellationReason() {
        return this.cancellationReason;
    }

    @java.lang.SuppressWarnings("all")
    public void setSocietyId(final UUID societyId) {
        this.societyId = societyId;
    }

    @java.lang.SuppressWarnings("all")
    public void setRequirementId(final UUID requirementId) {
        this.requirementId = requirementId;
    }

    @java.lang.SuppressWarnings("all")
    public void setPlanName(final String planName) {
        this.planName = planName;
    }

    @java.lang.SuppressWarnings("all")
    public void setDescription(final String description) {
        this.description = description;
    }

    @java.lang.SuppressWarnings("all")
    public void setEnabledModules(final String enabledModules) {
        this.enabledModules = enabledModules;
    }

    @java.lang.SuppressWarnings("all")
    public void setMaxFlats(final Integer maxFlats) {
        this.maxFlats = maxFlats;
    }

    @java.lang.SuppressWarnings("all")
    public void setMaxStaff(final Integer maxStaff) {
        this.maxStaff = maxStaff;
    }

    @java.lang.SuppressWarnings("all")
    public void setMaxStorageGb(final Integer maxStorageGb) {
        this.maxStorageGb = maxStorageGb;
    }

    @java.lang.SuppressWarnings("all")
    public void setPriceMonthly(final BigDecimal priceMonthly) {
        this.priceMonthly = priceMonthly;
    }

    @java.lang.SuppressWarnings("all")
    public void setPricePerSpace(final BigDecimal pricePerSpace) {
        this.pricePerSpace = pricePerSpace;
    }

    @java.lang.SuppressWarnings("all")
    public void setPricePerStaff(final BigDecimal pricePerStaff) {
        this.pricePerStaff = pricePerStaff;
    }

    @java.lang.SuppressWarnings("all")
    public void setSetupFee(final BigDecimal setupFee) {
        this.setupFee = setupFee;
    }

    @java.lang.SuppressWarnings("all")
    public void setBillingCycle(final String billingCycle) {
        this.billingCycle = billingCycle;
    }

    @java.lang.SuppressWarnings("all")
    public void setValidFrom(final LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    @java.lang.SuppressWarnings("all")
    public void setValidUntil(final LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    @java.lang.SuppressWarnings("all")
    public void setStatus(final String status) {
        this.status = status;
    }

    @java.lang.SuppressWarnings("all")
    public void setCreatedBy(final UUID createdBy) {
        this.createdBy = createdBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setActivatedAt(final OffsetDateTime activatedAt) {
        this.activatedAt = activatedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setCancelledAt(final OffsetDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setCancelledBy(final UUID cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setCancellationReason(final String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    @java.lang.SuppressWarnings("all")
    public SocietyCustomPlan() {}
}
