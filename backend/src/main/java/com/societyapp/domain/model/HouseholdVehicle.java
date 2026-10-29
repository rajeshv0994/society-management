package com.societyapp.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class HouseholdVehicle extends BaseUuidEntity {
    private UUID societyId;
    private UUID primaryResidentId;
    private String vehicleType;
    private String registrationNumber;
    private String make;
    private String model;
    private String color;
    private String parkingSlot;
    private String stickerNumber;
    private String verificationStatus;
    private String verificationNote;
    private UUID reviewedBy;
    private OffsetDateTime reviewedAt;
    private boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getPrimaryResidentId() {
        return this.primaryResidentId;
    }

    @java.lang.SuppressWarnings("all")
    public String getVehicleType() {
        return this.vehicleType;
    }

    @java.lang.SuppressWarnings("all")
    public String getRegistrationNumber() {
        return this.registrationNumber;
    }

    @java.lang.SuppressWarnings("all")
    public String getMake() {
        return this.make;
    }

    @java.lang.SuppressWarnings("all")
    public String getModel() {
        return this.model;
    }

    @java.lang.SuppressWarnings("all")
    public String getColor() {
        return this.color;
    }

    @java.lang.SuppressWarnings("all")
    public String getParkingSlot() {
        return this.parkingSlot;
    }

    @java.lang.SuppressWarnings("all")
    public String getStickerNumber() {
        return this.stickerNumber;
    }

    @java.lang.SuppressWarnings("all")
    public String getVerificationStatus() {
        return this.verificationStatus;
    }

    @java.lang.SuppressWarnings("all")
    public String getVerificationNote() {
        return this.verificationNote;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getReviewedBy() {
        return this.reviewedBy;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getReviewedAt() {
        return this.reviewedAt;
    }

    @java.lang.SuppressWarnings("all")
    public boolean isActive() {
        return this.active;
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
    public void setPrimaryResidentId(final UUID primaryResidentId) {
        this.primaryResidentId = primaryResidentId;
    }

    @java.lang.SuppressWarnings("all")
    public void setVehicleType(final String vehicleType) {
        this.vehicleType = vehicleType;
    }

    @java.lang.SuppressWarnings("all")
    public void setRegistrationNumber(final String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    @java.lang.SuppressWarnings("all")
    public void setMake(final String make) {
        this.make = make;
    }

    @java.lang.SuppressWarnings("all")
    public void setModel(final String model) {
        this.model = model;
    }

    @java.lang.SuppressWarnings("all")
    public void setColor(final String color) {
        this.color = color;
    }

    @java.lang.SuppressWarnings("all")
    public void setParkingSlot(final String parkingSlot) {
        this.parkingSlot = parkingSlot;
    }

    @java.lang.SuppressWarnings("all")
    public void setStickerNumber(final String stickerNumber) {
        this.stickerNumber = stickerNumber;
    }

    @java.lang.SuppressWarnings("all")
    public void setVerificationStatus(final String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    @java.lang.SuppressWarnings("all")
    public void setVerificationNote(final String verificationNote) {
        this.verificationNote = verificationNote;
    }

    @java.lang.SuppressWarnings("all")
    public void setReviewedBy(final UUID reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setReviewedAt(final OffsetDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setActive(final boolean active) {
        this.active = active;
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
    public HouseholdVehicle() {}
}
