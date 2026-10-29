package com.societyapp.controller.mapper;

import com.societyapp.application.model.emergency.EmergencyModels;
import com.societyapp.controller.model.EmergencyResponse;

public final class EmergencyHttpMapper {
    public EmergencyResponse.Workspace toResponse(EmergencyModels.Workspace value) {
        return new EmergencyResponse.Workspace(
                value.incidents().stream().map(this::toResponse).toList(),
                value.directory().stream().map(this::toResponse).toList(),
                value.residentOptions().stream().map(this::resident).toList(),
                value.responderOptions().stream()
                        .map(
                                item ->
                                        new EmergencyResponse.Responder(
                                                item.id(), item.name(), item.mobile()))
                        .toList(),
                value.total(),
                value.open(),
                value.critical(),
                value.responding(),
                value.resolved());
    }

    public EmergencyResponse.Incident toResponse(EmergencyModels.IncidentView value) {
        return new EmergencyResponse.Incident(
                value.id(),
                value.incidentNumber(),
                value.incidentType().code(),
                value.severity().code(),
                value.title(),
                value.description(),
                value.locationDetails(),
                value.latitude(),
                value.longitude(),
                value.peopleAffected(),
                value.injuredPersonName(),
                value.injuredPersonMobile(),
                value.injuryDetails(),
                value.status().code(),
                value.emergencyContactNotified(),
                value.ambulanceRequested(),
                value.policeRequested(),
                value.fireServiceRequested(),
                value.hospitalName(),
                value.externalReference(),
                value.acknowledgedAt(),
                value.responseStartedAt(),
                value.arrivedAt(),
                value.stabilizedAt(),
                value.resolvedAt(),
                value.closedAt(),
                value.resolutionSummary(),
                value.closedReason(),
                value.createdAt(),
                value.updatedAt(),
                value.reporterId(),
                value.reporterName(),
                value.residentId(),
                value.residentName(),
                value.residentMobile(),
                value.assignedStaffId(),
                value.assignedStaffName(),
                value.assignedStaffMobile(),
                value.spaceId(),
                value.spaceNumber(),
                value.block());
    }

    public EmergencyResponse.DirectoryContact toResponse(EmergencyModels.DirectoryContact value) {
        return new EmergencyResponse.DirectoryContact(
                value.id(),
                value.contactType().code(),
                value.name(),
                value.phone(),
                value.alternatePhone(),
                value.address(),
                value.available24x7(),
                value.priority());
    }

    public EmergencyResponse.HistoryEntry toResponse(EmergencyModels.HistoryEntry value) {
        return new EmergencyResponse.HistoryEntry(
                value.id(),
                value.eventType().code(),
                value.fromStatus() == null ? null : value.fromStatus().code(),
                value.toStatus() == null ? null : value.toStatus().code(),
                value.note(),
                value.actorRole().code(),
                value.occurredAt(),
                value.actorName());
    }

    private EmergencyResponse.ResidentSpace resident(EmergencyModels.ResidentSpace value) {
        return new EmergencyResponse.ResidentSpace(
                value.residentId(),
                value.residentName(),
                value.residentMobile(),
                value.spaceId(),
                value.spaceNumber(),
                value.block(),
                value.emergencyContacts().stream()
                        .map(
                                item ->
                                        new EmergencyResponse.HouseholdContact(
                                                item.name(),
                                                item.relationship(),
                                                item.mobile(),
                                                item.priority()))
                        .toList());
    }
}
