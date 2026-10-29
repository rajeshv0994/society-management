package com.societyapp.application.mapper;

import com.societyapp.application.model.emergency.EmergencyModels;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;

public final class EmergencyViewMapper {
    private final UserAccountJpaRepository users;
    private final FlatRepository flats;
    private final FloorRepository floors;
    private final BlockRepository blocks;

    public EmergencyViewMapper(
            UserAccountJpaRepository users,
            FlatRepository flats,
            FloorRepository floors,
            BlockRepository blocks) {
        this.users = users;
        this.flats = flats;
        this.floors = floors;
        this.blocks = blocks;
    }

    public EmergencyModels.IncidentView incident(EmergencyIncident item) {
        var reporter = users.findById(item.getReporterId()).orElse(null);
        var resident =
                item.getResidentId() == null
                        ? null
                        : users.findById(item.getResidentId()).orElse(null);
        var staff =
                item.getAssignedStaffId() == null
                        ? null
                        : users.findById(item.getAssignedStaffId()).orElse(null);
        var flat = item.getFlatId() == null ? null : flats.findById(item.getFlatId()).orElse(null);
        var block =
                flat == null
                        ? null
                        : floors.findById(flat.getFloorId())
                                .flatMap(floor -> blocks.findById(floor.getBlockId()))
                                .map(Block::getName)
                                .orElse(null);
        return new EmergencyModels.IncidentView(
                item.getId(),
                item.getIncidentNumber(),
                EmergencyIncidentType.from(item.getIncidentType()),
                IncidentSeverity.from(item.getSeverity()),
                item.getTitle(),
                item.getDescription(),
                item.getLocationDetails(),
                item.getLatitude(),
                item.getLongitude(),
                item.getPeopleAffected(),
                item.getInjuredPersonName(),
                item.getInjuredPersonMobile(),
                item.getInjuryDetails(),
                EmergencyIncidentStatus.from(item.getStatus()),
                item.isEmergencyContactNotified(),
                item.isAmbulanceRequested(),
                item.isPoliceRequested(),
                item.isFireServiceRequested(),
                item.getHospitalName(),
                item.getExternalReference(),
                item.getAcknowledgedAt(),
                item.getResponseStartedAt(),
                item.getArrivedAt(),
                item.getStabilizedAt(),
                item.getResolvedAt(),
                item.getClosedAt(),
                item.getResolutionSummary(),
                item.getClosedReason(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getReporterId(),
                reporter == null ? null : reporter.getFullName(),
                item.getResidentId(),
                resident == null ? null : resident.getFullName(),
                resident == null ? null : resident.getMobile(),
                item.getAssignedStaffId(),
                staff == null ? null : staff.getFullName(),
                staff == null ? null : staff.getMobile(),
                item.getFlatId(),
                flat == null ? null : flat.getFlatNumber(),
                block);
    }

    public EmergencyModels.DirectoryContact directory(EmergencyDirectoryContact item) {
        return new EmergencyModels.DirectoryContact(
                item.getId(),
                EmergencyContactType.from(item.getContactType()),
                item.getName(),
                item.getPhone(),
                item.getAlternatePhone(),
                item.getAddress(),
                item.isAvailable24x7(),
                item.getPriority());
    }

    public EmergencyModels.HistoryEntry event(EmergencyIncidentEvent item) {
        return new EmergencyModels.HistoryEntry(
                item.getId(),
                EmergencyEventType.from(item.getEventType()),
                item.getFromStatus() == null
                        ? null
                        : EmergencyIncidentStatus.from(item.getFromStatus()),
                item.getToStatus() == null
                        ? null
                        : EmergencyIncidentStatus.from(item.getToStatus()),
                item.getNote(),
                UserRole.from(item.getActorRole()),
                item.getOccurredAt(),
                users.findById(item.getActorId()).map(UserAccount::getFullName).orElse(null));
    }
}
