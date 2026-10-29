package com.societyapp.domain.model;

import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

public class StaffSalaryPayment extends BaseUuidEntity {
    private UUID societyId;
    private UUID staffUserId;
    private LocalDate salaryMonth;
    private BigDecimal amount;
    private LocalDate paidOn;
    private String paymentMethod;
    private String referenceNumber;
    private String note;
    private UUID recordedBy;
    private OffsetDateTime createdAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getStaffUserId() {
        return this.staffUserId;
    }

    @java.lang.SuppressWarnings("all")
    public LocalDate getSalaryMonth() {
        return this.salaryMonth;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getAmount() {
        return this.amount;
    }

    @java.lang.SuppressWarnings("all")
    public LocalDate getPaidOn() {
        return this.paidOn;
    }

    @java.lang.SuppressWarnings("all")
    public String getPaymentMethod() {
        return this.paymentMethod;
    }

    @java.lang.SuppressWarnings("all")
    public String getReferenceNumber() {
        return this.referenceNumber;
    }

    @java.lang.SuppressWarnings("all")
    public String getNote() {
        return this.note;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getRecordedBy() {
        return this.recordedBy;
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
    public void setStaffUserId(final UUID staffUserId) {
        this.staffUserId = staffUserId;
    }

    @java.lang.SuppressWarnings("all")
    public void setSalaryMonth(final LocalDate salaryMonth) {
        this.salaryMonth = salaryMonth;
    }

    @java.lang.SuppressWarnings("all")
    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    @java.lang.SuppressWarnings("all")
    public void setPaidOn(final LocalDate paidOn) {
        this.paidOn = paidOn;
    }

    @java.lang.SuppressWarnings("all")
    public void setPaymentMethod(final String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @java.lang.SuppressWarnings("all")
    public void setReferenceNumber(final String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    @java.lang.SuppressWarnings("all")
    public void setNote(final String note) {
        this.note = note;
    }

    @java.lang.SuppressWarnings("all")
    public void setRecordedBy(final UUID recordedBy) {
        this.recordedBy = recordedBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public StaffSalaryPayment() {}
}
