package com.societyapp.domain.type;

public enum IdentityType implements DomainCode {
    AADHAAR,
    PASSPORT,
    DRIVING_LICENCE,
    VOTER_ID,
    OTHER;

    public static IdentityType from(String value) {
        if (value == null || value.isBlank()) return null;
        return DomainCode.parse(IdentityType.class, value, "identity type");
    }
}
