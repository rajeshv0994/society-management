package com.societyapp.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class EmergencyDirectoryContact extends BaseUuidEntity {
    private UUID societyId;
    private String contactType;
    private String name;
    private String phone;
    private String alternatePhone;
    private String address;
    private boolean available24x7;
    private int priority;
    private boolean active;
    private UUID createdBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public String getContactType() {
        return this.contactType;
    }

    @java.lang.SuppressWarnings("all")
    public String getName() {
        return this.name;
    }

    @java.lang.SuppressWarnings("all")
    public String getPhone() {
        return this.phone;
    }

    @java.lang.SuppressWarnings("all")
    public String getAlternatePhone() {
        return this.alternatePhone;
    }

    @java.lang.SuppressWarnings("all")
    public String getAddress() {
        return this.address;
    }

    @java.lang.SuppressWarnings("all")
    public boolean isAvailable24x7() {
        return this.available24x7;
    }

    @java.lang.SuppressWarnings("all")
    public int getPriority() {
        return this.priority;
    }

    @java.lang.SuppressWarnings("all")
    public boolean isActive() {
        return this.active;
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
    public void setContactType(final String contactType) {
        this.contactType = contactType;
    }

    @java.lang.SuppressWarnings("all")
    public void setName(final String name) {
        this.name = name;
    }

    @java.lang.SuppressWarnings("all")
    public void setPhone(final String phone) {
        this.phone = phone;
    }

    @java.lang.SuppressWarnings("all")
    public void setAlternatePhone(final String alternatePhone) {
        this.alternatePhone = alternatePhone;
    }

    @java.lang.SuppressWarnings("all")
    public void setAddress(final String address) {
        this.address = address;
    }

    @java.lang.SuppressWarnings("all")
    public void setAvailable24x7(final boolean available24x7) {
        this.available24x7 = available24x7;
    }

    @java.lang.SuppressWarnings("all")
    public void setPriority(final int priority) {
        this.priority = priority;
    }

    @java.lang.SuppressWarnings("all")
    public void setActive(final boolean active) {
        this.active = active;
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
    public EmergencyDirectoryContact() {}
}
