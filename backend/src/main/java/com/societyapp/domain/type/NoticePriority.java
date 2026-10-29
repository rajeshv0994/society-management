package com.societyapp.domain.type;

public enum NoticePriority implements DomainCode {
    NORMAL,
    IMPORTANT,
    URGENT;

    public static NoticePriority from(String value) {
        return DomainCode.parse(NoticePriority.class, value, "notice priority");
    }
}
