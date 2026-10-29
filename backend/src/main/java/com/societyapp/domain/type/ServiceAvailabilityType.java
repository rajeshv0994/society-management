package com.societyapp.domain.type;

public enum ServiceAvailabilityType implements DomainCode {
    DEFAULT_FREE,
    OPTIONAL_FREE,
    PAID_PER_SPACE;

    public static ServiceAvailabilityType from(String value) {
        return DomainCode.parse(ServiceAvailabilityType.class, value, "service availability type");
    }
}
