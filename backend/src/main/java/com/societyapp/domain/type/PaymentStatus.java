package com.societyapp.domain.type;

public enum PaymentStatus implements DomainCode {
    FREE,
    NOT_REQUIRED,
    PENDING,
    PENDING_PAYMENT,
    PAYMENT_PENDING,
    DUE,
    SUBMITTED,
    AUTHORIZED,
    CAPTURED,
    PART_PAID,
    OVERDUE,
    PAID,
    SUCCESS,
    FAILED,
    CANCELLED,
    REJECTED,
    REFUND_PENDING,
    REFUNDED,
    WAIVED;

    public static PaymentStatus from(String value) {
        return DomainCode.parse(PaymentStatus.class, value, "payment status");
    }
}
