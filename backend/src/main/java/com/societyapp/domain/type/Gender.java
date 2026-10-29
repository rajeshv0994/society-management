package com.societyapp.domain.type;

public enum Gender implements DomainCode {
    MALE,
    FEMALE,
    OTHER,
    PREFER_NOT_TO_SAY;

    public static Gender from(String value) {
        return DomainCode.parse(Gender.class, value, "gender");
    }
}
