package com.societyapp.domain.type;

public enum PaymentAttemptStatus implements DomainCode {
    CREATED,
    AUTHORIZED,
    CAPTURED,
    FAILED;

    public static PaymentAttemptStatus from(String value) {
        return DomainCode.parse(PaymentAttemptStatus.class, value, "payment attempt status");
    }
}
