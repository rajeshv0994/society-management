package com.societyapp.domain.type;

public enum ParkingAction implements DomainCode {
    ALLOCATE,
    RELEASE,
    ACTIVATE,
    RESERVE,
    MAINTENANCE,
    DEACTIVATE;

    public static ParkingAction from(String value) {
        return DomainCode.parse(ParkingAction.class, value, "parking action");
    }
}
