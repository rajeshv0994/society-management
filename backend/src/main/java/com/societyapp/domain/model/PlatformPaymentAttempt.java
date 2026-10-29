package com.societyapp.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class PlatformPaymentAttempt extends BaseUuidEntity {
    private UUID subscriptionOrderId;
    private String providerOrderId;
    private String providerPaymentId;
    private BigDecimal amount;
    private String currency;
    private String requestedMethod;
    private String status;
    private String failureCode;
    private String failureDescription;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSubscriptionOrderId() {
        return this.subscriptionOrderId;
    }

    @java.lang.SuppressWarnings("all")
    public String getProviderOrderId() {
        return this.providerOrderId;
    }

    @java.lang.SuppressWarnings("all")
    public String getProviderPaymentId() {
        return this.providerPaymentId;
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
    public String getRequestedMethod() {
        return this.requestedMethod;
    }

    @java.lang.SuppressWarnings("all")
    public String getStatus() {
        return this.status;
    }

    @java.lang.SuppressWarnings("all")
    public String getFailureCode() {
        return this.failureCode;
    }

    @java.lang.SuppressWarnings("all")
    public String getFailureDescription() {
        return this.failureDescription;
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
    public void setSubscriptionOrderId(final UUID subscriptionOrderId) {
        this.subscriptionOrderId = subscriptionOrderId;
    }

    @java.lang.SuppressWarnings("all")
    public void setProviderOrderId(final String providerOrderId) {
        this.providerOrderId = providerOrderId;
    }

    @java.lang.SuppressWarnings("all")
    public void setProviderPaymentId(final String providerPaymentId) {
        this.providerPaymentId = providerPaymentId;
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
    public void setRequestedMethod(final String requestedMethod) {
        this.requestedMethod = requestedMethod;
    }

    @java.lang.SuppressWarnings("all")
    public void setStatus(final String status) {
        this.status = status;
    }

    @java.lang.SuppressWarnings("all")
    public void setFailureCode(final String failureCode) {
        this.failureCode = failureCode;
    }

    @java.lang.SuppressWarnings("all")
    public void setFailureDescription(final String failureDescription) {
        this.failureDescription = failureDescription;
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
    public PlatformPaymentAttempt() {}
}
