package com.societyapp.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class EmergencyIncident extends BaseUuidEntity {
    private UUID societyId;
    private String incidentNumber;
    private UUID reporterId;
    private String reporterRole;
    private UUID residentId;
    private UUID flatId;
    private String incidentType;
    private String severity;
    private String title;
    private String description;
    private String locationDetails;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private int peopleAffected;
    private String injuredPersonName;
    private String injuredPersonMobile;
    private String injuryDetails;
    private String status;
    private UUID assignedStaffId;
    private boolean emergencyContactNotified;
    private boolean ambulanceRequested;
    private boolean policeRequested;
    private boolean fireServiceRequested;
    private String hospitalName;
    private String externalReference;
    private OffsetDateTime acknowledgedAt;
    private OffsetDateTime responseStartedAt;
    private OffsetDateTime arrivedAt;
    private OffsetDateTime stabilizedAt;
    private OffsetDateTime resolvedAt;
    private OffsetDateTime closedAt;
    private String resolutionSummary;
    private String closedReason;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @java.lang.SuppressWarnings("all")
    public UUID getSocietyId() {
        return this.societyId;
    }

    @java.lang.SuppressWarnings("all")
    public String getIncidentNumber() {
        return this.incidentNumber;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getReporterId() {
        return this.reporterId;
    }

    @java.lang.SuppressWarnings("all")
    public String getReporterRole() {
        return this.reporterRole;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getResidentId() {
        return this.residentId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getFlatId() {
        return this.flatId;
    }

    @java.lang.SuppressWarnings("all")
    public String getIncidentType() {
        return this.incidentType;
    }

    @java.lang.SuppressWarnings("all")
    public String getSeverity() {
        return this.severity;
    }

    @java.lang.SuppressWarnings("all")
    public String getTitle() {
        return this.title;
    }

    @java.lang.SuppressWarnings("all")
    public String getDescription() {
        return this.description;
    }

    @java.lang.SuppressWarnings("all")
    public String getLocationDetails() {
        return this.locationDetails;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getLatitude() {
        return this.latitude;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getLongitude() {
        return this.longitude;
    }

    @java.lang.SuppressWarnings("all")
    public int getPeopleAffected() {
        return this.peopleAffected;
    }

    @java.lang.SuppressWarnings("all")
    public String getInjuredPersonName() {
        return this.injuredPersonName;
    }

    @java.lang.SuppressWarnings("all")
    public String getInjuredPersonMobile() {
        return this.injuredPersonMobile;
    }

    @java.lang.SuppressWarnings("all")
    public String getInjuryDetails() {
        return this.injuryDetails;
    }

    @java.lang.SuppressWarnings("all")
    public String getStatus() {
        return this.status;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getAssignedStaffId() {
        return this.assignedStaffId;
    }

    @java.lang.SuppressWarnings("all")
    public boolean isEmergencyContactNotified() {
        return this.emergencyContactNotified;
    }

    @java.lang.SuppressWarnings("all")
    public boolean isAmbulanceRequested() {
        return this.ambulanceRequested;
    }

    @java.lang.SuppressWarnings("all")
    public boolean isPoliceRequested() {
        return this.policeRequested;
    }

    @java.lang.SuppressWarnings("all")
    public boolean isFireServiceRequested() {
        return this.fireServiceRequested;
    }

    @java.lang.SuppressWarnings("all")
    public String getHospitalName() {
        return this.hospitalName;
    }

    @java.lang.SuppressWarnings("all")
    public String getExternalReference() {
        return this.externalReference;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getAcknowledgedAt() {
        return this.acknowledgedAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getResponseStartedAt() {
        return this.responseStartedAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getArrivedAt() {
        return this.arrivedAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getStabilizedAt() {
        return this.stabilizedAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getResolvedAt() {
        return this.resolvedAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getClosedAt() {
        return this.closedAt;
    }

    @java.lang.SuppressWarnings("all")
    public String getResolutionSummary() {
        return this.resolutionSummary;
    }

    @java.lang.SuppressWarnings("all")
    public String getClosedReason() {
        return this.closedReason;
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
    public void setIncidentNumber(final String incidentNumber) {
        this.incidentNumber = incidentNumber;
    }

    @java.lang.SuppressWarnings("all")
    public void setReporterId(final UUID reporterId) {
        this.reporterId = reporterId;
    }

    @java.lang.SuppressWarnings("all")
    public void setReporterRole(final String reporterRole) {
        this.reporterRole = reporterRole;
    }

    @java.lang.SuppressWarnings("all")
    public void setResidentId(final UUID residentId) {
        this.residentId = residentId;
    }

    @java.lang.SuppressWarnings("all")
    public void setFlatId(final UUID flatId) {
        this.flatId = flatId;
    }

    @java.lang.SuppressWarnings("all")
    public void setIncidentType(final String incidentType) {
        this.incidentType = incidentType;
    }

    @java.lang.SuppressWarnings("all")
    public void setSeverity(final String severity) {
        this.severity = severity;
    }

    @java.lang.SuppressWarnings("all")
    public void setTitle(final String title) {
        this.title = title;
    }

    @java.lang.SuppressWarnings("all")
    public void setDescription(final String description) {
        this.description = description;
    }

    @java.lang.SuppressWarnings("all")
    public void setLocationDetails(final String locationDetails) {
        this.locationDetails = locationDetails;
    }

    @java.lang.SuppressWarnings("all")
    public void setLatitude(final BigDecimal latitude) {
        this.latitude = latitude;
    }

    @java.lang.SuppressWarnings("all")
    public void setLongitude(final BigDecimal longitude) {
        this.longitude = longitude;
    }

    @java.lang.SuppressWarnings("all")
    public void setPeopleAffected(final int peopleAffected) {
        this.peopleAffected = peopleAffected;
    }

    @java.lang.SuppressWarnings("all")
    public void setInjuredPersonName(final String injuredPersonName) {
        this.injuredPersonName = injuredPersonName;
    }

    @java.lang.SuppressWarnings("all")
    public void setInjuredPersonMobile(final String injuredPersonMobile) {
        this.injuredPersonMobile = injuredPersonMobile;
    }

    @java.lang.SuppressWarnings("all")
    public void setInjuryDetails(final String injuryDetails) {
        this.injuryDetails = injuryDetails;
    }

    @java.lang.SuppressWarnings("all")
    public void setStatus(final String status) {
        this.status = status;
    }

    @java.lang.SuppressWarnings("all")
    public void setAssignedStaffId(final UUID assignedStaffId) {
        this.assignedStaffId = assignedStaffId;
    }

    @java.lang.SuppressWarnings("all")
    public void setEmergencyContactNotified(final boolean emergencyContactNotified) {
        this.emergencyContactNotified = emergencyContactNotified;
    }

    @java.lang.SuppressWarnings("all")
    public void setAmbulanceRequested(final boolean ambulanceRequested) {
        this.ambulanceRequested = ambulanceRequested;
    }

    @java.lang.SuppressWarnings("all")
    public void setPoliceRequested(final boolean policeRequested) {
        this.policeRequested = policeRequested;
    }

    @java.lang.SuppressWarnings("all")
    public void setFireServiceRequested(final boolean fireServiceRequested) {
        this.fireServiceRequested = fireServiceRequested;
    }

    @java.lang.SuppressWarnings("all")
    public void setHospitalName(final String hospitalName) {
        this.hospitalName = hospitalName;
    }

    @java.lang.SuppressWarnings("all")
    public void setExternalReference(final String externalReference) {
        this.externalReference = externalReference;
    }

    @java.lang.SuppressWarnings("all")
    public void setAcknowledgedAt(final OffsetDateTime acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setResponseStartedAt(final OffsetDateTime responseStartedAt) {
        this.responseStartedAt = responseStartedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setArrivedAt(final OffsetDateTime arrivedAt) {
        this.arrivedAt = arrivedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setStabilizedAt(final OffsetDateTime stabilizedAt) {
        this.stabilizedAt = stabilizedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setResolvedAt(final OffsetDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setClosedAt(final OffsetDateTime closedAt) {
        this.closedAt = closedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setResolutionSummary(final String resolutionSummary) {
        this.resolutionSummary = resolutionSummary;
    }

    @java.lang.SuppressWarnings("all")
    public void setClosedReason(final String closedReason) {
        this.closedReason = closedReason;
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
    public EmergencyIncident() {}
}
