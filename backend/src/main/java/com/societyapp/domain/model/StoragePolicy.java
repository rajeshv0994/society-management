package com.societyapp.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class StoragePolicy {
    private Short id;
    private int defaultFreeGb;
    private int purchaseBlockGb;
    private BigDecimal pricePerBlockMonthly;
    private UUID updatedBy;
    private OffsetDateTime updatedAt;

    @java.lang.SuppressWarnings("all")
    public Short getId() {
        return this.id;
    }

    @java.lang.SuppressWarnings("all")
    public int getDefaultFreeGb() {
        return this.defaultFreeGb;
    }

    @java.lang.SuppressWarnings("all")
    public int getPurchaseBlockGb() {
        return this.purchaseBlockGb;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getPricePerBlockMonthly() {
        return this.pricePerBlockMonthly;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getUpdatedBy() {
        return this.updatedBy;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setId(final Short id) {
        this.id = id;
    }

    @java.lang.SuppressWarnings("all")
    public void setDefaultFreeGb(final int defaultFreeGb) {
        this.defaultFreeGb = defaultFreeGb;
    }

    @java.lang.SuppressWarnings("all")
    public void setPurchaseBlockGb(final int purchaseBlockGb) {
        this.purchaseBlockGb = purchaseBlockGb;
    }

    @java.lang.SuppressWarnings("all")
    public void setPricePerBlockMonthly(final BigDecimal pricePerBlockMonthly) {
        this.pricePerBlockMonthly = pricePerBlockMonthly;
    }

    @java.lang.SuppressWarnings("all")
    public void setUpdatedBy(final UUID updatedBy) {
        this.updatedBy = updatedBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setUpdatedAt(final OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @java.lang.SuppressWarnings("all")
    public StoragePolicy() {}
}
