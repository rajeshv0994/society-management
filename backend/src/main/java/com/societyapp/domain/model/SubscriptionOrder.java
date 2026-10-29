package com.societyapp.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class SubscriptionOrder extends BaseUuidEntity {
    private UUID societyId;
    private String invoiceNumber;
    private LocalDate billingMonth;
    private LocalDate dueDate;
    private String billingCycle;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String planCode;
    private BigDecimal amount;
    private String currency;
    private String provider;
    private String providerReference;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime paidAt;
    private String paymentMethod;
    private String paymentNote;
    private OffsetDateTime submittedAt;
    private OffsetDateTime reviewedAt;
    private UUID reviewedBy;
    private String rejectionReason;
    private String pricingBreakdown = "[]";

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public String getInvoiceNumber() {
        return this.invoiceNumber;
    }

    @java.lang.SuppressWarnings("all")
    public LocalDate getBillingMonth() {
        return this.billingMonth;
    }

    @java.lang.SuppressWarnings("all")
    public LocalDate getDueDate() {
        return this.dueDate;
    }

    @java.lang.SuppressWarnings("all")
    public String getBillingCycle() {
        return this.billingCycle;
    }

    @java.lang.SuppressWarnings("all")
    public LocalDate getPeriodStart() {
        return this.periodStart;
    }

    @java.lang.SuppressWarnings("all")
    public LocalDate getPeriodEnd() {
        return this.periodEnd;
    }

    @java.lang.SuppressWarnings("all")
    public String getPlanCode() {
        return this.planCode;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getAmount() {
        return this.amount;
    }

    @java.lang.SuppressWarnings("all")
    public String getCurrency() {
        return this.currency;
    }

    @java.lang.SuppressWarnings("all")
    public String getProvider() {
        return this.provider;
    }

    @java.lang.SuppressWarnings("all")
    public String getProviderReference() {
        return this.providerReference;
    }

    @java.lang.SuppressWarnings("all")
    public String getStatus() {
        return this.status;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getPaidAt() {
        return this.paidAt;
    }

    @java.lang.SuppressWarnings("all")
    public String getPaymentMethod() {
        return this.paymentMethod;
    }

    @java.lang.SuppressWarnings("all")
    public String getPaymentNote() {
        return this.paymentNote;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getSubmittedAt() {
        return this.submittedAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getReviewedAt() {
        return this.reviewedAt;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getReviewedBy() {
        return this.reviewedBy;
    }

    @java.lang.SuppressWarnings("all")
    public String getRejectionReason() {
        return this.rejectionReason;
    }

    @java.lang.SuppressWarnings("all")
    public String getPricingBreakdown() {
        return this.pricingBreakdown;
    }

    @java.lang.SuppressWarnings("all")
    public void setSocietyId(final UUID societyId) {
        this.societyId = societyId;
    }

    @java.lang.SuppressWarnings("all")
    public void setInvoiceNumber(final String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    @java.lang.SuppressWarnings("all")
    public void setBillingMonth(final LocalDate billingMonth) {
        this.billingMonth = billingMonth;
    }

    @java.lang.SuppressWarnings("all")
    public void setDueDate(final LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    @java.lang.SuppressWarnings("all")
    public void setBillingCycle(final String billingCycle) {
        this.billingCycle = billingCycle;
    }

    @java.lang.SuppressWarnings("all")
    public void setPeriodStart(final LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    @java.lang.SuppressWarnings("all")
    public void setPeriodEnd(final LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    @java.lang.SuppressWarnings("all")
    public void setPlanCode(final String planCode) {
        this.planCode = planCode;
    }

    @java.lang.SuppressWarnings("all")
    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    @java.lang.SuppressWarnings("all")
    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    @java.lang.SuppressWarnings("all")
    public void setProvider(final String provider) {
        this.provider = provider;
    }

    @java.lang.SuppressWarnings("all")
    public void setProviderReference(final String providerReference) {
        this.providerReference = providerReference;
    }

    @java.lang.SuppressWarnings("all")
    public void setStatus(final String status) {
        this.status = status;
    }

    @java.lang.SuppressWarnings("all")
    public void setCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setPaidAt(final OffsetDateTime paidAt) {
        this.paidAt = paidAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setPaymentMethod(final String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @java.lang.SuppressWarnings("all")
    public void setPaymentNote(final String paymentNote) {
        this.paymentNote = paymentNote;
    }

    @java.lang.SuppressWarnings("all")
    public void setSubmittedAt(final OffsetDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setReviewedAt(final OffsetDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setReviewedBy(final UUID reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setRejectionReason(final String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    @java.lang.SuppressWarnings("all")
    public void setPricingBreakdown(final String pricingBreakdown) {
        this.pricingBreakdown = pricingBreakdown;
    }

    @java.lang.SuppressWarnings("all")
    public SubscriptionOrder() {}
}
