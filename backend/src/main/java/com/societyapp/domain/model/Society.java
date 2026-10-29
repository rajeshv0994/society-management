package com.societyapp.domain.model;

import java.time.OffsetDateTime;

public class Society extends BaseUuidEntity {
    private String name;
    private String registrationNumber;
    private String societyCode;
    private String societyType;
    private int totalBlocks;
    private int totalFlats;
    private Integer approximateResidents;
    private int plannedStaff;
    private String address;
    private String city;
    private String state;
    private String pinCode;
    private String contactPerson;
    private String email;
    private String mobile;
    private String planCode;
    private String enabledModules = "[]";
    private String status;
    private String rejectionReason;
    private String registrationAccessHash;
    private String subscriptionStatus;
    private OffsetDateTime subscriptionStartedAt;
    private OffsetDateTime subscriptionExpiresAt;
    private String subscriptionBillingCycle = "MONTHLY";
    private boolean setupCompleted;
    private String description;
    private String website;
    private String logoStorageKey;
    private String logoContentType;
    private OffsetDateTime createdAt;
    private OffsetDateTime approvedAt;
    private OffsetDateTime updatedAt;

    @java.lang.SuppressWarnings("all")
    public String getName() {
        return this.name;
    }

    @java.lang.SuppressWarnings("all")
    public String getRegistrationNumber() {
        return this.registrationNumber;
    }

    @java.lang.SuppressWarnings("all")
    public String getSocietyCode() {
        return this.societyCode;
    }

    @java.lang.SuppressWarnings("all")
    public String getSocietyType() {
        return this.societyType;
    }

    @java.lang.SuppressWarnings("all")
    public int getTotalBlocks() {
        return this.totalBlocks;
    }

    @java.lang.SuppressWarnings("all")
    public int getTotalFlats() {
        return this.totalFlats;
    }

    @java.lang.SuppressWarnings("all")
    public Integer getApproximateResidents() {
        return this.approximateResidents;
    }

    @java.lang.SuppressWarnings("all")
    public int getPlannedStaff() {
        return this.plannedStaff;
    }

    @java.lang.SuppressWarnings("all")
    public String getAddress() {
        return this.address;
    }

    @java.lang.SuppressWarnings("all")
    public String getCity() {
        return this.city;
    }

    @java.lang.SuppressWarnings("all")
    public String getState() {
        return this.state;
    }

    @java.lang.SuppressWarnings("all")
    public String getPinCode() {
        return this.pinCode;
    }

    @java.lang.SuppressWarnings("all")
    public String getContactPerson() {
        return this.contactPerson;
    }

    @java.lang.SuppressWarnings("all")
    public String getEmail() {
        return this.email;
    }

    @java.lang.SuppressWarnings("all")
    public String getMobile() {
        return this.mobile;
    }

    @java.lang.SuppressWarnings("all")
    public String getPlanCode() {
        return this.planCode;
    }

    @java.lang.SuppressWarnings("all")
    public String getEnabledModules() {
        return this.enabledModules;
    }

    @java.lang.SuppressWarnings("all")
    public String getStatus() {
        return this.status;
    }

    @java.lang.SuppressWarnings("all")
    public String getRejectionReason() {
        return this.rejectionReason;
    }

    @java.lang.SuppressWarnings("all")
    public String getRegistrationAccessHash() {
        return this.registrationAccessHash;
    }

