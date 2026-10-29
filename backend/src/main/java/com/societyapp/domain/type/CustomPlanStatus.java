package com.societyapp.domain.type;

public enum CustomPlanStatus implements DomainCode {
    DRAFT,
    ACTIVE,
    REPLACED,
    CANCELLED;

    public static CustomPlanStatus from(String value) {
        return DomainCode.parse(CustomPlanStatus.class, value, "custom plan status");
    }
}
