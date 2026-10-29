package com.societyapp.domain.type;

public enum AccountStatus implements DomainCode {
    ACTIVE,
    INACTIVE,
    SUSPENDED;

    public static AccountStatus from(String value) {
        return DomainCode.parse(AccountStatus.class, value, "account status");
    }
}
