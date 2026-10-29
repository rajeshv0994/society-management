package com.societyapp.domain.type;

public enum VisitorApprovalMethod implements DomainCode {
    RESIDENT_APP,
    RESIDENT_CANCELLED,
    PHONE_VERIFIED,
    STAFF_SELF;

    public static VisitorApprovalMethod from(String value) {
        return DomainCode.parse(VisitorApprovalMethod.class, value, "visitor approval method");
    }
}
