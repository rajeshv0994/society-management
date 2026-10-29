package com.societyapp.domain.type;

public enum ApprovalAction implements DomainCode {
    APPROVE,
    REJECT,
    CANCEL,
    COMPLETE,
    PUBLISH,
    ARCHIVE,
    REFUND,
    CHECK_IN,
    CHECK_OUT;

    public static ApprovalAction from(String value) {
        return DomainCode.parse(ApprovalAction.class, value, "action");
    }
}
