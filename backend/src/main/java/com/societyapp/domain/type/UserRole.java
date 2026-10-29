package com.societyapp.domain.type;

public enum UserRole implements DomainCode {
    SUPER_ADMIN,
    SOCIETY_ADMIN,
    STAFF,
    RESIDENT;

    public static UserRole from(String value) {
        return DomainCode.parse(UserRole.class, value, "user role");
    }
}
