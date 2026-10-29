package com.societyapp.domain.type;

public enum SubscriptionStatus implements DomainCode {
    TRIAL,
    ACTIVE,
    PENDING,
    PENDING_PAYMENT,
    PAYMENT_PENDING,
    DUE,
    SUSPENDED,
    EXPIRED,
    CANCELLED,
    REJECTED,
    CLOSED;

    public static SubscriptionStatus from(String value) {
        return DomainCode.parse(SubscriptionStatus.class, value, "subscription status");
    }
}
