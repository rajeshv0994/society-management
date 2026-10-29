package com.societyapp.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class PlatformPaymentSetting extends BaseUuidEntity {
    private String beneficiaryName;
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String upiId;
    private String paymentInstructions;
    private UUID updatedBy;
    private OffsetDateTime updatedAt;

    @java.lang.SuppressWarnings("all")
    public String getBeneficiaryName() {
        return this.beneficiaryName;
    }

    @java.lang.SuppressWarnings("all")
    public String getBankName() {
        return this.bankName;
    }

    @java.lang.SuppressWarnings("all")
    public String getAccountNumber() {
        return this.accountNumber;
    }

    @java.lang.SuppressWarnings("all")
    public String getIfscCode() {
        return this.ifscCode;
    }

    @java.lang.SuppressWarnings("all")
    public String getUpiId() {
        return this.upiId;
    }

    @java.lang.SuppressWarnings("all")
    public String getPaymentInstructions() {
        return this.paymentInstructions;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getUpdatedBy() {
        return this.updatedBy;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setBeneficiaryName(final String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    @java.lang.SuppressWarnings("all")
    public void setBankName(final String bankName) {
        this.bankName = bankName;
    }

    @java.lang.SuppressWarnings("all")
    public void setAccountNumber(final String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @java.lang.SuppressWarnings("all")
    public void setIfscCode(final String ifscCode) {
        this.ifscCode = ifscCode;
    }

    @java.lang.SuppressWarnings("all")
    public void setUpiId(final String upiId) {
        this.upiId = upiId;
    }

    @java.lang.SuppressWarnings("all")
    public void setPaymentInstructions(final String paymentInstructions) {
        this.paymentInstructions = paymentInstructions;
    }

    @java.lang.SuppressWarnings("all")
    public void setUpdatedBy(final UUID updatedBy) {
        this.updatedBy = updatedBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setUpdatedAt(final OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @java.lang.SuppressWarnings("all")
    public PlatformPaymentSetting() {}
}
