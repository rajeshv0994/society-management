package com.societyapp.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Visitor extends BaseUuidEntity {
    private UUID societyId;
    private UUID flatId;
    private String visitorName;
    private String mobile;
    private String purpose;
    private String vehicleNumber;
    private OffsetDateTime expectedAt;
    private UUID approvedBy;
    private UUID hostResidentId;
    private int accompanyingMembers;
    private String photoStorageKey;
    private String photoContentType;
    private OffsetDateTime residentDecisionAt;
    private String targetType;
    private UUID targetUserId;
    private String targetName;
    private UUID staffApprovedBy;
    private String approvalMethod;
    private String approvalNote;
    private UUID checkedInBy;
    private OffsetDateTime checkedInAt;
    private OffsetDateTime checkedOutAt;
    private String status;
    private OffsetDateTime createdAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getFlatId() {
        return this.flatId;
    }

    @java.lang.SuppressWarnings("all")
    public String getVisitorName() {
        return this.visitorName;
    }

    @java.lang.SuppressWarnings("all")
    public String getMobile() {
        return this.mobile;
    }

    @java.lang.SuppressWarnings("all")
    public String getPurpose() {
        return this.purpose;
    }

    @java.lang.SuppressWarnings("all")
    public String getVehicleNumber() {
        return this.vehicleNumber;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getExpectedAt() {
        return this.expectedAt;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getApprovedBy() {
        return this.approvedBy;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getHostResidentId() {
        return this.hostResidentId;
    }

    @java.lang.SuppressWarnings("all")
    public int getAccompanyingMembers() {
        return this.accompanyingMembers;
    }

    @java.lang.SuppressWarnings("all")
    public String getPhotoStorageKey() {
        return this.photoStorageKey;
    }

    @java.lang.SuppressWarnings("all")
    public String getPhotoContentType() {
        return this.photoContentType;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getResidentDecisionAt() {
        return this.residentDecisionAt;
    }

    @java.lang.SuppressWarnings("all")
    public String getTargetType() {
        return this.targetType;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getTargetUserId() {
        return this.targetUserId;
    }

    @java.lang.SuppressWarnings("all")
    public String getTargetName() {
        return this.targetName;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getStaffApprovedBy() {
        return this.staffApprovedBy;
    }

    @java.lang.SuppressWarnings("all")
    public String getApprovalMethod() {
        return this.approvalMethod;
    }

    @java.lang.SuppressWarnings("all")
    public String getApprovalNote() {
        return this.approvalNote;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getCheckedInBy() {
        return this.checkedInBy;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCheckedInAt() {
        return this.checkedInAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCheckedOutAt() {
        return this.checkedOutAt;
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
    public void setSocietyId(final UUID societyId) {
        this.societyId = societyId;
    }

    @java.lang.SuppressWarnings("all")
    public void setFlatId(final UUID flatId) {
        this.flatId = flatId;
    }

    @java.lang.SuppressWarnings("all")
    public void setVisitorName(final String visitorName) {
        this.visitorName = visitorName;
    }

    @java.lang.SuppressWarnings("all")
    public void setMobile(final String mobile) {
        this.mobile = mobile;
    }

    @java.lang.SuppressWarnings("all")
    public void setPurpose(final String purpose) {
        this.purpose = purpose;
    }

    @java.lang.SuppressWarnings("all")
    public void setVehicleNumber(final String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    @java.lang.SuppressWarnings("all")
    public void setExpectedAt(final OffsetDateTime expectedAt) {
        this.expectedAt = expectedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setApprovedBy(final UUID approvedBy) {
        this.approvedBy = approvedBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setHostResidentId(final UUID hostResidentId) {
        this.hostResidentId = hostResidentId;
    }

    @java.lang.SuppressWarnings("all")
    public void setAccompanyingMembers(final int accompanyingMembers) {
        this.accompanyingMembers = accompanyingMembers;
    }

    @java.lang.SuppressWarnings("all")
    public void setPhotoStorageKey(final String photoStorageKey) {
        this.photoStorageKey = photoStorageKey;
    }

    @java.lang.SuppressWarnings("all")
    public void setPhotoContentType(final String photoContentType) {
        this.photoContentType = photoContentType;
    }

    @java.lang.SuppressWarnings("all")
    public void setResidentDecisionAt(final OffsetDateTime residentDecisionAt) {
        this.residentDecisionAt = residentDecisionAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setTargetType(final String targetType) {
        this.targetType = targetType;
    }

    @java.lang.SuppressWarnings("all")
    public void setTargetUserId(final UUID targetUserId) {
        this.targetUserId = targetUserId;
    }

    @java.lang.SuppressWarnings("all")
    public void setTargetName(final String targetName) {
        this.targetName = targetName;
    }

    @java.lang.SuppressWarnings("all")
    public void setStaffApprovedBy(final UUID staffApprovedBy) {
        this.staffApprovedBy = staffApprovedBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setApprovalMethod(final String approvalMethod) {
        this.approvalMethod = approvalMethod;
    }

    @java.lang.SuppressWarnings("all")
    public void setApprovalNote(final String approvalNote) {
        this.approvalNote = approvalNote;
    }

    @java.lang.SuppressWarnings("all")
    public void setCheckedInBy(final UUID checkedInBy) {
        this.checkedInBy = checkedInBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setCheckedInAt(final OffsetDateTime checkedInAt) {
        this.checkedInAt = checkedInAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setCheckedOutAt(final OffsetDateTime checkedOutAt) {
        this.checkedOutAt = checkedOutAt;
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
    public Visitor() {}
}
