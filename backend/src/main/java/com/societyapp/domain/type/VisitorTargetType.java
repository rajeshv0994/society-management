package com.societyapp.domain.type;

public enum VisitorTargetType implements DomainCode {
    RESIDENT,
    STAFF,
    OTHER;

    public UserRole requiredRole() {
        return switch (this) {
            case RESIDENT -> UserRole.RESIDENT;
            case STAFF -> UserRole.STAFF;
            case OTHER -> null;
        };
    }

    public static VisitorTargetType from(String value) {
        return DomainCode.parse(VisitorTargetType.class, value, "visitor target type");
    }
}
