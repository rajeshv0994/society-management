package com.societyapp.domain.type;

public enum PhotoAction implements DomainCode {
    APPROVE,
    REJECT,
    REMOVE,
    FEATURE,
    UNFEATURE;

    public static PhotoAction from(String value) {
        return DomainCode.parse(PhotoAction.class, value, "photo action");
    }
}
