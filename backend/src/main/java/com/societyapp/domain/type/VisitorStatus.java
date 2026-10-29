package com.societyapp.domain.type;

public enum VisitorStatus implements DomainCode {
    EXPECTED,
    PENDING_RESIDENT_APPROVAL,
    PENDING_STAFF_APPROVAL,
    APPROVED,
    DENIED,
    CANCELLED,
    CHECKED_IN,
    CHECKED_OUT;

    public static VisitorStatus from(String value) {
        return DomainCode.parse(VisitorStatus.class, value, "visitor status");
    }
}
