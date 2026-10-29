package com.societyapp.domain.model;

import java.util.UUID;

public class Floor extends BaseUuidEntity {
    private UUID blockId;
    private String name;
    private int floorNumber;

    @java.lang.SuppressWarnings("all")
    public UUID getBlockId() {
        return this.blockId;
    }

    @java.lang.SuppressWarnings("all")
    public String getName() {
        return this.name;
    }

    @java.lang.SuppressWarnings("all")
    public int getFloorNumber() {
        return this.floorNumber;
    }

    @java.lang.SuppressWarnings("all")
    public void setBlockId(final UUID blockId) {
        this.blockId = blockId;
    }

    @java.lang.SuppressWarnings("all")
    public void setName(final String name) {
        this.name = name;
    }

    @java.lang.SuppressWarnings("all")
    public void setFloorNumber(final int floorNumber) {
        this.floorNumber = floorNumber;
    }

    @java.lang.SuppressWarnings("all")
    public Floor() {}
}
