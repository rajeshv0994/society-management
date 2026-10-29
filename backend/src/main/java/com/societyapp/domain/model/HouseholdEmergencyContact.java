package com.societyapp.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class HouseholdEmergencyContact extends BaseUuidEntity {
    private UUID societyId;
    private UUID primaryResidentId;
    private String fullName;
    private String relationship;
    private String mobile;
    private String alternateMobile;
    private int priority;
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
    public String getFullName() {
        return this.fullName;
    }

    @java.lang.SuppressWarnings("all")
    public String getRelationship() {
        return this.relationship;
    }

    @java.lang.SuppressWarnings("all")
    public String getMobile() {
        return this.mobile;
    }

    @java.lang.SuppressWarnings("all")
    public String getAlternateMobile() {
        return this.alternateMobile;
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
    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }

    @java.lang.SuppressWarnings("all")
    public void setRelationship(final String relationship) {
        this.relationship = relationship;
    }

    @java.lang.SuppressWarnings("all")
    public void setMobile(final String mobile) {
        this.mobile = mobile;
    }

    @java.lang.SuppressWarnings("all")
    public void setAlternateMobile(final String alternateMobile) {
        this.alternateMobile = alternateMobile;
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
    public void setCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setUpdatedAt(final OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @java.lang.SuppressWarnings("all")
    public HouseholdEmergencyContact() {}
}
