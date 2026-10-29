package com.societyapp.domain.type;

public enum BuiltInServiceCode implements DomainCode {
    RESIDENTS,
    STAFF,
    VISITORS,
    COMPLAINTS,
    BILLING,
    PAYMENTS,
    AMENITIES,
    NOTICES,
    DOCUMENTS,
    PARKING,
    VENDORS,
    DELIVERIES,
    EMERGENCY_INCIDENTS,
    ACTIVITIES_GALLERIES;

    public static BuiltInServiceCode from(String value) {
        return DomainCode.parse(BuiltInServiceCode.class, value, "built-in service");
    }
}
