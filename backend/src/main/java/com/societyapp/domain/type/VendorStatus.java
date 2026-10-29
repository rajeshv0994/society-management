package com.societyapp.domain.type;

public enum VendorStatus implements DomainCode {
    ACTIVE,
    INACTIVE;

    public static VendorStatus from(String value) {
        return DomainCode.parse(VendorStatus.class, value, "vendor status");
    }
}
