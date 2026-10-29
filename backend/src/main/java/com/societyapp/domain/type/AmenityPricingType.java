package com.societyapp.domain.type;

public enum AmenityPricingType implements DomainCode {
    FREE,
    HOURLY,
    PER_SLOT,
    FIXED;

    public static AmenityPricingType from(String value) {
        return DomainCode.parse(AmenityPricingType.class, value, "amenity pricing type");
    }
}
