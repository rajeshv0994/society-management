package com.societyapp.domain.type;

public enum EmergencyIncidentStatus implements DomainCode {
    REPORTED,
    ACKNOWLEDGED,
    RESPONDING,
    ON_SCENE,
    STABILIZED,
    RESOLVED,
    CLOSED,
    CANCELLED,
    FALSE_ALARM;

    public boolean isOpen() {
        return switch (this) {
            case REPORTED, ACKNOWLEDGED, RESPONDING, ON_SCENE, STABILIZED -> true;
            case RESOLVED, CLOSED, CANCELLED, FALSE_ALARM -> false;
        };
    }

    public static EmergencyIncidentStatus from(String value) {
        return DomainCode.parse(EmergencyIncidentStatus.class, value, "emergency incident status");
    }
}
