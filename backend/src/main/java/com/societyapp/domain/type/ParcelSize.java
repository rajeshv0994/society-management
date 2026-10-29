package com.societyapp.domain.type;

public enum ParcelSize implements DomainCode {
    SMALL,
    MEDIUM,
    LARGE,
    OVERSIZED;

    public static ParcelSize from(String value) {
        return DomainCode.parse(ParcelSize.class, value, "parcel size");
    }
}
