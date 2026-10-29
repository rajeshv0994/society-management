package com.societyapp.domain.type;

public enum ApprovalMode implements DomainCode {
    AUTO,
    MANUAL;

    public static ApprovalMode from(String value) {
        return DomainCode.parse(ApprovalMode.class, value, "approval mode");
    }
}
