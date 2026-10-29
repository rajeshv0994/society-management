package com.societyapp.domain.type;

public enum ChargeCalculationType implements DomainCode {
    FIXED,
    PER_SQFT,
    PERCENTAGE;

    public static ChargeCalculationType from(String value) {
        return DomainCode.parse(ChargeCalculationType.class, value, "charge calculation type");
    }
}
