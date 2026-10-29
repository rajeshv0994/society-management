package com.societyapp.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class AmenityBooking extends BaseUuidEntity {
    private UUID societyId;
    private UUID amenityId;
    private UUID bookedBy;
    private String bookingNumber;
    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;
    private BigDecimal amount;
    private BigDecimal baseCharge;
    private BigDecimal depositAmount;
    private String purpose;
    private int attendees;
    private String status;
    private String paymentStatus;
    private String paymentMethod;
    private String paymentReference;
    private UUID reviewedBy;
    private OffsetDateTime reviewedAt;
    private String rejectionReason;
    private OffsetDateTime cancelledAt;
    private OffsetDateTime checkedInAt;
    private OffsetDateTime checkedOutAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getAmenityId() {
        return this.amenityId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getBookedBy() {
        return this.bookedBy;
    }

    @java.lang.SuppressWarnings("all")
    public String getBookingNumber() {
        return this.bookingNumber;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getStartsAt() {
        return this.startsAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getEndsAt() {
        return this.endsAt;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getAmount() {
        return this.amount;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getBaseCharge() {
        return this.baseCharge;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getDepositAmount() {
        return this.depositAmount;
    }

    @java.lang.SuppressWarnings("all")
    public String getPurpose() {
        return this.purpose;
    }

    @java.lang.SuppressWarnings("all")
    public int getAttendees() {
        return this.attendees;
    }

    @java.lang.SuppressWarnings("all")
    public String getStatus() {
        return this.status;
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
    public OffsetDateTime getCancelledAt() {
        return this.cancelledAt;
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
    public void setAmenityId(final UUID amenityId) {
        this.amenityId = amenityId;
    }

    @java.lang.SuppressWarnings("all")
    public void setBookedBy(final UUID bookedBy) {
        this.bookedBy = bookedBy;
    }

    @java.lang.SuppressWarnings("all")
    public void setBookingNumber(final String bookingNumber) {
        this.bookingNumber = bookingNumber;
    }

    @java.lang.SuppressWarnings("all")
    public void setStartsAt(final OffsetDateTime startsAt) {
        this.startsAt = startsAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setEndsAt(final OffsetDateTime endsAt) {
        this.endsAt = endsAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    @java.lang.SuppressWarnings("all")
    public void setBaseCharge(final BigDecimal baseCharge) {
        this.baseCharge = baseCharge;
    }

    @java.lang.SuppressWarnings("all")
    public void setDepositAmount(final BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    @java.lang.SuppressWarnings("all")
    public void setPurpose(final String purpose) {
        this.purpose = purpose;
    }

    @java.lang.SuppressWarnings("all")
    public void setAttendees(final int attendees) {
        this.attendees = attendees;
    }

    @java.lang.SuppressWarnings("all")
    public void setStatus(final String status) {
        this.status = status;
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
    public void setCancelledAt(final OffsetDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
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
    public void setCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setUpdatedAt(final OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @java.lang.SuppressWarnings("all")
    public AmenityBooking() {}
}
