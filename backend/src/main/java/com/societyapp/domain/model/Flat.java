package com.societyapp.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Flat extends BaseUuidEntity {
    private UUID floorId;
    private String flatNumber;
    private String occupancyStatus;
    private UUID activeOwnerId;
    private UUID activeTenantId;
    private BigDecimal areaSqft;

    @java.lang.SuppressWarnings("all")
    public UUID getFloorId() {
        return this.floorId;
    }

    @java.lang.SuppressWarnings("all")
    public String getFlatNumber() {
        return this.flatNumber;
    }

    @java.lang.SuppressWarnings("all")
    public String getOccupancyStatus() {
        return this.occupancyStatus;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getActiveOwnerId() {
        return this.activeOwnerId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getActiveTenantId() {
        return this.activeTenantId;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getAreaSqft() {
        return this.areaSqft;
    }

    @java.lang.SuppressWarnings("all")
    public void setFloorId(final UUID floorId) {
        this.floorId = floorId;
    }

    @java.lang.SuppressWarnings("all")
    public void setFlatNumber(final String flatNumber) {
        this.flatNumber = flatNumber;
    }

    @java.lang.SuppressWarnings("all")
    public void setOccupancyStatus(final String occupancyStatus) {
        this.occupancyStatus = occupancyStatus;
    }

    @java.lang.SuppressWarnings("all")
    public void setActiveOwnerId(final UUID activeOwnerId) {
        this.activeOwnerId = activeOwnerId;
    }

    @java.lang.SuppressWarnings("all")
    public void setActiveTenantId(final UUID activeTenantId) {
        this.activeTenantId = activeTenantId;
    }

    @java.lang.SuppressWarnings("all")
    public void setAreaSqft(final BigDecimal areaSqft) {
        this.areaSqft = areaSqft;
    }

    @java.lang.SuppressWarnings("all")
    public Flat() {}
}
