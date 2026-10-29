package com.societyapp.domain.type;

public enum ActivityAudience implements DomainCode {
    ALL_MEMBERS,
    ADULTS,
    CHILDREN,
    SENIOR_CITIZENS,
    WOMEN,
    MEN,
    OTHER;

    public static ActivityAudience from(String value) {
        return DomainCode.parse(ActivityAudience.class, value, "activity audience");
    }
}
