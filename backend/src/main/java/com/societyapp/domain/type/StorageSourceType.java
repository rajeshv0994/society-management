package com.societyapp.domain.type;

public enum StorageSourceType implements DomainCode {
    UPLOAD,
    SOCIETY_DOCUMENT,
    VISITOR_PHOTO,
    ACTIVITY_PHOTO,
    SOCIETY_LOGO;

    public static StorageSourceType from(String value) {
        return DomainCode.parse(StorageSourceType.class, value, "storage source type");
    }
}
