package com.societyapp.application.command;

import com.societyapp.application.exception.*;
import com.societyapp.application.mapper.EmergencyViewMapper;
import com.societyapp.application.model.emergency.EmergencyModels;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.time.OffsetDateTime;
import java.util.*;

public final class EmergencyLifecycleCoordinator {
    private final EmergencyIncidentRepository incidents;
    private final EmergencyIncidentEventRepository events;
    private final UserAccountJpaRepository users;
    private final StaffPermissionRepository permissions;
    private final NotificationOutboxRepository notifications;
    private final EmergencyViewMapper views;

    public EmergencyLifecycleCoordinator(
            EmergencyIncidentRepository incidents,
            EmergencyIncidentEventRepository events,
            UserAccountJpaRepository users,
            StaffPermissionRepository permissions,
            NotificationOutboxRepository notifications,
            EmergencyViewMapper views) {
        this.incidents = incidents;
        this.events = events;
        this.users = users;
        this.permissions = permissions;
        this.notifications = notifications;
        this.views = views;
    }

    public EmergencyModels.IncidentView action(
            EmergencyModels.Actor actor, UUID incidentId, EmergencyModels.ActionCommand command) {
        var item = requireIncident(actor.societyId(), incidentId);
        var requested = command.action();
        String note = command.note();
        UUID assignedStaffId = command.assignedStaffId();
        String hospitalName = command.hospitalName();
        String externalReference = command.externalReference();
        String old = item.getStatus();
        switch (requested) {
            case ACKNOWLEDGE -> {
                requireManager(actor.userId(), actor.role());
                requireStatus(item, EmergencyIncidentStatus.REPORTED);
                item.setStatus(EmergencyIncidentStatus.ACKNOWLEDGED.code());
                item.setAcknowledgedAt(OffsetDateTime.now());
            }
            case ASSIGN -> {
                requireManager(actor.userId(), actor.role());
                requireOpen(item);
                if (assignedStaffId == null)
                    throw new IllegalArgumentException("Responder is required");
                var responder = requireStaff(actor.societyId(), assignedStaffId);
                item.setAssignedStaffId(assignedStaffId);
                if (EmergencyIncidentStatus.REPORTED.matches(item.getStatus())) {
                    item.setStatus(EmergencyIncidentStatus.ACKNOWLEDGED.code());
                    item.setAcknowledgedAt(OffsetDateTime.now());
                }
                notifyUser(responder.getId(), item, "EMERGENCY_ASSIGNED");
            }
            case START_RESPONSE -> {
                requireManager(actor.userId(), actor.role());
                requireOneOf(
                        item,
                        "Incident is not awaiting response",
                        EmergencyIncidentStatus.REPORTED,
                        EmergencyIncidentStatus.ACKNOWLEDGED);
                item.setStatus(EmergencyIncidentStatus.RESPONDING.code());
                item.setResponseStartedAt(OffsetDateTime.now());
                if (actor.role() == UserRole.STAFF && item.getAssignedStaffId() == null)
                    item.setAssignedStaffId(actor.userId());
                if (item.getAcknowledgedAt() == null) item.setAcknowledgedAt(OffsetDateTime.now());
            }
            case ARRIVE -> {
                requireManager(actor.userId(), actor.role());
                requireStatus(item, EmergencyIncidentStatus.RESPONDING);
                item.setStatus(EmergencyIncidentStatus.ON_SCENE.code());
                item.setArrivedAt(OffsetDateTime.now());
            }
            case REQUEST_AMBULANCE, REQUEST_POLICE, REQUEST_FIRE -> {
                requireManager(actor.userId(), actor.role());
                requireOpen(item);
                requireNote(note, "Dispatch details or call reference are required");
                if (requested == EmergencyAction.REQUEST_AMBULANCE)
                    item.setAmbulanceRequested(true);
                if (requested == EmergencyAction.REQUEST_POLICE) item.setPoliceRequested(true);
                if (requested == EmergencyAction.REQUEST_FIRE) item.setFireServiceRequested(true);
                item.setExternalReference(blank(externalReference));
                item.setHospitalName(blank(hospitalName));
            }
            case NOTIFY_CONTACT -> {
                requireManager(actor.userId(), actor.role());
                requireOpen(item);
                requireNote(note, "Contact notification details are required");
                item.setEmergencyContactNotified(true);
                if (item.getResidentId() != null)
                    notifyUser(item.getResidentId(), item, "EMERGENCY_CONTACT_NOTIFIED");
            }
            case ADD_UPDATE -> {
                requireVisible(item, actor.userId(), actor.role());
                requireOpen(item);
                requireNote(note, "Update note is required");
            }
            case ESCALATE -> {
                requireManager(actor.userId(), actor.role());
                requireOpen(item);
                var severity = command.severity();
                if (severity == null)
                    throw new IllegalArgumentException("A valid escalation severity is required");
                if (severity == IncidentSeverity.LOW)
                    throw new IllegalArgumentException("A valid escalation severity is required");
                if (severity.rank() <= IncidentSeverity.from(item.getSeverity()).rank())
                    throw new IllegalArgumentException("New severity must be higher");
                item.setSeverity(severity.code());
            }
            case STABILIZE -> {
                requireManager(actor.userId(), actor.role());
                requireOneOf(
                        item,
                        "Incident response is not on scene",
                        EmergencyIncidentStatus.RESPONDING,
                        EmergencyIncidentStatus.ON_SCENE);
                requireNote(note, "Stabilization details are required");
                item.setStatus(EmergencyIncidentStatus.STABILIZED.code());
                item.setStabilizedAt(OffsetDateTime.now());
            }
            case RESOLVE -> {
                requireManager(actor.userId(), actor.role());
                requireOpen(item);
                requireNote(note, "Resolution summary is required");
                item.setStatus(EmergencyIncidentStatus.RESOLVED.code());
                item.setResolutionSummary(note.trim());
                item.setResolvedAt(OffsetDateTime.now());
                notifyRelevant(item, "EMERGENCY_RESOLVED");
            }
            case CLOSE -> {
                requireAdmin(actor.role());
                requireStatus(item, EmergencyIncidentStatus.RESOLVED);
                item.setStatus(EmergencyIncidentStatus.CLOSED.code());
                item.setClosedReason(blank(note));
                item.setClosedAt(OffsetDateTime.now());
            }
            case CANCEL -> {
                if (actor.role() == UserRole.RESIDENT)
                    requireResidentOwner(item, actor.userId(), actor.role());
                else requireManager(actor.userId(), actor.role());
                requireOneOf(
                        item,
                        "Incident response has already started",
                        EmergencyIncidentStatus.REPORTED,
                        EmergencyIncidentStatus.ACKNOWLEDGED);
                requireNote(note, "Cancellation reason is required");
                item.setStatus(EmergencyIncidentStatus.CANCELLED.code());
                item.setClosedReason(note.trim());
                item.setClosedAt(OffsetDateTime.now());
            }
            case FALSE_ALARM -> {
                requireManager(actor.userId(), actor.role());
                requireOpen(item);
                requireNote(note, "False alarm reason is required");
                item.setStatus(EmergencyIncidentStatus.FALSE_ALARM.code());
                item.setClosedReason(note.trim());
                item.setClosedAt(OffsetDateTime.now());
            }
            case REOPEN -> {
                requireAdmin(actor.role());
                requireOneOf(
                        item,
                        "Incident is already open",
                        EmergencyIncidentStatus.RESOLVED,
                        EmergencyIncidentStatus.CLOSED,
                        EmergencyIncidentStatus.CANCELLED,
                        EmergencyIncidentStatus.FALSE_ALARM);
                requireNote(note, "Reopen reason is required");
                item.setStatus(EmergencyIncidentStatus.RESPONDING.code());
                item.setResponseStartedAt(OffsetDateTime.now());
                item.setResolvedAt(null);
                item.setClosedAt(null);
                item.setClosedReason(null);
            }
        }
        item.setUpdatedAt(OffsetDateTime.now());
        item = incidents.save(item);
        addEvent(
                item,
                actor.userId(),
                actor.role(),
                EmergencyEventType.from(requested),
                old,
                item.getStatus(),
                note);
        return views.incident(item);
    }

