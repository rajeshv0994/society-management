package com.societyapp.domain.type;

public enum PaymentChannel implements DomainCode {
    ONLINE,
    OFFLINE;

    public static PaymentChannel from(String value) {
        return DomainCode.parse(PaymentChannel.class, value, "payment channel");
    }
}
