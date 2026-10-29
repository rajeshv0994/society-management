package com.societyapp.application.query;

import com.societyapp.application.exception.AuthorizationException;
import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.mapper.EmergencyViewMapper;
import com.societyapp.application.model.emergency.EmergencyModels;
import com.societyapp.application.port.in.StructureAccessUseCase;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.util.*;

public final class EmergencyQueryCoordinator {
    private final EmergencyIncidentRepository incidents;
    private final EmergencyIncidentEventRepository events;
    private final EmergencyDirectoryContactRepository directory;
    private final UserAccountJpaRepository users;
    private final FlatRepository flats;
    private final FloorRepository floors;
    private final BlockRepository blocks;
    private final StructureAccessUseCase access;
    private final StaffPermissionRepository permissions;
    private final HouseholdEmergencyContactRepository householdContacts;
    private final EmergencyViewMapper views;

    public EmergencyQueryCoordinator(
            EmergencyIncidentRepository incidents,
            EmergencyIncidentEventRepository events,
            EmergencyDirectoryContactRepository directory,
            UserAccountJpaRepository users,
            FlatRepository flats,
            FloorRepository floors,
            BlockRepository blocks,
            StructureAccessUseCase access,
            StaffPermissionRepository permissions,
            HouseholdEmergencyContactRepository householdContacts,
            EmergencyViewMapper views) {
        this.incidents = incidents;
        this.events = events;
        this.directory = directory;
        this.users = users;
        this.flats = flats;
        this.floors = floors;
        this.blocks = blocks;
        this.access = access;
        this.permissions = permissions;
        this.householdContacts = householdContacts;
        this.views = views;
    }

    public EmergencyModels.Workspace workspace(EmergencyModels.Actor actor) {
        if (actor.role() == UserRole.STAFF) requireManager(actor.userId(), actor.role());
        var rows =
                actor.role() == UserRole.RESIDENT
                        ? incidents.findBySocietyIdAndResidentIdOrderByCreatedAtDesc(
                                actor.societyId(), actor.userId())
                        : incidents.findBySocietyIdOrderByCreatedAtDesc(actor.societyId());
        return new EmergencyModels.Workspace(
                rows.stream().map(views::incident).toList(),
                directory
                        .findBySocietyIdAndActiveTrueOrderByContactTypeAscPriorityAsc(
                                actor.societyId())
                        .stream()
                        .map(views::directory)
                        .toList(),
                residentOptions(actor),
                responderOptions(actor.societyId()),
                rows.size(),
                rows.stream()
                        .filter(row -> EmergencyIncidentStatus.from(row.getStatus()).isOpen())
                        .count(),
                rows.stream()
                        .filter(row -> IncidentSeverity.CRITICAL.matches(row.getSeverity()))
                        .filter(row -> EmergencyIncidentStatus.from(row.getStatus()).isOpen())
                        .count(),
                rows.stream()
                        .filter(
                                row ->
                                        Set.of(
                                                        EmergencyIncidentStatus.RESPONDING,
                                                        EmergencyIncidentStatus.ON_SCENE,
                                                        EmergencyIncidentStatus.STABILIZED)
                                                .contains(
                                                        EmergencyIncidentStatus.from(
                                                                row.getStatus())))
                        .count(),
                rows.stream()
                        .filter(
                                row ->
                                        Set.of(
                                                        EmergencyIncidentStatus.RESOLVED,
                                                        EmergencyIncidentStatus.CLOSED)
                                                .contains(
                                                        EmergencyIncidentStatus.from(
                                                                row.getStatus())))
                        .count());
    }

    public List<EmergencyModels.HistoryEntry> history(
            EmergencyModels.Actor actor, UUID incidentId) {
        if (actor.role() == UserRole.STAFF) requireManager(actor.userId(), actor.role());
        var incident =
                incidents
                        .findByIdAndSocietyId(incidentId, actor.societyId())
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Emergency incident not found"));
        if (actor.role() == UserRole.RESIDENT) {
            if (!actor.userId().equals(incident.getResidentId()))
                throw new AuthorizationException(
                        "You can only manage your own emergency incidents");
        } else requireManager(actor.userId(), actor.role());
        return events.findByIncidentIdOrderByOccurredAt(incidentId).stream()
                .map(views::event)
                .toList();
    }

    private List<EmergencyModels.ResidentSpace> residentOptions(EmergencyModels.Actor actor) {
        var result = new ArrayList<EmergencyModels.ResidentSpace>();
        var residents =
                actor.role() == UserRole.RESIDENT
                        ? List.of(requireResident(actor.societyId(), actor.userId()))
                        : users
                                .findBySocietyIdAndRoleOrderByFullName(
                                        actor.societyId(), UserRole.RESIDENT.code())
                                .stream()
                                .filter(user -> AccountStatus.ACTIVE.matches(user.getStatus()))
                                .toList();
        for (var resident : residents)
            for (var flat : flats.findAll()) {
                if (!access.belongsToSociety(flat.getId(), actor.societyId())
                        || !access.occupiedBy(flat.getId(), resident.getId())) continue;
                var block =
                        floors.findById(flat.getFloorId())
                                .flatMap(floor -> blocks.findById(floor.getBlockId()))
                                .map(Block::getName)
                                .orElse(null);
                var contacts =
                        householdContacts
                                .findBySocietyIdAndPrimaryResidentIdAndActiveTrueOrderByPriority(
                                        actor.societyId(), resident.getId())
                                .stream()
                                .map(
                                        contact ->
                                                new EmergencyModels.HouseholdContact(
                                                        contact.getFullName(),
                                                        contact.getRelationship(),
                                                        contact.getMobile(),
                                                        contact.getPriority()))
                                .toList();
                result.add(
                        new EmergencyModels.ResidentSpace(
                                resident.getId(),
                                resident.getFullName(),
                                resident.getMobile(),
                                flat.getId(),
                                flat.getFlatNumber(),
                                block,
                                contacts));
            }
        return result;
    }

    private List<EmergencyModels.Responder> responderOptions(UUID societyId) {
        return users
                .findBySocietyIdAndRoleOrderByFullName(societyId, UserRole.STAFF.code())
                .stream()
                .filter(user -> AccountStatus.ACTIVE.matches(user.getStatus()))
                .filter(
                        user ->
                                permissions.existsByStaffUserIdAndPermissionCode(
                                        user.getId(),
                                        StaffPermissionCode.EMERGENCY_INCIDENTS.code()))
                .map(
                        user ->
                                new EmergencyModels.Responder(
                                        user.getId(),
                                        user.getFullName(),
                                        user.getMobile() == null ? "" : user.getMobile()))
                .toList();
    }

    private UserAccount requireResident(UUID societyId, UUID id) {
        return users.findById(id)
                .filter(user -> societyId.equals(user.getSocietyId()))
                .filter(user -> UserRole.RESIDENT.matches(user.getRole()))
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found"));
    }

    private void requireManager(UUID actorId, UserRole role) {
        if (role == UserRole.SOCIETY_ADMIN) return;
        if (role == UserRole.STAFF
                && permissions.existsByStaffUserIdAndPermissionCode(
                        actorId, StaffPermissionCode.EMERGENCY_INCIDENTS.code())) return;
        throw new AuthorizationException("Emergency incidents permission is required");
    }
}
