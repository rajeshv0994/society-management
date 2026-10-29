package com.societyapp.domain.type;

public enum WorkPriority implements DomainCode {
    LOW,
    NORMAL,
    MEDIUM,
    HIGH,
    URGENT;

    public static WorkPriority from(String value) {
        return DomainCode.parse(WorkPriority.class, value, "priority");
    }
}
