package com.societyapp.domain.model;

import java.time.*;
import java.util.UUID;

public class StaffAttendance extends BaseUuidEntity {
    private UUID societyId;
    private UUID staffUserId;
    private LocalDate attendanceDate;
    private String status;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private String note;
    private UUID markedBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getStaffUserId() {
        return this.staffUserId;
    }

    @java.lang.SuppressWarnings("all")
    public LocalDate getAttendanceDate() {
        return this.attendanceDate;
    }

    @java.lang.SuppressWarnings("all")
    public String getStatus() {
        return this.status;
    }

    @java.lang.SuppressWarnings("all")
    public LocalTime getCheckIn() {
        return this.checkIn;
    }

    @java.lang.SuppressWarnings("all")
    public LocalTime getCheckOut() {
        return this.checkOut;
    }

    @java.lang.SuppressWarnings("all")
    public String getNote() {
        return this.note;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getMarkedBy() {
        return this.markedBy;
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
    public void setStaffUserId(final UUID staffUserId) {
        this.staffUserId = staffUserId;
    }

    @java.lang.SuppressWarnings("all")
    public void setAttendanceDate(final LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    @java.lang.SuppressWarnings("all")
    public void setStatus(final String status) {
        this.status = status;
    }

    @java.lang.SuppressWarnings("all")
    public void setCheckIn(final LocalTime checkIn) {
        this.checkIn = checkIn;
    }

    @java.lang.SuppressWarnings("all")
    public void setCheckOut(final LocalTime checkOut) {
        this.checkOut = checkOut;
    }

    @java.lang.SuppressWarnings("all")
    public void setNote(final String note) {
        this.note = note;
    }

    @java.lang.SuppressWarnings("all")
    public void setMarkedBy(final UUID markedBy) {
        this.markedBy = markedBy;
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
    public StaffAttendance() {}
}
