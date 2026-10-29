package com.societyapp.domain.type;

public enum RegistrationStatus implements DomainCode {
    PENDING_APPROVAL,
    REJECTED,
    ACTIVE;

    public static RegistrationStatus from(String value) {
        return DomainCode.parse(RegistrationStatus.class, value, "registration status");
    }
}
