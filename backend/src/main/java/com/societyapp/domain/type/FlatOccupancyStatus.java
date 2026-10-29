package com.societyapp.domain.type;

public enum FlatOccupancyStatus implements DomainCode {
    VACANT,
    OWNER_OCCUPIED,
    TENANT_OCCUPIED;

    public static FlatOccupancyStatus from(String value) {
        return DomainCode.parse(FlatOccupancyStatus.class, value, "occupancy status");
    }
}
