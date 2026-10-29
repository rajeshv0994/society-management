package com.societyapp.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class Payment extends BaseUuidEntity {
    private UUID societyId;
    private UUID invoiceId;
    private UUID paidBy;
    private BigDecimal amount;
    private String paymentMethod;
    private String providerReference;
    private String receiptNumber;
    private String paymentChannel;
    private UUID recordedBy;
    private String note;
    private String status;
    private OffsetDateTime paidAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getInvoiceId() {
        return this.invoiceId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getPaidBy() {
        return this.paidBy;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getAmount() {
        return this.amount;
    }

    @java.lang.SuppressWarnings("all")
    public String getPaymentMethod() {
        return this.paymentMethod;
    }

    @java.lang.SuppressWarnings("all")
    public String getProviderReference() {
        return this.providerReference;
    }

    @java.lang.SuppressWarnings("all")
    public String getReceiptNumber() {
        return this.receiptNumber;
    }

    @java.lang.SuppressWarnings("all")
    public String getPaymentChannel() {
        return this.paymentChannel;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getRecordedBy() {
        return this.recordedBy;
    }

    @java.lang.SuppressWarnings("all")
    public String getNote() {
        return this.note;
    }

    @java.lang.SuppressWarnings("all")
    public String getStatus() {
        return this.status;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getPaidAt() {
        return this.paidAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setSocietyId(final UUID societyId) {
        this.societyId = societyId;
    }

    @java.lang.SuppressWarnings("all")
    public void setInvoiceId(final UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    @java.lang.SuppressWarnings("all")
    public void setPaidBy(final UUID paidBy) {
        this.paidBy = paidBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    @java.lang.SuppressWarnings("all")
    public void setPaymentMethod(final String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @java.lang.SuppressWarnings("all")
    public void setProviderReference(final String providerReference) {
        this.providerReference = providerReference;
    }

    @java.lang.SuppressWarnings("all")
    public void setReceiptNumber(final String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    @java.lang.SuppressWarnings("all")
    public void setPaymentChannel(final String paymentChannel) {
        this.paymentChannel = paymentChannel;
    }

    @java.lang.SuppressWarnings("all")
    public void setRecordedBy(final UUID recordedBy) {
        this.recordedBy = recordedBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setNote(final String note) {
        this.note = note;
    }

    @java.lang.SuppressWarnings("all")
    public void setStatus(final String status) {
        this.status = status;
    }

    @java.lang.SuppressWarnings("all")
    public void setPaidAt(final OffsetDateTime paidAt) {
        this.paidAt = paidAt;
    }

    @java.lang.SuppressWarnings("all")
    public Payment() {}
}
