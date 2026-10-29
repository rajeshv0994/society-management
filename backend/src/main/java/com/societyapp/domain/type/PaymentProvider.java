package com.societyapp.domain.type;

public enum PaymentProvider implements DomainCode {
    MANUAL,
    RAZORPAY,
    BANK_TRANSFER,
    UPI,
    CARD,
    CHEQUE;

    public static PaymentProvider from(String value) {
        return DomainCode.parse(PaymentProvider.class, value, "payment provider");
    }
}
