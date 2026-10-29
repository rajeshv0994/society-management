package com.societyapp.domain.type;

public enum NotificationChannel implements DomainCode {
    EMAIL,
    SMS,
    WHATSAPP,
    PUSH;

    public static NotificationChannel from(String value) {
        return DomainCode.parse(NotificationChannel.class, value, "notification channel");
    }
}
