package com.societyapp.domain.type;

public enum ParticipationStatus implements DomainCode {
    PENDING_PAYMENT,
    PENDING_APPROVAL,
    CONFIRMED,
    CHECKED_IN,
    ATTENDED,
    CANCELLED,
    REJECTED,
    NO_SHOW;

    public static ParticipationStatus from(String value) {
        return DomainCode.parse(ParticipationStatus.class, value, "participation status");
    }
}
