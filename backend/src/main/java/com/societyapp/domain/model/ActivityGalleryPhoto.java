package com.societyapp.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ActivityGalleryPhoto extends BaseUuidEntity {
    private UUID societyId;
    private UUID activityId;
    private UUID submittedBy;
    private String storageKey;
    private String contentType;
    private String caption;
    private String moderationStatus;
    private boolean featured;
    private UUID reviewedBy;
    private OffsetDateTime reviewedAt;
    private String rejectionReason;
    private OffsetDateTime createdAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getActivityId() {
        return this.activityId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getSubmittedBy() {
        return this.submittedBy;
    }

    @java.lang.SuppressWarnings("all")
    public String getStorageKey() {
        return this.storageKey;
    }

    @java.lang.SuppressWarnings("all")
    public String getContentType() {
        return this.contentType;
    }

    @java.lang.SuppressWarnings("all")
    public String getCaption() {
        return this.caption;
    }

    @java.lang.SuppressWarnings("all")
    public String getModerationStatus() {
        return this.moderationStatus;
    }

    @java.lang.SuppressWarnings("all")
    public boolean isFeatured() {
        return this.featured;
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
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
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
    public void setSubmittedBy(final UUID submittedBy) {
        this.submittedBy = submittedBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setStorageKey(final String storageKey) {
        this.storageKey = storageKey;
    }

    @java.lang.SuppressWarnings("all")
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    @java.lang.SuppressWarnings("all")
    public void setCaption(final String caption) {
        this.caption = caption;
    }

    @java.lang.SuppressWarnings("all")
    public void setModerationStatus(final String moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    @java.lang.SuppressWarnings("all")
    public void setFeatured(final boolean featured) {
        this.featured = featured;
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
    public void setCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public ActivityGalleryPhoto() {}
}
