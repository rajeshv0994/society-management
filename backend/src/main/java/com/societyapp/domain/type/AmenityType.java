package com.societyapp.domain.type;

public enum AmenityType implements DomainCode {
    COMMUNITY_CENTER,
    GYM,
    SWIMMING_POOL,
    SPORTS_COURT,
    GUEST_ROOM,
    PLAY_AREA,
    OTHER;

    public static AmenityType from(String value) {
        return DomainCode.parse(AmenityType.class, value, "amenity type");
    }
}
