package com.societyapp.domain.type;

public enum SocietyStatus implements DomainCode {
    PENDING_VERIFICATION,
    APPROVED,
    REJECTED,
    ACTIVE,
    INACTIVE,
    SUSPENDED;

    public static SocietyStatus from(String value) {
        return DomainCode.parse(SocietyStatus.class, value, "society status");
    }
}