    private void requireOneOf(
            EmergencyIncident item, String message, EmergencyIncidentStatus... values) {
        if (Arrays.stream(values).noneMatch(v -> v.matches(item.getStatus())))
            throw new IllegalStateException(message);
    }

    private void requireOpen(EmergencyIncident item) {
        if (!EmergencyIncidentStatus.from(item.getStatus()).isOpen())
            throw new IllegalStateException("Only an open incident can be assigned");
    }

    private void requireStatus(EmergencyIncident item, EmergencyIncidentStatus status) {
        if (!status.matches(item.getStatus()))
            throw new IllegalStateException(
                    "Incident must be " + status.code().toLowerCase(Locale.ROOT));
    }

    private void requireNote(String note, String message) {
        if (note == null || note.isBlank()) throw new IllegalArgumentException(message);
    }

    private void requireVisible(EmergencyIncident item, UUID actor, UserRole role) {
        if (role == UserRole.RESIDENT) requireResidentOwner(item, actor, role);
        else requireManager(actor, role);
    }

    private void requireResidentOwner(EmergencyIncident item, UUID actor, UserRole role) {
        if (role != UserRole.RESIDENT || !actor.equals(item.getResidentId()))
            throw new AuthorizationException("You can only manage your own emergency incidents");
    }

    private void requireManager(UUID actor, UserRole role) {
        if (role == UserRole.SOCIETY_ADMIN) return;
        if (role == UserRole.STAFF
                && permissions.existsByStaffUserIdAndPermissionCode(
                        actor, StaffPermissionCode.EMERGENCY_INCIDENTS.code())) return;
        throw new AuthorizationException("Emergency incidents permission is required");
    }

    private void requireAdmin(UserRole role) {
        if (role != UserRole.SOCIETY_ADMIN)
            throw new AuthorizationException("Society administrator access is required");
    }

    private EmergencyIncident requireIncident(UUID society, UUID id) {
        return incidents
                .findByIdAndSocietyId(id, society)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency incident not found"));
    }

    private UserAccount requireStaff(UUID society, UUID id) {
        return users.findById(id)
                .filter(u -> society.equals(u.getSocietyId()))
                .filter(u -> UserRole.STAFF.matches(u.getRole()))
                .filter(u -> AccountStatus.ACTIVE.matches(u.getStatus()))
                .filter(
                        u ->
                                permissions.existsByStaffUserIdAndPermissionCode(
                                        id, StaffPermissionCode.EMERGENCY_INCIDENTS.code()))
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Eligible emergency responder not found"));
    }

    private void notifyRelevant(EmergencyIncident item, String template) {
        notifyUser(item.getReporterId(), item, template);
        if (item.getResidentId() != null && !item.getResidentId().equals(item.getReporterId()))
            notifyUser(item.getResidentId(), item, template);
    }

    private void notifyUser(UUID id, EmergencyIncident item, String template) {
        users.findById(id).ifPresent(user -> notify(user, item, template));
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
            UUID actor,
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
        event.setNote(blank(note));
        event.setActorId(actor);
        event.setActorRole(role.code());
        event.setOccurredAt(OffsetDateTime.now());
        events.save(event);
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
