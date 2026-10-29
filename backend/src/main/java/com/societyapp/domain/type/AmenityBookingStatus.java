package com.societyapp.domain.type;

public enum AmenityBookingStatus implements DomainCode {
    PENDING_PAYMENT,
    PENDING_APPROVAL,
    CONFIRMED,
    CHECKED_IN,
    COMPLETED,
    CANCELLED,
    REJECTED;

    public static AmenityBookingStatus from(String value) {
        return DomainCode.parse(AmenityBookingStatus.class, value, "amenity booking status");
    }
}
