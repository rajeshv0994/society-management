package com.societyapp.domain.type;

public enum AttendanceStatus implements DomainCode {
    PRESENT,
    ABSENT,
    HALF_DAY,
    LEAVE;

    public static AttendanceStatus from(String value) {
        return DomainCode.parse(AttendanceStatus.class, value, "attendance status");
    }
}
