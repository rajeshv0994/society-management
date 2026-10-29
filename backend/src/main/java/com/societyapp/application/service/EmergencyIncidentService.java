package com.societyapp.application.service;

import com.societyapp.application.command.EmergencyLifecycleCoordinator;
import com.societyapp.application.exception.AuthorizationException;
import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.mapper.EmergencyViewMapper;
import com.societyapp.application.model.emergency.EmergencyModels;
import com.societyapp.application.port.in.EmergencyIncidentUseCase;
import com.societyapp.application.port.in.StructureAccessUseCase;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.query.EmergencyQueryCoordinator;
import com.societyapp.application.usecase.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.EmergencyEventType;
import com.societyapp.domain.type.EmergencyIncidentStatus;
import com.societyapp.domain.type.NotificationStatus;
import com.societyapp.domain.type.StaffPermissionCode;
import com.societyapp.domain.type.UserRole;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class EmergencyIncidentService implements EmergencyIncidentUseCase {
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
    private final NotificationOutboxRepository notifications;
    private final EmergencyViewMapper views;
    private final EmergencyQueryCoordinator queries;
    private final EmergencyLifecycleCoordinator lifecycle;

    public EmergencyIncidentService(
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
            NotificationOutboxRepository notifications) {
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
        this.notifications = notifications;
        this.views = new EmergencyViewMapper(users, flats, floors, blocks);
        this.queries =
                new EmergencyQueryCoordinator(
                        incidents,
                        events,
                        directory,
                        users,
                        flats,
                        floors,
                        blocks,
                        access,
                        permissions,
                        householdContacts,
                        views);
        this.lifecycle =
                new EmergencyLifecycleCoordinator(
                        incidents, events, users, permissions, notifications, views);
    }

    @Override
    public EmergencyModels.Workspace workspace(EmergencyModels.Actor actor) {
        return queries.workspace(actor);
    }

    @Override
    public List<EmergencyModels.HistoryEntry> history(
            EmergencyModels.Actor actor, UUID incidentId) {
        return queries.history(actor, incidentId);
    }

    @Override
    public EmergencyModels.IncidentView create(
            EmergencyModels.Actor actor, EmergencyModels.CreateCommand command) {
        UUID residentId = command.residentId();
        if (actor.role() == UserRole.RESIDENT) {
            residentId = actor.userId();
            if (command.spaceId() == null) throw new IllegalArgumentException("Space is required");
            access.requireFlat(command.spaceId(), actor.societyId());
            if (!access.occupiedBy(command.spaceId(), actor.userId())) {
                throw new IllegalArgumentException("You do not occupy the selected space");
            }
        } else {
            requireManager(actor.userId(), actor.role());
            if (residentId != null) requireResident(actor.societyId(), residentId);
            if (command.spaceId() != null) {
                access.requireFlat(command.spaceId(), actor.societyId());
                if (residentId != null && !access.occupiedBy(command.spaceId(), residentId)) {
                    throw new IllegalArgumentException(
                            "Resident does not occupy the selected space");
                }
            }
        }
        if (command.latitude() != null
                && (command.latitude().compareTo(BigDecimal.valueOf(-90)) < 0
                        || command.latitude().compareTo(BigDecimal.valueOf(90)) > 0)) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (command.longitude() != null
                && (command.longitude().compareTo(BigDecimal.valueOf(-180)) < 0
                        || command.longitude().compareTo(BigDecimal.valueOf(180)) > 0)) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
        var item = new EmergencyIncident();
        item.setSocietyId(actor.societyId());
        item.setIncidentNumber(incidentNumber());
        item.setReporterId(actor.userId());
        item.setReporterRole(actor.role().code());
        item.setResidentId(residentId);
        item.setFlatId(command.spaceId());
        item.setIncidentType(command.incidentType().code());
        item.setSeverity(command.severity().code());
        item.setTitle(command.title().trim());
        item.setDescription(command.description().trim());
        item.setLocationDetails(command.locationDetails().trim());
        item.setLatitude(command.latitude());
        item.setLongitude(command.longitude());
        item.setPeopleAffected(command.peopleAffected());
        item.setInjuredPersonName(blankToNull(command.injuredPersonName()));
        item.setInjuredPersonMobile(blankToNull(command.injuredPersonMobile()));
        item.setInjuryDetails(blankToNull(command.injuryDetails()));
        item.setStatus(EmergencyIncidentStatus.REPORTED.code());
        item.setCreatedAt(OffsetDateTime.now());
        item.setUpdatedAt(OffsetDateTime.now());
        item = incidents.save(item);
        addEvent(
                item,
                actor.userId(),
                actor.role(),
                EmergencyEventType.REPORTED,
                null,
                EmergencyIncidentStatus.REPORTED.code(),
                command.description());
        notifySocietyAdmins(actor.societyId(), item, "EMERGENCY_REPORTED");
        if (residentId != null && !actor.userId().equals(residentId)) {
            notifyUser(residentId, item, "EMERGENCY_REPORTED_FOR_RESIDENT");
        }
        return views.incident(item);
    }

    @Override
    public EmergencyModels.IncidentView action(
            EmergencyModels.Actor actor, UUID incidentId, EmergencyModels.ActionCommand command) {
        return lifecycle.action(actor, incidentId, command);
    }

    @Override
    public EmergencyModels.DirectoryContact saveDirectory(
            EmergencyModels.Actor actor, UUID id, EmergencyModels.DirectoryCommand command) {
        requireAdmin(actor.role());
        var item =
                id == null
                        ? new EmergencyDirectoryContact()
                        : directory
                                .findByIdAndSocietyId(id, actor.societyId())
                                .orElseThrow(
                                        () ->
                                                new ResourceNotFoundException(
                                                        "Emergency contact not found"));
        if (id == null) {
            item.setSocietyId(actor.societyId());
            item.setCreatedBy(actor.userId());
            item.setCreatedAt(OffsetDateTime.now());
        }
        item.setContactType(command.contactType().code());
        item.setName(command.name().trim());
        item.setPhone(command.phone().trim());
        item.setAlternatePhone(blankToNull(command.alternatePhone()));
        item.setAddress(blankToNull(command.address()));
        item.setAvailable24x7(command.available24x7());
        item.setPriority(command.priority());
        item.setActive(true);
        item.setUpdatedAt(OffsetDateTime.now());
        return views.directory(directory.save(item));
    }

    @Override
    public void removeDirectory(EmergencyModels.Actor actor, UUID id) {
        requireAdmin(actor.role());
        var item =
                directory
                        .findByIdAndSocietyId(id, actor.societyId())
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Emergency contact not found"));
        item.setActive(false);
        item.setUpdatedAt(OffsetDateTime.now());
        directory.save(item);
    }

    private void requireManager(UUID actorId, UserRole role) {
        if (role == UserRole.SOCIETY_ADMIN) return;
        if (role == UserRole.STAFF
                && permissions.existsByStaffUserIdAndPermissionCode(
                        actorId, StaffPermissionCode.EMERGENCY_INCIDENTS.code())) return;
        throw new AuthorizationException("Emergency incidents permission is required");
    }

    private void requireAdmin(UserRole role) {
        if (role != UserRole.SOCIETY_ADMIN) {
            throw new AuthorizationException("Society administrator access is required");
        }
    }

    private UserAccount requireResident(UUID societyId, UUID id) {
        return users.findById(id)
                .filter(user -> societyId.equals(user.getSocietyId()))
                .filter(user -> UserRole.RESIDENT.matches(user.getRole()))
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found"));
    }

    private void notifySocietyAdmins(UUID societyId, EmergencyIncident item, String template) {
        users.findBySocietyIdAndRoleOrderByFullName(societyId, UserRole.SOCIETY_ADMIN.code())
                .forEach(user -> notify(user, item, template));
    }

    private void notifyUser(UUID userId, EmergencyIncident item, String template) {
        users.findById(userId).ifPresent(user -> notify(user, item, template));
    }

    private void notify(UserAccount user, EmergencyIncident item, String template) {
        if (user.getEmail() == null || user.getEmail().isBlank()) return;
        var message = new NotificationOutbox();
        message.setSocietyId(item.getSocietyId());
        message.setChannel(com.societyapp.domain.type.NotificationChannel.EMAIL.code());
        message.setDestination(user.getEmail());
        message.setTemplateCode(template);
        message.setPayload(
                "{\"incidentNumber\":\""
                        + item.getIncidentNumber()
                        + "\",\"severity\":\""
                        + item.getSeverity()
                        + "\",\"title\":\""
                        + item.getTitle().replace("\"", "")
                        + "\"}");
        message.setStatus(NotificationStatus.PENDING.code());
        message.setAttempts(0);
        message.setCreatedAt(OffsetDateTime.now());
        notifications.save(message);
    }

    private void addEvent(
            EmergencyIncident item,
            UUID actorId,
            UserRole role,
            EmergencyEventType type,
            String from,
            String to,
            String note) {
        var event = new EmergencyIncidentEvent();
        event.setIncidentId(item.getId());
        event.setSocietyId(item.getSocietyId());
        event.setEventType(type.code());
        event.setFromStatus(from);
        event.setToStatus(to);
        event.setNote(blankToNull(note));
        event.setActorId(actorId);
        event.setActorRole(role.code());
        event.setOccurredAt(OffsetDateTime.now());
        events.save(event);
    }

    private String incidentNumber() {
        return "EMG-"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"))
                + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
