package com.societyapp.domain.type;

public enum ActivityAction implements DomainCode {
    PUBLISH,
    CANCEL,
    COMPLETE,
    ARCHIVE;

    public static ActivityAction from(String value) {
        return DomainCode.parse(ActivityAction.class, value, "activity action");
    }
}
