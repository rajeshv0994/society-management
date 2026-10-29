package com.societyapp.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class MaintenanceChargeRule extends BaseUuidEntity {
    private UUID societyId;
    private String name;
    private String calculationType;
    private BigDecimal amount;
    private String frequency;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public String getName() {
        return this.name;
    }

    @java.lang.SuppressWarnings("all")
    public String getCalculationType() {
        return this.calculationType;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getAmount() {
        return this.amount;
    }

    @java.lang.SuppressWarnings("all")
    public String getFrequency() {
        return this.frequency;
    }

    @java.lang.SuppressWarnings("all")
    public LocalDate getEffectiveFrom() {
        return this.effectiveFrom;
    }

    @java.lang.SuppressWarnings("all")
    public LocalDate getEffectiveTo() {
        return this.effectiveTo;
    }

    @java.lang.SuppressWarnings("all")
    public boolean isActive() {
        return this.active;
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
    public void setName(final String name) {
        this.name = name;
    }

    @java.lang.SuppressWarnings("all")
    public void setCalculationType(final String calculationType) {
        this.calculationType = calculationType;
    }

    @java.lang.SuppressWarnings("all")
    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    @java.lang.SuppressWarnings("all")
    public void setFrequency(final String frequency) {
        this.frequency = frequency;
    }

    @java.lang.SuppressWarnings("all")
    public void setEffectiveFrom(final LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    @java.lang.SuppressWarnings("all")
    public void setEffectiveTo(final LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    @java.lang.SuppressWarnings("all")
    public void setActive(final boolean active) {
        this.active = active;
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
    public MaintenanceChargeRule() {}
}
