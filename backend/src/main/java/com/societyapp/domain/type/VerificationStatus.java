package com.societyapp.domain.type;

public enum VerificationStatus implements DomainCode {
    PENDING,
    VERIFIED,
    REJECTED;

    public static VerificationStatus from(String value) {
        return DomainCode.parse(VerificationStatus.class, value, "verification status");
    }
}
