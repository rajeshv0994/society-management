package com.societyapp.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class PlatformServiceModule {
    private String code;
    private String name;
    private String description;
    private String status;
    private int sortOrder;
    private String availabilityType = "OPTIONAL_FREE";
    private BigDecimal pricePerSpace = BigDecimal.ZERO;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @java.lang.SuppressWarnings("all")
    public String getCode() {
        return this.code;
    }

    @java.lang.SuppressWarnings("all")
    public String getName() {
        return this.name;
    }

    @java.lang.SuppressWarnings("all")
    public String getDescription() {
        return this.description;
    }

    @java.lang.SuppressWarnings("all")
    public String getStatus() {
        return this.status;
    }

    @java.lang.SuppressWarnings("all")
    public int getSortOrder() {
        return this.sortOrder;
    }

    @java.lang.SuppressWarnings("all")
    public String getAvailabilityType() {
        return this.availabilityType;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getPricePerSpace() {
        return this.pricePerSpace;
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
    public void setCode(final String code) {
        this.code = code;
    }

    @java.lang.SuppressWarnings("all")
    public void setName(final String name) {
        this.name = name;
    }

    @java.lang.SuppressWarnings("all")
    public void setDescription(final String description) {
        this.description = description;
    }

    @java.lang.SuppressWarnings("all")
    public void setStatus(final String status) {
        this.status = status;
    }

    @java.lang.SuppressWarnings("all")
    public void setSortOrder(final int sortOrder) {
        this.sortOrder = sortOrder;
    }

    @java.lang.SuppressWarnings("all")
    public void setAvailabilityType(final String availabilityType) {
        this.availabilityType = availabilityType;
    }

    @java.lang.SuppressWarnings("all")
    public void setPricePerSpace(final BigDecimal pricePerSpace) {
        this.pricePerSpace = pricePerSpace;
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
    public PlatformServiceModule() {}
}
