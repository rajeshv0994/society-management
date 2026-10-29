package com.societyapp.domain.type;

public enum EmergencyIncidentType implements DomainCode {
    MEDICAL,
    ACCIDENT,
    FIRE,
    SECURITY,
    GAS_LEAK,
    ELECTRICAL,
    ELEVATOR,
    NATURAL_DISASTER,
    OTHER;

    public static EmergencyIncidentType from(String value) {
        return DomainCode.parse(EmergencyIncidentType.class, value, "emergency incident type");
    }
}
