package com.societyapp.domain.type;

public enum NotificationStatus implements DomainCode {
    PENDING,
    SENT,
    FAILED;

    public static NotificationStatus from(String value) {
        return DomainCode.parse(NotificationStatus.class, value, "notification status");
    }
}
