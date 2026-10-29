package com.societyapp.domain.type;

public enum StorageStatus implements DomainCode {
    ACTIVE,
    DELETED,
    CANCELLED;

    public static StorageStatus from(String value) {
        return DomainCode.parse(StorageStatus.class, value, "storage status");
    }
}
