package com.societyapp.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class MaintenanceInvoice extends BaseUuidEntity {
    private UUID societyId;
    private UUID flatId;
    private String invoiceNumber;
    private LocalDate billingMonth;
    private LocalDate dueDate;
    private BigDecimal subtotal;
    private BigDecimal membershipFee;
    private BigDecimal otherCharges;
    private String otherChargeNote;
    private BigDecimal lateFee;
    private BigDecimal paidAmount;
    private String status;
    private OffsetDateTime createdAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getFlatId() {
        return this.flatId;
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
    public BigDecimal getSubtotal() {
        return this.subtotal;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getMembershipFee() {
        return this.membershipFee;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getOtherCharges() {
        return this.otherCharges;
    }

    @java.lang.SuppressWarnings("all")
    public String getOtherChargeNote() {
        return this.otherChargeNote;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getLateFee() {
        return this.lateFee;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getPaidAmount() {
        return this.paidAmount;
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
    public void setSocietyId(final UUID societyId) {
        this.societyId = societyId;
    }

    @java.lang.SuppressWarnings("all")
    public void setFlatId(final UUID flatId) {
        this.flatId = flatId;
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
    public void setSubtotal(final BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @java.lang.SuppressWarnings("all")
    public void setMembershipFee(final BigDecimal membershipFee) {
        this.membershipFee = membershipFee;
    }

    @java.lang.SuppressWarnings("all")
    public void setOtherCharges(final BigDecimal otherCharges) {
        this.otherCharges = otherCharges;
    }

    @java.lang.SuppressWarnings("all")
    public void setOtherChargeNote(final String otherChargeNote) {
        this.otherChargeNote = otherChargeNote;
    }

    @java.lang.SuppressWarnings("all")
    public void setLateFee(final BigDecimal lateFee) {
        this.lateFee = lateFee;
    }

    @java.lang.SuppressWarnings("all")
    public void setPaidAmount(final BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
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
    public MaintenanceInvoice() {}
}
