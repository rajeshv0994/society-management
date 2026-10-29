package com.societyapp.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class Delivery extends BaseUuidEntity {
    private UUID societyId;
    private UUID flatId;
    private UUID residentId;
    private String deliveryNumber;
    private String trackingNumber;
    private String provider;
    private String senderName;
    private String deliveryPersonName;
    private String deliveryPersonMobile;
    private String parcelType;
    private int packageCount;
    private String parcelSize;
    private boolean fragile;
    private String storageLocation;
    private OffsetDateTime expectedAt;
    private String notes;
    private String status;
    private String residentDecision;
    private OffsetDateTime residentDecisionAt;
    private String authorizedCollector;
    private String pickupCode;
    private BigDecimal codAmount;
    private String codPaymentStatus;
    private String codPaymentMethod;
    private String codPaymentReference;
    private OffsetDateTime codPaidAt;
    private UUID receivedBy;
    private OffsetDateTime receivedAt;
    private String collectedByName;
    private UUID handedOverBy;
    private OffsetDateTime collectedAt;
    private OffsetDateTime deliveredAt;
    private OffsetDateTime returnedAt;
    private String closedReason;
    private UUID createdBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getFlatId() {
        return this.flatId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getResidentId() {
        return this.residentId;
    }

    @java.lang.SuppressWarnings("all")
    public String getDeliveryNumber() {
        return this.deliveryNumber;
    }

    @java.lang.SuppressWarnings("all")
    public String getTrackingNumber() {
        return this.trackingNumber;
    }

    @java.lang.SuppressWarnings("all")
    public String getProvider() {
        return this.provider;
    }

    @java.lang.SuppressWarnings("all")
    public String getSenderName() {
        return this.senderName;
    }

    @java.lang.SuppressWarnings("all")
    public String getDeliveryPersonName() {
        return this.deliveryPersonName;
    }

    @java.lang.SuppressWarnings("all")
    public String getDeliveryPersonMobile() {
        return this.deliveryPersonMobile;
    }

    @java.lang.SuppressWarnings("all")
    public String getParcelType() {
        return this.parcelType;
    }

    @java.lang.SuppressWarnings("all")
    public int getPackageCount() {
        return this.packageCount;
    }

    @java.lang.SuppressWarnings("all")
    public String getParcelSize() {
        return this.parcelSize;
    }

    @java.lang.SuppressWarnings("all")
    public boolean isFragile() {
        return this.fragile;
    }

    @java.lang.SuppressWarnings("all")
    public String getStorageLocation() {
        return this.storageLocation;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getExpectedAt() {
        return this.expectedAt;
    }

    @java.lang.SuppressWarnings("all")
    public String getNotes() {
        return this.notes;
    }

    @java.lang.SuppressWarnings("all")
    public String getStatus() {
        return this.status;
    }

    @java.lang.SuppressWarnings("all")
    public String getResidentDecision() {
        return this.residentDecision;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getResidentDecisionAt() {
        return this.residentDecisionAt;
    }

    @java.lang.SuppressWarnings("all")
    public String getAuthorizedCollector() {
        return this.authorizedCollector;
    }

    @java.lang.SuppressWarnings("all")
    public String getPickupCode() {
        return this.pickupCode;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getCodAmount() {
        return this.codAmount;
    }

    @java.lang.SuppressWarnings("all")
    public String getCodPaymentStatus() {
        return this.codPaymentStatus;
    }

    @java.lang.SuppressWarnings("all")
    public String getCodPaymentMethod() {
        return this.codPaymentMethod;
    }

    @java.lang.SuppressWarnings("all")
    public String getCodPaymentReference() {
        return this.codPaymentReference;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCodPaidAt() {
        return this.codPaidAt;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getReceivedBy() {
        return this.receivedBy;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getReceivedAt() {
        return this.receivedAt;
    }

    @java.lang.SuppressWarnings("all")
    public String getCollectedByName() {
        return this.collectedByName;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getHandedOverBy() {
        return this.handedOverBy;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCollectedAt() {
        return this.collectedAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getDeliveredAt() {
        return this.deliveredAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getReturnedAt() {
        return this.returnedAt;
    }

    @java.lang.SuppressWarnings("all")
    public String getClosedReason() {
        return this.closedReason;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getCreatedBy() {
        return this.createdBy;
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
    public void setFlatId(final UUID flatId) {
        this.flatId = flatId;
    }

    @java.lang.SuppressWarnings("all")
    public void setResidentId(final UUID residentId) {
        this.residentId = residentId;
    }

    @java.lang.SuppressWarnings("all")
    public void setDeliveryNumber(final String deliveryNumber) {
        this.deliveryNumber = deliveryNumber;
    }

    @java.lang.SuppressWarnings("all")
    public void setTrackingNumber(final String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    @java.lang.SuppressWarnings("all")
    public void setProvider(final String provider) {
        this.provider = provider;
    }

    @java.lang.SuppressWarnings("all")
    public void setSenderName(final String senderName) {
        this.senderName = senderName;
    }

    @java.lang.SuppressWarnings("all")
    public void setDeliveryPersonName(final String deliveryPersonName) {
        this.deliveryPersonName = deliveryPersonName;
    }

    @java.lang.SuppressWarnings("all")
    public void setDeliveryPersonMobile(final String deliveryPersonMobile) {
        this.deliveryPersonMobile = deliveryPersonMobile;
    }

    @java.lang.SuppressWarnings("all")
    public void setParcelType(final String parcelType) {
        this.parcelType = parcelType;
    }

    @java.lang.SuppressWarnings("all")
    public void setPackageCount(final int packageCount) {
        this.packageCount = packageCount;
    }

    @java.lang.SuppressWarnings("all")
    public void setParcelSize(final String parcelSize) {
        this.parcelSize = parcelSize;
    }

    @java.lang.SuppressWarnings("all")
    public void setFragile(final boolean fragile) {
        this.fragile = fragile;
    }

    @java.lang.SuppressWarnings("all")
    public void setStorageLocation(final String storageLocation) {
        this.storageLocation = storageLocation;
    }

    @java.lang.SuppressWarnings("all")
    public void setExpectedAt(final OffsetDateTime expectedAt) {
        this.expectedAt = expectedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setNotes(final String notes) {
        this.notes = notes;
    }

    @java.lang.SuppressWarnings("all")
    public void setStatus(final String status) {
        this.status = status;
    }

    @java.lang.SuppressWarnings("all")
    public void setResidentDecision(final String residentDecision) {
        this.residentDecision = residentDecision;
    }

    @java.lang.SuppressWarnings("all")
    public void setResidentDecisionAt(final OffsetDateTime residentDecisionAt) {
        this.residentDecisionAt = residentDecisionAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setAuthorizedCollector(final String authorizedCollector) {
        this.authorizedCollector = authorizedCollector;
    }

    @java.lang.SuppressWarnings("all")
    public void setPickupCode(final String pickupCode) {
        this.pickupCode = pickupCode;
    }

    @java.lang.SuppressWarnings("all")
    public void setCodAmount(final BigDecimal codAmount) {
        this.codAmount = codAmount;
    }

    @java.lang.SuppressWarnings("all")
    public void setCodPaymentStatus(final String codPaymentStatus) {
        this.codPaymentStatus = codPaymentStatus;
    }

    @java.lang.SuppressWarnings("all")
    public void setCodPaymentMethod(final String codPaymentMethod) {
        this.codPaymentMethod = codPaymentMethod;
    }

    @java.lang.SuppressWarnings("all")
    public void setCodPaymentReference(final String codPaymentReference) {
        this.codPaymentReference = codPaymentReference;
    }

    @java.lang.SuppressWarnings("all")
    public void setCodPaidAt(final OffsetDateTime codPaidAt) {
        this.codPaidAt = codPaidAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setReceivedBy(final UUID receivedBy) {
        this.receivedBy = receivedBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setReceivedAt(final OffsetDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setCollectedByName(final String collectedByName) {
        this.collectedByName = collectedByName;
    }

    @java.lang.SuppressWarnings("all")
    public void setHandedOverBy(final UUID handedOverBy) {
        this.handedOverBy = handedOverBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setCollectedAt(final OffsetDateTime collectedAt) {
        this.collectedAt = collectedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setDeliveredAt(final OffsetDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setReturnedAt(final OffsetDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setClosedReason(final String closedReason) {
        this.closedReason = closedReason;
    }

    @java.lang.SuppressWarnings("all")
    public void setCreatedBy(final UUID createdBy) {
        this.createdBy = createdBy;
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
    public Delivery() {}
}
