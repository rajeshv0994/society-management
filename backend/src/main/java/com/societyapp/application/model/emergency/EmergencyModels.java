package com.societyapp.application.model.emergency;

import com.societyapp.domain.type.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class EmergencyModels {
    private EmergencyModels() {}

    public record Actor(UUID societyId, UUID userId, UserRole role) {}

    public record CreateCommand(
            UUID residentId,
            UUID spaceId,
            EmergencyIncidentType incidentType,
            IncidentSeverity severity,
            String title,
            String description,
            String locationDetails,
            BigDecimal latitude,
            BigDecimal longitude,
            int peopleAffected,
            String injuredPersonName,
            String injuredPersonMobile,
            String injuryDetails) {}

    public record ActionCommand(
            EmergencyAction action,
            String note,
            UUID assignedStaffId,
            String hospitalName,
            String externalReference,
            IncidentSeverity severity) {}

    public record DirectoryCommand(
            EmergencyContactType contactType,
            String name,
            String phone,
            String alternatePhone,
            String address,
            boolean available24x7,
            int priority) {}

    public record IncidentView(
            UUID id,
            String incidentNumber,
            EmergencyIncidentType incidentType,
            IncidentSeverity severity,
            String title,
            String description,
            String locationDetails,
            BigDecimal latitude,
            BigDecimal longitude,
            int peopleAffected,
            String injuredPersonName,
            String injuredPersonMobile,
            String injuryDetails,
            EmergencyIncidentStatus status,
            boolean emergencyContactNotified,
            boolean ambulanceRequested,
            boolean policeRequested,
            boolean fireServiceRequested,
            String hospitalName,
            String externalReference,
            OffsetDateTime acknowledgedAt,
            OffsetDateTime responseStartedAt,
            OffsetDateTime arrivedAt,
            OffsetDateTime stabilizedAt,
            OffsetDateTime resolvedAt,
            OffsetDateTime closedAt,
            String resolutionSummary,
            String closedReason,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            UUID reporterId,
            String reporterName,
            UUID residentId,
            String residentName,
            String residentMobile,
            UUID assignedStaffId,
            String assignedStaffName,
            String assignedStaffMobile,
            UUID spaceId,
            String spaceNumber,
            String block) {}

    public record DirectoryContact(
            UUID id,
            EmergencyContactType contactType,
            String name,
            String phone,
            String alternatePhone,
            String address,
            boolean available24x7,
            int priority) {}

    public record HouseholdContact(String name, String relationship, String mobile, int priority) {}

    public record ResidentSpace(
            UUID residentId,
            String residentName,
            String residentMobile,
            UUID spaceId,
            String spaceNumber,
            String block,
            List<HouseholdContact> emergencyContacts) {}

    public record Responder(UUID id, String name, String mobile) {}

    public record Workspace(
            List<IncidentView> incidents,
            List<DirectoryContact> directory,
            List<ResidentSpace> residentOptions,
            List<Responder> responderOptions,
            int total,
            long open,
            long critical,
            long responding,
            long resolved) {}

    public record HistoryEntry(
            UUID id,
            EmergencyEventType eventType,
            EmergencyIncidentStatus fromStatus,
            EmergencyIncidentStatus toStatus,
            String note,
            UserRole actorRole,
            OffsetDateTime occurredAt,
            String actorName) {}
}
