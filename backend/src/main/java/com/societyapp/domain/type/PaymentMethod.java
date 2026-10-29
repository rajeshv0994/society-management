package com.societyapp.domain.type;

public enum PaymentMethod implements DomainCode {
    CASH,
    UPI,
    CARD,
    NET_BANKING,
    BANK_TRANSFER,
    CHEQUE;

    public String razorpayCode() {
        return PaymentProvider.RAZORPAY.code() + "_" + code();
    }

    public static PaymentMethod from(String value) {
        return DomainCode.parse(PaymentMethod.class, value, "payment method");
    }
}
