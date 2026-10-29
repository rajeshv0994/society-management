package com.societyapp.domain.type;

public enum DeliveryStatus implements DomainCode {
    EXPECTED,
    RECEIVED,
    READY_FOR_PICKUP,
    OUT_FOR_DELIVERY,
    COLLECTED,
    DELIVERED,
    REJECTED,
    CANCELLED,
    RETURNED,
    DAMAGED,
    LOST;

    public static DeliveryStatus from(String value) {
        return DomainCode.parse(DeliveryStatus.class, value, "delivery status");
    }
}
