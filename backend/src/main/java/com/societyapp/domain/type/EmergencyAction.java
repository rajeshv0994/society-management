package com.societyapp.domain.type;

public enum EmergencyAction implements DomainCode {
    ACKNOWLEDGE,
    ASSIGN,
    START_RESPONSE,
    ARRIVE,
    REQUEST_AMBULANCE,
    REQUEST_POLICE,
    REQUEST_FIRE,
    NOTIFY_CONTACT,
    ADD_UPDATE,
    ESCALATE,
    STABILIZE,
    RESOLVE,
    CLOSE,
    CANCEL,
    FALSE_ALARM,
    REOPEN;

    public static EmergencyAction from(String value) {
        return DomainCode.parse(EmergencyAction.class, value, "emergency action");
    }
}
