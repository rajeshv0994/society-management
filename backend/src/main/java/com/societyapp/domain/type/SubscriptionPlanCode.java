package com.societyapp.domain.type;

public enum SubscriptionPlanCode implements DomainCode {
    TRIAL,
    STANDARD;

    public static SubscriptionPlanCode from(String value) {
        return DomainCode.parse(SubscriptionPlanCode.class, value, "subscription plan code");
    }
}
