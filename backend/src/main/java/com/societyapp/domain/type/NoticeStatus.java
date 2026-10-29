package com.societyapp.domain.type;

public enum NoticeStatus implements DomainCode {
    DRAFT,
    PUBLISHED,
    ARCHIVED;

    public static NoticeStatus from(String value) {
        return DomainCode.parse(NoticeStatus.class, value, "notice status");
    }
}
