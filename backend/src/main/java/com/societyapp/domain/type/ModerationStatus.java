package com.societyapp.domain.type;

public enum ModerationStatus implements DomainCode {
    PENDING,
    PUBLISHED,
    REJECTED,
    REMOVED;

    public static ModerationStatus from(String value) {
        return DomainCode.parse(ModerationStatus.class, value, "moderation status");
    }
}
