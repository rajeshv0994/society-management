package com.societyapp.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ParkingSlot extends BaseUuidEntity {
    private UUID societyId;
    private String slotNumber;
    private String slotType;
    private String status;
    private UUID flatId;
    private String vehicleNumber;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public String getSlotNumber() {
        return this.slotNumber;
    }

    @java.lang.SuppressWarnings("all")
    public String getSlotType() {
        return this.slotType;
    }

    @java.lang.SuppressWarnings("all")
    public String getStatus() {
        return this.status;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getFlatId() {
        return this.flatId;
    }

    @java.lang.SuppressWarnings("all")
    public String getVehicleNumber() {
        return this.vehicleNumber;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setSocietyId(final UUID societyId) {
        this.societyId = societyId;
    }

    @java.lang.SuppressWarnings("all")
    public void setSlotNumber(final String slotNumber) {
        this.slotNumber = slotNumber;
    }

    @java.lang.SuppressWarnings("all")
    public void setSlotType(final String slotType) {
        this.slotType = slotType;
    }

    @java.lang.SuppressWarnings("all")
    public void setStatus(final String status) {
        this.status = status;
    }

    @java.lang.SuppressWarnings("all")
    public void setFlatId(final UUID flatId) {
        this.flatId = flatId;
    }

    @java.lang.SuppressWarnings("all")
    public void setVehicleNumber(final String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    @java.lang.SuppressWarnings("all")
    public void setCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setUpdatedAt(final OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @java.lang.SuppressWarnings("all")
    public ParkingSlot() {}
}
