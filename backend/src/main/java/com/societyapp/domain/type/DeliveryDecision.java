package com.societyapp.domain.type;

public enum DeliveryDecision implements DomainCode {
    ACCEPTED,
    REJECTED;

    public static DeliveryDecision from(String value) {
        return DomainCode.parse(DeliveryDecision.class, value, "delivery decision");
    }
}
