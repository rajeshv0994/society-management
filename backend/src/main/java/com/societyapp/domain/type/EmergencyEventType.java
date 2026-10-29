package com.societyapp.domain.type;

public enum EmergencyEventType implements DomainCode {
    REPORTED,
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

    public static EmergencyEventType from(String value) {
        return DomainCode.parse(EmergencyEventType.class, value, "emergency event type");
    }

    public static EmergencyEventType from(EmergencyAction action) {
        return valueOf(action.name());
    }
}
