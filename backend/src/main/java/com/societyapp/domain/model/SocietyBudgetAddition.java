package com.societyapp.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class SocietyBudgetAddition extends BaseUuidEntity {
    private UUID societyId;
    private int budgetYear;
    private BigDecimal amount;
    private String note;
    private UUID addedBy;
    private OffsetDateTime addedAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public int getBudgetYear() {
        return this.budgetYear;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getAmount() {
        return this.amount;
    }

    @java.lang.SuppressWarnings("all")
    public String getNote() {
        return this.note;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getAddedBy() {
        return this.addedBy;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getAddedAt() {
        return this.addedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setSocietyId(final UUID societyId) {
        this.societyId = societyId;
    }

    @java.lang.SuppressWarnings("all")
    public void setBudgetYear(final int budgetYear) {
        this.budgetYear = budgetYear;
    }

    @java.lang.SuppressWarnings("all")
    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    @java.lang.SuppressWarnings("all")
    public void setNote(final String note) {
        this.note = note;
    }

    @java.lang.SuppressWarnings("all")
    public void setAddedBy(final UUID addedBy) {
        this.addedBy = addedBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setAddedAt(final OffsetDateTime addedAt) {
        this.addedAt = addedAt;
    }

    @java.lang.SuppressWarnings("all")
    public SocietyBudgetAddition() {}
}
