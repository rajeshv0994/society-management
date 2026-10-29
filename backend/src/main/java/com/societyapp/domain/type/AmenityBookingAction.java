package com.societyapp.domain.type;

public enum AmenityBookingAction implements DomainCode {
    CANCEL,
    APPROVE,
    REJECT,
    CHECK_IN,
    CHECK_OUT,
    REFUND;

    public static AmenityBookingAction from(String value) {
        return DomainCode.parse(AmenityBookingAction.class, value, "booking action");
    }
}
