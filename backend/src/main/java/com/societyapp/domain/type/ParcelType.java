package com.societyapp.domain.type;

public enum ParcelType implements DomainCode {
    DOCUMENT,
    PACKAGE,
    FOOD,
    GROCERY,
    MEDICINE,
    OTHER;

    public static ParcelType from(String value) {
        return DomainCode.parse(ParcelType.class, value, "parcel type");
    }
}
