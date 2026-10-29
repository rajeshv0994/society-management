package com.societyapp.domain.type;

public enum ChargeFrequency implements DomainCode {
    MONTHLY,
    QUARTERLY,
    YEARLY,
    ONE_TIME;

    public static ChargeFrequency from(String value) {
        return DomainCode.parse(ChargeFrequency.class, value, "charge frequency");
    }
}
