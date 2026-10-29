package com.societyapp.domain.type;

public enum VehicleType implements DomainCode {
    CAR,
    MOTORCYCLE,
    SCOOTER,
    BICYCLE,
    OTHER;

    public static VehicleType from(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Vehicle type is required");
        return DomainCode.parse(VehicleType.class, value, "vehicle type");
    }
}
