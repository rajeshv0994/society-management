package com.societyapp.domain.type;

public enum ParkingSlotStatus implements DomainCode {
    AVAILABLE,
    ALLOCATED,
    RESERVED,
    MAINTENANCE,
    INACTIVE;

    public static ParkingSlotStatus from(String value) {
        return DomainCode.parse(ParkingSlotStatus.class, value, "parking slot status");
    }
}
