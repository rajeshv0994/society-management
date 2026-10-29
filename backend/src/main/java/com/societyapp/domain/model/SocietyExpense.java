package com.societyapp.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class SocietyExpense extends BaseUuidEntity {
    private UUID societyId;
    private String category;
    private String vendor;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String paymentMethod;
    private String paymentReference;
    private UUID createdBy;
    private OffsetDateTime createdAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public String getCategory() {
        return this.category;
    }

    @java.lang.SuppressWarnings("all")
    public String getVendor() {
        return this.vendor;
    }

    @java.lang.SuppressWarnings("all")
    public String getDescription() {
        return this.description;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getAmount() {
        return this.amount;
    }

    @java.lang.SuppressWarnings("all")
    public LocalDate getExpenseDate() {
        return this.expenseDate;
    }

    @java.lang.SuppressWarnings("all")
    public String getPaymentMethod() {
        return this.paymentMethod;
    }

    @java.lang.SuppressWarnings("all")
    public String getPaymentReference() {
        return this.paymentReference;
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
    public void setSocietyId(final UUID societyId) {
        this.societyId = societyId;
    }

    @java.lang.SuppressWarnings("all")
    public void setCategory(final String category) {
        this.category = category;
    }

    @java.lang.SuppressWarnings("all")
    public void setVendor(final String vendor) {
        this.vendor = vendor;
    }

    @java.lang.SuppressWarnings("all")
    public void setDescription(final String description) {
        this.description = description;
    }

    @java.lang.SuppressWarnings("all")
    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    @java.lang.SuppressWarnings("all")
    public void setExpenseDate(final LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    @java.lang.SuppressWarnings("all")
    public void setPaymentMethod(final String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @java.lang.SuppressWarnings("all")
    public void setPaymentReference(final String paymentReference) {
        this.paymentReference = paymentReference;
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
    public SocietyExpense() {}
}
