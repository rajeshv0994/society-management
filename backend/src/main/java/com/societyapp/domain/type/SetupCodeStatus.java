package com.societyapp.domain.type;

public enum SetupCodeStatus implements DomainCode {
    ACTIVE,
    USED;

    public static SetupCodeStatus from(String value) {
        return DomainCode.parse(SetupCodeStatus.class, value, "setup code status");
    }
}
