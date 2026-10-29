package com.societyapp.domain.type;

public enum ResidentType implements DomainCode {
    OWNER,
    TENANT;

    public static ResidentType from(String value) {
        return DomainCode.parse(ResidentType.class, value, "resident type");
    }
}
