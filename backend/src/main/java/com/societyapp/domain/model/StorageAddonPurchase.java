package com.societyapp.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class StorageAddonPurchase extends BaseUuidEntity {
    private UUID societyId;
    private UUID purchasedBy;
    private int blocks;
    private int blockSizeGb;
    private long additionalBytes;
    private BigDecimal pricePerBlockMonthly;
    private BigDecimal monthlyAmount;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime cancelledAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getPurchasedBy() {
        return this.purchasedBy;
    }

    @java.lang.SuppressWarnings("all")
    public int getBlocks() {
        return this.blocks;
    }

    @java.lang.SuppressWarnings("all")
    public int getBlockSizeGb() {
        return this.blockSizeGb;
    }

    @java.lang.SuppressWarnings("all")
    public long getAdditionalBytes() {
        return this.additionalBytes;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getPricePerBlockMonthly() {
        return this.pricePerBlockMonthly;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getMonthlyAmount() {
        return this.monthlyAmount;
    }

    @java.lang.SuppressWarnings("all")
    public String getStatus() {
        return this.status;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCancelledAt() {
        return this.cancelledAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setSocietyId(final UUID societyId) {
        this.societyId = societyId;
    }

    @java.lang.SuppressWarnings("all")
    public void setPurchasedBy(final UUID purchasedBy) {
        this.purchasedBy = purchasedBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setBlocks(final int blocks) {
        this.blocks = blocks;
    }

    @java.lang.SuppressWarnings("all")
    public void setBlockSizeGb(final int blockSizeGb) {
        this.blockSizeGb = blockSizeGb;
    }

    @java.lang.SuppressWarnings("all")
    public void setAdditionalBytes(final long additionalBytes) {
        this.additionalBytes = additionalBytes;
    }

    @java.lang.SuppressWarnings("all")
    public void setPricePerBlockMonthly(final BigDecimal pricePerBlockMonthly) {
        this.pricePerBlockMonthly = pricePerBlockMonthly;
    }

    @java.lang.SuppressWarnings("all")
    public void setMonthlyAmount(final BigDecimal monthlyAmount) {
        this.monthlyAmount = monthlyAmount;
    }

    @java.lang.SuppressWarnings("all")
    public void setStatus(final String status) {
        this.status = status;
    }

    @java.lang.SuppressWarnings("all")
    public void setCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setCancelledAt(final OffsetDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    @java.lang.SuppressWarnings("all")
    public StorageAddonPurchase() {}
}