    @java.lang.SuppressWarnings("all")
    public String getSubscriptionStatus() {
        return this.subscriptionStatus;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getSubscriptionStartedAt() {
        return this.subscriptionStartedAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getSubscriptionExpiresAt() {
        return this.subscriptionExpiresAt;
    }

    @java.lang.SuppressWarnings("all")
    public String getSubscriptionBillingCycle() {
        return this.subscriptionBillingCycle;
    }

    @java.lang.SuppressWarnings("all")
    public boolean isSetupCompleted() {
        return this.setupCompleted;
    }

    @java.lang.SuppressWarnings("all")
    public String getDescription() {
        return this.description;
    }

    @java.lang.SuppressWarnings("all")
    public String getWebsite() {
        return this.website;
    }

    @java.lang.SuppressWarnings("all")
    public String getLogoStorageKey() {
        return this.logoStorageKey;
    }

    @java.lang.SuppressWarnings("all")
    public String getLogoContentType() {
        return this.logoContentType;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getApprovedAt() {
        return this.approvedAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setName(final String name) {
        this.name = name;
    }

    @java.lang.SuppressWarnings("all")
    public void setRegistrationNumber(final String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    @java.lang.SuppressWarnings("all")
    public void setSocietyCode(final String societyCode) {
        this.societyCode = societyCode;
    }

    @java.lang.SuppressWarnings("all")
    public void setSocietyType(final String societyType) {
        this.societyType = societyType;
    }

    @java.lang.SuppressWarnings("all")
    public void setTotalBlocks(final int totalBlocks) {
        this.totalBlocks = totalBlocks;
    }

    @java.lang.SuppressWarnings("all")
    public void setTotalFlats(final int totalFlats) {
        this.totalFlats = totalFlats;
    }

    @java.lang.SuppressWarnings("all")
    public void setApproximateResidents(final Integer approximateResidents) {
        this.approximateResidents = approximateResidents;
    }

    @java.lang.SuppressWarnings("all")
    public void setPlannedStaff(final int plannedStaff) {
        this.plannedStaff = plannedStaff;
    }

    @java.lang.SuppressWarnings("all")
    public void setAddress(final String address) {
        this.address = address;
    }

    @java.lang.SuppressWarnings("all")
    public void setCity(final String city) {
        this.city = city;
    }

    @java.lang.SuppressWarnings("all")
    public void setState(final String state) {
        this.state = state;
    }

    @java.lang.SuppressWarnings("all")
    public void setPinCode(final String pinCode) {
        this.pinCode = pinCode;
    }

    @java.lang.SuppressWarnings("all")
    public void setContactPerson(final String contactPerson) {
        this.contactPerson = contactPerson;
    }

    @java.lang.SuppressWarnings("all")
    public void setEmail(final String email) {
        this.email = email;
    }

    @java.lang.SuppressWarnings("all")
    public void setMobile(final String mobile) {
        this.mobile = mobile;
    }

    @java.lang.SuppressWarnings("all")
    public void setPlanCode(final String planCode) {
        this.planCode = planCode;
    }

    @java.lang.SuppressWarnings("all")
    public void setEnabledModules(final String enabledModules) {
        this.enabledModules = enabledModules;
    }

    @java.lang.SuppressWarnings("all")
    public void setStatus(final String status) {
        this.status = status;
    }

    @java.lang.SuppressWarnings("all")
    public void setRejectionReason(final String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    @java.lang.SuppressWarnings("all")
    public void setRegistrationAccessHash(final String registrationAccessHash) {
        this.registrationAccessHash = registrationAccessHash;
    }

    @java.lang.SuppressWarnings("all")
    public void setSubscriptionStatus(final String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    @java.lang.SuppressWarnings("all")
    public void setSubscriptionStartedAt(final OffsetDateTime subscriptionStartedAt) {
        this.subscriptionStartedAt = subscriptionStartedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setSubscriptionExpiresAt(final OffsetDateTime subscriptionExpiresAt) {
        this.subscriptionExpiresAt = subscriptionExpiresAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setSubscriptionBillingCycle(final String subscriptionBillingCycle) {
        this.subscriptionBillingCycle = subscriptionBillingCycle;
    }

    @java.lang.SuppressWarnings("all")
    public void setSetupCompleted(final boolean setupCompleted) {
        this.setupCompleted = setupCompleted;
    }

    @java.lang.SuppressWarnings("all")
    public void setDescription(final String description) {
        this.description = description;
    }

    @java.lang.SuppressWarnings("all")
    public void setWebsite(final String website) {
        this.website = website;
    }

    @java.lang.SuppressWarnings("all")
    public void setLogoStorageKey(final String logoStorageKey) {
        this.logoStorageKey = logoStorageKey;
    }

    @java.lang.SuppressWarnings("all")
    public void setLogoContentType(final String logoContentType) {
        this.logoContentType = logoContentType;
    }

    @java.lang.SuppressWarnings("all")
    public void setCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setApprovedAt(final OffsetDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setUpdatedAt(final OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @java.lang.SuppressWarnings("all")
    public Society() {}
}
