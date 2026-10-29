package com.societyapp.domain.type;

public enum ParkingSlotType implements DomainCode {
    CAR,
    BIKE,
    VISITOR,
    ACCESSIBLE;

    public static ParkingSlotType from(String value) {
        return DomainCode.parse(ParkingSlotType.class, value, "parking slot type");
    }
}
