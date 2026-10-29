package com.societyapp.domain.type;

public enum NoticeAudience implements DomainCode {
    ALL,
    RESIDENTS,
    STAFF;

    public static NoticeAudience from(String value) {
        return DomainCode.parse(NoticeAudience.class, value, "notice audience");
    }
}
