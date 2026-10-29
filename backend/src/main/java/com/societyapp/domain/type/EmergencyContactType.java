package com.societyapp.domain.type;

public enum EmergencyContactType implements DomainCode {
    AMBULANCE,
    HOSPITAL,
    POLICE,
    FIRE,
    SOCIETY_SECURITY,
    DISASTER_CONTROL,
    OTHER;

    public static EmergencyContactType from(String value) {
        return DomainCode.parse(EmergencyContactType.class, value, "emergency contact type");
    }
}
