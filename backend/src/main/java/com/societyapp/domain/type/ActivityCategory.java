package com.societyapp.domain.type;

public enum ActivityCategory implements DomainCode {
    SPORTS,
    CULTURAL,
    EDUCATIONAL,
    HEALTH,
    SOCIAL,
    RELIGIOUS,
    CHILDREN,
    SENIOR_CITIZEN,
    VOLUNTEERING,
    OTHER;

    public static ActivityCategory from(String value) {
        return DomainCode.parse(ActivityCategory.class, value, "activity category");
    }
}
