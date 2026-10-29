package com.societyapp.domain.type;

public enum PlatformServiceStatus implements DomainCode {
    ACTIVE,
    COMING_SOON,
    RETIRED;

    public static PlatformServiceStatus from(String value) {
        return DomainCode.parse(PlatformServiceStatus.class, value, "platform service status");
    }
}
