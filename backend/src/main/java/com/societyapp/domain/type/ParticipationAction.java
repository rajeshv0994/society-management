package com.societyapp.domain.type;

public enum ParticipationAction implements DomainCode {
    CANCEL,
    APPROVE,
    REJECT,
    CHECK_IN,
    ATTENDED,
    NO_SHOW,
    REFUND;

    public static ParticipationAction from(String value) {
        return DomainCode.parse(ParticipationAction.class, value, "participation action");
    }
}
