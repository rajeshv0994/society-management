package com.societyapp.domain.model;

import java.time.*;
import java.util.UUID;

public class ResidentRegistration extends BaseUuidEntity {
    private UUID societyId;
    private UUID flatId;
    private String residentType;
    private String fullName;
    private String mobile;
    private String email;
    private LocalDate dob;
    private String gender;
    private String emergencyContact;
    private LocalDate moveInDate;
    private LocalDate moveOutDate;
    private String status;
    private String rejectionReason;
    private boolean invited;
    private String invitationTokenHash;
    private OffsetDateTime invitationExpiresAt;
    private OffsetDateTime submittedAt;
    private UUID reviewedBy;
    private OffsetDateTime reviewedAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getFlatId() {
        return this.flatId;
    }

    @java.lang.SuppressWarnings("all")
    public String getResidentType() {
        return this.residentType;
    }

    @java.lang.SuppressWarnings("all")
    public String getFullName() {
        return this.fullName;
    }

    @java.lang.SuppressWarnings("all")
    public String getMobile() {
        return this.mobile;
    }

    @java.lang.SuppressWarnings("all")
    public String getEmail() {
        return this.email;
    }

    @java.lang.SuppressWarnings("all")
    public LocalDate getDob() {
        return this.dob;
    }

    @java.lang.SuppressWarnings("all")
    public String getGender() {
        return this.gender;
    }

    @java.lang.SuppressWarnings("all")
    public String getEmergencyContact() {
        return this.emergencyContact;
    }

    @java.lang.SuppressWarnings("all")
    public LocalDate getMoveInDate() {
        return this.moveInDate;
    }

    @java.lang.SuppressWarnings("all")
    public LocalDate getMoveOutDate() {
        return this.moveOutDate;
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
    public boolean isInvited() {
        return this.invited;
    }

    @java.lang.SuppressWarnings("all")
    public String getInvitationTokenHash() {
        return this.invitationTokenHash;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getInvitationExpiresAt() {
        return this.invitationExpiresAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getSubmittedAt() {
        return this.submittedAt;
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
    public void setSocietyId(final UUID societyId) {
        this.societyId = societyId;
    }

    @java.lang.SuppressWarnings("all")
    public void setFlatId(final UUID flatId) {
        this.flatId = flatId;
    }

    @java.lang.SuppressWarnings("all")
    public void setResidentType(final String residentType) {
        this.residentType = residentType;
    }

    @java.lang.SuppressWarnings("all")
    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }

    @java.lang.SuppressWarnings("all")
    public void setMobile(final String mobile) {
        this.mobile = mobile;
    }

    @java.lang.SuppressWarnings("all")
    public void setEmail(final String email) {
        this.email = email;
    }

    @java.lang.SuppressWarnings("all")
    public void setDob(final LocalDate dob) {
        this.dob = dob;
    }

    @java.lang.SuppressWarnings("all")
    public void setGender(final String gender) {
        this.gender = gender;
    }

    @java.lang.SuppressWarnings("all")
    public void setEmergencyContact(final String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    @java.lang.SuppressWarnings("all")
    public void setMoveInDate(final LocalDate moveInDate) {
        this.moveInDate = moveInDate;
    }

    @java.lang.SuppressWarnings("all")
    public void setMoveOutDate(final LocalDate moveOutDate) {
        this.moveOutDate = moveOutDate;
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
    public void setInvited(final boolean invited) {
        this.invited = invited;
    }

    @java.lang.SuppressWarnings("all")
    public void setInvitationTokenHash(final String invitationTokenHash) {
        this.invitationTokenHash = invitationTokenHash;
    }

    @java.lang.SuppressWarnings("all")
    public void setInvitationExpiresAt(final OffsetDateTime invitationExpiresAt) {
        this.invitationExpiresAt = invitationExpiresAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setSubmittedAt(final OffsetDateTime submittedAt) {
        this.submittedAt = submittedAt;
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
    public ResidentRegistration() {}
}
