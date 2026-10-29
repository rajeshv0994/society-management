package com.societyapp.domain.model;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public class Amenity extends BaseUuidEntity {
    private UUID societyId;
    private String name;
    private String amenityType;
    private String amenityCode;
    private String description;
    private Integer capacity;
    private String location;
    private BigDecimal hourlyCharge;
    private String pricingType;
    private BigDecimal bookingCharge;
    private BigDecimal refundableDeposit;
    private LocalTime opensAt;
    private LocalTime closesAt;
    private boolean bookingApprovalRequired;
    private int slotMinutes;
    private int advanceBookingDays;
    private int cancellationHours;
    private String instructions;
    private boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public String getName() {
        return this.name;
    }

    @java.lang.SuppressWarnings("all")
    public String getAmenityType() {
        return this.amenityType;
    }

    @java.lang.SuppressWarnings("all")
    public String getAmenityCode() {
        return this.amenityCode;
    }

    @java.lang.SuppressWarnings("all")
    public String getDescription() {
        return this.description;
    }

    @java.lang.SuppressWarnings("all")
    public Integer getCapacity() {
        return this.capacity;
    }

    @java.lang.SuppressWarnings("all")
    public String getLocation() {
        return this.location;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getHourlyCharge() {
        return this.hourlyCharge;
    }

    @java.lang.SuppressWarnings("all")
    public String getPricingType() {
        return this.pricingType;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getBookingCharge() {
        return this.bookingCharge;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getRefundableDeposit() {
        return this.refundableDeposit;
    }

    @java.lang.SuppressWarnings("all")
    public LocalTime getOpensAt() {
        return this.opensAt;
    }

    @java.lang.SuppressWarnings("all")
    public LocalTime getClosesAt() {
        return this.closesAt;
    }

    @java.lang.SuppressWarnings("all")
    public boolean isBookingApprovalRequired() {
        return this.bookingApprovalRequired;
    }

    @java.lang.SuppressWarnings("all")
    public int getSlotMinutes() {
        return this.slotMinutes;
    }

    @java.lang.SuppressWarnings("all")
    public int getAdvanceBookingDays() {
        return this.advanceBookingDays;
    }

    @java.lang.SuppressWarnings("all")
    public int getCancellationHours() {
        return this.cancellationHours;
    }

    @java.lang.SuppressWarnings("all")
    public String getInstructions() {
        return this.instructions;
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
    public void setName(final String name) {
        this.name = name;
    }

    @java.lang.SuppressWarnings("all")
    public void setAmenityType(final String amenityType) {
        this.amenityType = amenityType;
    }

    @java.lang.SuppressWarnings("all")
    public void setAmenityCode(final String amenityCode) {
        this.amenityCode = amenityCode;
    }

    @java.lang.SuppressWarnings("all")
    public void setDescription(final String description) {
        this.description = description;
    }

    @java.lang.SuppressWarnings("all")
    public void setCapacity(final Integer capacity) {
        this.capacity = capacity;
    }

    @java.lang.SuppressWarnings("all")
    public void setLocation(final String location) {
        this.location = location;
    }

    @java.lang.SuppressWarnings("all")
    public void setHourlyCharge(final BigDecimal hourlyCharge) {
        this.hourlyCharge = hourlyCharge;
    }

    @java.lang.SuppressWarnings("all")
    public void setPricingType(final String pricingType) {
        this.pricingType = pricingType;
    }

    @java.lang.SuppressWarnings("all")
    public void setBookingCharge(final BigDecimal bookingCharge) {
        this.bookingCharge = bookingCharge;
    }

    @java.lang.SuppressWarnings("all")
    public void setRefundableDeposit(final BigDecimal refundableDeposit) {
        this.refundableDeposit = refundableDeposit;
    }

    @java.lang.SuppressWarnings("all")
    public void setOpensAt(final LocalTime opensAt) {
        this.opensAt = opensAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setClosesAt(final LocalTime closesAt) {
        this.closesAt = closesAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setBookingApprovalRequired(final boolean bookingApprovalRequired) {
        this.bookingApprovalRequired = bookingApprovalRequired;
    }

    @java.lang.SuppressWarnings("all")
    public void setSlotMinutes(final int slotMinutes) {
        this.slotMinutes = slotMinutes;
    }

    @java.lang.SuppressWarnings("all")
    public void setAdvanceBookingDays(final int advanceBookingDays) {
        this.advanceBookingDays = advanceBookingDays;
    }

    @java.lang.SuppressWarnings("all")
    public void setCancellationHours(final int cancellationHours) {
        this.cancellationHours = cancellationHours;
    }

    @java.lang.SuppressWarnings("all")
    public void setInstructions(final String instructions) {
        this.instructions = instructions;
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
    public Amenity() {}
}
