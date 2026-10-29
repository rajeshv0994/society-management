package com.societyapp.domain.type;

public enum BillingCycle implements DomainCode {
    MONTHLY,
    QUARTERLY,
    YEARLY;

    public static BillingCycle from(String value) {
        return DomainCode.parse(BillingCycle.class, value, "billing cycle");
    }
}
