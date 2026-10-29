package com.societyapp.domain.type;

public enum DeliveryPaymentMethod implements DomainCode {
    CASH,
    UPI,
    CARD,
    RAZORPAY_UPI,
    RAZORPAY_CARD,
    RAZORPAY_NET_BANKING;

    public static DeliveryPaymentMethod from(String value) {
        return DomainCode.parse(DeliveryPaymentMethod.class, value, "delivery payment method");
    }
}
