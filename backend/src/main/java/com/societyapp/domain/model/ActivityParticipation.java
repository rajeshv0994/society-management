package com.societyapp.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ActivityParticipation extends BaseUuidEntity {
    private UUID societyId;
    private UUID activityId;
    private UUID participantId;
    private int participantCount;
    private String participantNames;
    private String contactMobile;
    private String notes;
    private BigDecimal amount;
    private String paymentStatus;
    private String paymentMethod;
    private String paymentReference;
    private OffsetDateTime paidAt;
    private String status;
    private UUID reviewedBy;
    private OffsetDateTime reviewedAt;
    private String rejectionReason;
    private OffsetDateTime checkedInAt;
    private OffsetDateTime attendedAt;
    private OffsetDateTime cancelledAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getActivityId() {
        return this.activityId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getParticipantId() {
        return this.participantId;
    }

    @java.lang.SuppressWarnings("all")
    public int getParticipantCount() {
        return this.participantCount;
    }

    @java.lang.SuppressWarnings("all")
    public String getParticipantNames() {
        return this.participantNames;
    }

    @java.lang.SuppressWarnings("all")
    public String getContactMobile() {
        return this.contactMobile;
    }

    @java.lang.SuppressWarnings("all")
    public String getNotes() {
        return this.notes;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getAmount() {
        return this.amount;
    }

    @java.lang.SuppressWarnings("all")
    public String getPaymentStatus() {
        return this.paymentStatus;
    }

    @java.lang.SuppressWarnings("all")
    public String getPaymentMethod() {
        return this.paymentMethod;
    }

    @java.lang.SuppressWarnings("all")
    public String getPaymentReference() {
        return this.paymentReference;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getPaidAt() {
        return this.paidAt;
    }

    @java.lang.SuppressWarnings("all")
    public String getStatus() {
        return this.status;
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
    public String getRejectionReason() {
        return this.rejectionReason;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCheckedInAt() {
        return this.checkedInAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getAttendedAt() {
        return this.attendedAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getCancelledAt() {
        return this.cancelledAt;
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
    public void setActivityId(final UUID activityId) {
        this.activityId = activityId;
    }

    @java.lang.SuppressWarnings("all")
    public void setParticipantId(final UUID participantId) {
        this.participantId = participantId;
    }

    @java.lang.SuppressWarnings("all")
    public void setParticipantCount(final int participantCount) {
        this.participantCount = participantCount;
    }

    @java.lang.SuppressWarnings("all")
    public void setParticipantNames(final String participantNames) {
        this.participantNames = participantNames;
    }

    @java.lang.SuppressWarnings("all")
    public void setContactMobile(final String contactMobile) {
        this.contactMobile = contactMobile;
    }

    @java.lang.SuppressWarnings("all")
    public void setNotes(final String notes) {
        this.notes = notes;
    }

    @java.lang.SuppressWarnings("all")
    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    @java.lang.SuppressWarnings("all")
    public void setPaymentStatus(final String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    @java.lang.SuppressWarnings("all")
    public void setPaymentMethod(final String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @java.lang.SuppressWarnings("all")
    public void setPaymentReference(final String paymentReference) {
        this.paymentReference = paymentReference;
    }

    @java.lang.SuppressWarnings("all")
    public void setPaidAt(final OffsetDateTime paidAt) {
        this.paidAt = paidAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setStatus(final String status) {
        this.status = status;
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
    public void setRejectionReason(final String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    @java.lang.SuppressWarnings("all")
    public void setCheckedInAt(final OffsetDateTime checkedInAt) {
        this.checkedInAt = checkedInAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setAttendedAt(final OffsetDateTime attendedAt) {
        this.attendedAt = attendedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setCancelledAt(final OffsetDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
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
    public ActivityParticipation() {}
}
