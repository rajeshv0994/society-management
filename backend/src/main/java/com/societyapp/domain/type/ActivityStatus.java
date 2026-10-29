package com.societyapp.domain.type;

public enum ActivityStatus implements DomainCode {
    DRAFT,
    PUBLISHED,
    CANCELLED,
    COMPLETED,
    ARCHIVED;

    public static ActivityStatus from(String value) {
        return DomainCode.parse(ActivityStatus.class, value, "activity status");
    }
}
