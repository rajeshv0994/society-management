package com.societyapp.domain.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class HouseholdMember extends BaseUuidEntity {
    private UUID societyId;
    private UUID primaryResidentId;
    private String memberCode;
    private String fullName;
    private String relationship;
    private String mobile;
    private LocalDate dateOfBirth;
    private String email;
    private String gender;
    private String bloodGroup;
    private String occupation;
    private String identityType;
    private String identityLast4;
    private String emergencyContactName;
    private String emergencyContactMobile;
    private boolean accessEnabled;
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
    public String getMemberCode() {
        return this.memberCode;
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
    public LocalDate getDateOfBirth() {
        return this.dateOfBirth;
    }

    @java.lang.SuppressWarnings("all")
    public String getEmail() {
        return this.email;
    }

    @java.lang.SuppressWarnings("all")
    public String getGender() {
        return this.gender;
    }

    @java.lang.SuppressWarnings("all")
    public String getBloodGroup() {
        return this.bloodGroup;
    }

    @java.lang.SuppressWarnings("all")
    public String getOccupation() {
        return this.occupation;
    }

    @java.lang.SuppressWarnings("all")
    public String getIdentityType() {
        return this.identityType;
    }

    @java.lang.SuppressWarnings("all")
    public String getIdentityLast4() {
        return this.identityLast4;
    }

    @java.lang.SuppressWarnings("all")
    public String getEmergencyContactName() {
        return this.emergencyContactName;
    }

    @java.lang.SuppressWarnings("all")
    public String getEmergencyContactMobile() {
        return this.emergencyContactMobile;
    }

    @java.lang.SuppressWarnings("all")
    public boolean isAccessEnabled() {
        return this.accessEnabled;
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
    public void setMemberCode(final String memberCode) {
        this.memberCode = memberCode;
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
    public void setDateOfBirth(final LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    @java.lang.SuppressWarnings("all")
    public void setEmail(final String email) {
        this.email = email;
    }

    @java.lang.SuppressWarnings("all")
    public void setGender(final String gender) {
        this.gender = gender;
    }

    @java.lang.SuppressWarnings("all")
    public void setBloodGroup(final String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    @java.lang.SuppressWarnings("all")
    public void setOccupation(final String occupation) {
        this.occupation = occupation;
    }

    @java.lang.SuppressWarnings("all")
    public void setIdentityType(final String identityType) {
        this.identityType = identityType;
    }

    @java.lang.SuppressWarnings("all")
    public void setIdentityLast4(final String identityLast4) {
        this.identityLast4 = identityLast4;
    }

    @java.lang.SuppressWarnings("all")
    public void setEmergencyContactName(final String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    @java.lang.SuppressWarnings("all")
    public void setEmergencyContactMobile(final String emergencyContactMobile) {
        this.emergencyContactMobile = emergencyContactMobile;
    }

    @java.lang.SuppressWarnings("all")
    public void setAccessEnabled(final boolean accessEnabled) {
        this.accessEnabled = accessEnabled;
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
    public HouseholdMember() {}
}
