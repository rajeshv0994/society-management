package com.societyapp.application.service;

import com.societyapp.application.exception.AuthorizationException;
import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.mapper.ComplaintViewMapper;
import com.societyapp.application.model.complaint.ComplaintModels;
import com.societyapp.application.port.in.ComplaintUseCase;
import com.societyapp.application.port.in.StructureAccessUseCase;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.usecase.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.AccountStatus;
import com.societyapp.domain.type.ComplaintStatus;
import com.societyapp.domain.type.StaffPermissionCode;
import com.societyapp.domain.type.UserRole;
import com.societyapp.domain.type.WorkPriority;
import java.time.OffsetDateTime;
import java.util.*;

public class ComplaintService implements ComplaintUseCase {
    private final ComplaintRepository complaints;
    private final ComplaintUpdateRepository updates;
    private final ComplaintCategoryRepository categories;
    private final UserAccountJpaRepository users;
    private final StaffPermissionRepository permissions;
    private final FlatRepository flats;
    private final FloorRepository floors;
    private final BlockRepository blocks;
    private final StructureAccessUseCase structure;
    private final ComplaintViewMapper viewMapper;
    private final ComplaintTimelineWriter timelineWriter;

    public ComplaintService(
            ComplaintRepository complaints,
            ComplaintUpdateRepository updates,
            ComplaintCategoryRepository categories,
            UserAccountJpaRepository users,
            StaffPermissionRepository permissions,
            FlatRepository flats,
            FloorRepository floors,
            BlockRepository blocks,
            StructureAccessUseCase structure) {
        this.complaints = complaints;
        this.updates = updates;
        this.categories = categories;
        this.users = users;
        this.permissions = permissions;
        this.flats = flats;
        this.floors = floors;
        this.blocks = blocks;
        this.structure = structure;
        this.viewMapper =
                new ComplaintViewMapper(users, categories, flats, floors, blocks, structure);
        this.timelineWriter = new ComplaintTimelineWriter(updates);
    }

    @Override
    public List<ComplaintModels.View> list(ComplaintModels.Actor actor) {
        UUID societyId = actor.societyId();
        UUID userId = actor.userId();
        String role = actor.role().code();
        if (UserRole.STAFF.matches(role)) requireStaffPermission(userId);
        return complaints.findBySocietyIdOrderByCreatedAtDesc(societyId).stream()
                .filter(
                        complaint ->
                                switch (UserRole.from(role)) {
                                    case RESIDENT ->
                                            userId.equals(complaint.getResidentId())
                                                    || (complaint.getResidentId() == null
                                                            && userId.equals(
                                                                    complaint.getRaisedBy()));
                                    case STAFF ->
                                            userId.equals(complaint.getAssignedTo())
                                                    || userId.equals(complaint.getCreatedBy());
                                    default -> true;
                                })
                .map(viewMapper::complaint)
                .toList();
    }

    @Override
    public ComplaintModels.Options options(ComplaintModels.Actor actor) {
        UUID societyId = actor.societyId();
        UUID userId = actor.userId();
        String role = actor.role().code();
        if (UserRole.STAFF.matches(role)) requireStaffPermission(userId);
        var categoryOptions =
                categories.findBySocietyIdOrderByName(societyId).stream()
                        .filter(ComplaintCategory::isActive)
                        .map(
                                category ->
                                        new ComplaintModels.Category(
                                                category.getId(),
                                                category.getName(),
                                                category.getSlaHours()))
                        .toList();
        if (UserRole.RESIDENT.matches(role)) {
            return new ComplaintModels.Options(
                    userId,
                    categoryOptions,
                    List.of(),
                    List.of(),
                    viewMapper.residentSpaces(societyId, userId));
        }
        var staff =
                users
                        .findBySocietyIdAndRoleOrderByFullName(societyId, UserRole.STAFF.code())
                        .stream()
                        .filter(user -> AccountStatus.ACTIVE.matches(user.getStatus()))
                        .filter(
                                user ->
                                        permissions.existsByStaffUserIdAndPermissionCode(
                                                user.getId(),
                                                StaffPermissionCode.COMPLAINTS.code()))
                        .map(
                                user ->
                                        new ComplaintModels.Staff(
                                                user.getId(), user.getFullName(), user.getMobile()))
                        .toList();
        return new ComplaintModels.Options(
                userId, categoryOptions, viewMapper.residentOptions(societyId), staff, List.of());
    }

    @Override
    public ComplaintModels.Detail detail(ComplaintModels.Actor actor, UUID id) {
        var complaint = requireVisible(actor.societyId(), actor.userId(), actor.role().code(), id);
        return new ComplaintModels.Detail(
                viewMapper.complaint(complaint),
                updates.findByComplaintIdOrderByCreatedAt(id).stream()
                        .map(viewMapper::update)
                        .toList());
    }

    @Override
    public ComplaintModels.Status create(
            ComplaintModels.Actor actor, ComplaintModels.CreateCommand command) {
        UUID societyId = actor.societyId();
        UUID actorId = actor.userId();
        String role = actor.role().code();
        UUID residentId = command.residentId();
        UUID flatId = command.spaceId();
        UUID categoryId = command.categoryId();
        UUID assignedTo = command.assignedTo();
        String title = command.title();
        String description = command.description();
        String priority = command.priority().code();
        if (UserRole.STAFF.matches(role)) requireStaffPermission(actorId);
        if (UserRole.STAFF.matches(role) && assignedTo != null && !actorId.equals(assignedTo)) {
            throw new AuthorizationException("Staff can only assign a complaint to themselves");
        }
        UUID targetResident = UserRole.RESIDENT.matches(role) ? actorId : residentId;
        if (targetResident == null) {
            throw new IllegalArgumentException("Resident is required");
        }
        var resident =
                users.findById(targetResident)
                        .filter(user -> societyId.equals(user.getSocietyId()))
                        .filter(user -> UserRole.RESIDENT.matches(user.getRole()))
                        .orElseThrow(() -> new ResourceNotFoundException("Resident not found"));
        UUID targetFlat = resolveResidentFlat(societyId, resident.getId(), flatId);
        ComplaintCategory category = null;
        if (categoryId != null) {
            category =
                    categories
                            .findById(categoryId)
                            .filter(item -> societyId.equals(item.getSocietyId()))
                            .filter(ComplaintCategory::isActive)
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Complaint category not found"));
        }
        if (assignedTo != null) requireAssignableStaff(societyId, assignedTo);
        var now = OffsetDateTime.now();
        var complaint = new Complaint();
        complaint.setSocietyId(societyId);
        complaint.setRaisedBy(actorId);
        complaint.setCreatedBy(actorId);
        complaint.setResidentId(resident.getId());
        complaint.setFlatId(targetFlat);
        complaint.setCategoryId(categoryId);
        complaint.setTitle(title);
        complaint.setDescription(description);
        complaint.setPriority(
                WorkPriority.from(priority == null ? WorkPriority.NORMAL.code() : priority).code());
        complaint.setAssignedTo(assignedTo);
        complaint.setStatus(
                assignedTo == null ? ComplaintStatus.OPEN.code() : ComplaintStatus.ASSIGNED.code());
        complaint.setDueAt(
                category == null ? now.plusHours(48) : now.plusHours(category.getSlaHours()));
        complaint.setCreatedAt(now);
        complaint.setUpdatedAt(now);
        complaint = complaints.save(complaint);
        timelineWriter.record(
                complaint.getId(),
                actorId,
                "CREATED",
                null,
                complaint.getStatus(),
                "Complaint created for " + resident.getFullName());
        return new ComplaintModels.Status(
                complaint.getId(), ComplaintStatus.from(complaint.getStatus()));
    }

    @Override
    public ComplaintModels.Status update(
            ComplaintModels.Actor actor, UUID id, ComplaintModels.UpdateCommand command) {
        UUID societyId = actor.societyId();
        UUID actorId = actor.userId();
        String role = actor.role().code();
        String status = command.status() == null ? null : command.status().code();
        UUID assignedTo = command.assignedTo();
        String note = command.note();
        String resolution = command.resolution();
        var complaint = requireVisible(societyId, actorId, role, id);
        if (UserRole.RESIDENT.matches(role)) {
            throw new AuthorizationException("Residents can add comments but cannot change status");
        }
        if (UserRole.STAFF.matches(role) && assignedTo != null && !actorId.equals(assignedTo)) {
            throw new AuthorizationException("Staff can only assign a complaint to themselves");
        }
        var previous = ComplaintStatus.from(complaint.getStatus());
        UUID previousAssignee = complaint.getAssignedTo();
        if (assignedTo != null) {
            requireAssignableStaff(societyId, assignedTo);
            complaint.setAssignedTo(assignedTo);
            if ((status == null || ComplaintStatus.OPEN.matches(status))
                    && previous == ComplaintStatus.OPEN) {
                status = ComplaintStatus.ASSIGNED.code();
            }
        }
        var next = status == null ? previous : ComplaintStatus.from(status);
        validateTransition(previous, next);
        String effectiveResolution =
                resolution == null || resolution.isBlank() ? complaint.getResolution() : resolution;
        if (next.isTerminal() && (effectiveResolution == null || effectiveResolution.isBlank())) {
            throw new IllegalArgumentException("Resolution is required to resolve or close");
        }
        boolean assignmentChanged = !Objects.equals(previousAssignee, complaint.getAssignedTo());
        boolean statusChanged = previous != next;
        boolean hasNote = note != null && !note.isBlank();
        boolean hasResolution = resolution != null && !resolution.isBlank();
        if (!assignmentChanged && !statusChanged && !hasNote && !hasResolution) {
            throw new IllegalArgumentException("Add a progress note or change the complaint");
        }
        complaint.setStatus(next.code());
        if (hasResolution) complaint.setResolution(resolution);
        complaint.setUpdatedAt(OffsetDateTime.now());
        complaint.setClosedAt(next.isTerminal() ? OffsetDateTime.now() : null);
        complaints.save(complaint);
        String event =
                statusChanged
                        ? "STATUS_CHANGED"
                        : assignmentChanged ? "ASSIGNMENT_CHANGED" : "PROGRESS_NOTE";
        if (assignmentChanged && !hasNote) {
            note = "Complaint assigned to " + assignedToName(complaint.getAssignedTo());
        }
        timelineWriter.record(id, actorId, event, previous.code(), next.code(), note);
        return new ComplaintModels.Status(id, next);
    }

    @Override
    public ComplaintModels.Commented comment(ComplaintModels.Actor actor, UUID id, String note) {
        UUID societyId = actor.societyId();
        UUID actorId = actor.userId();
        String role = actor.role().code();
        requireVisible(societyId, actorId, role, id);
        timelineWriter.record(id, actorId, "COMMENT", null, null, note);
        var complaint = complaints.findByIdAndSocietyId(id, societyId).orElseThrow();
        complaint.setUpdatedAt(OffsetDateTime.now());
        complaints.save(complaint);
        return new ComplaintModels.Commented(id, true);
    }

    private Complaint requireVisible(UUID societyId, UUID userId, String role, UUID id) {
        if (UserRole.STAFF.matches(role)) requireStaffPermission(userId);
        return complaints
                .findByIdAndSocietyId(id, societyId)
                .filter(
                        complaint ->
                                switch (UserRole.from(role)) {
                                    case RESIDENT ->
                                            userId.equals(complaint.getResidentId())
                                                    || (complaint.getResidentId() == null
                                                            && userId.equals(
                                                                    complaint.getRaisedBy()));
                                    case STAFF ->
                                            userId.equals(complaint.getAssignedTo())
                                                    || userId.equals(complaint.getCreatedBy());
                                    default -> true;
                                })
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));
    }

    private void requireStaffPermission(UUID staffId) {
        if (!permissions.existsByStaffUserIdAndPermissionCode(
                staffId, StaffPermissionCode.COMPLAINTS.code())) {
            throw new AuthorizationException("Complaints permission is required");
        }
    }

    private void requireAssignableStaff(UUID societyId, UUID staffId) {
        users.findById(staffId)
                .filter(user -> societyId.equals(user.getSocietyId()))
                .filter(user -> UserRole.STAFF.matches(user.getRole()))
                .filter(user -> AccountStatus.ACTIVE.matches(user.getStatus()))
                .filter(
                        user ->
                                permissions.existsByStaffUserIdAndPermissionCode(
                                        user.getId(), StaffPermissionCode.COMPLAINTS.code()))
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Active staff with Complaints permission not found"));
    }

    private String assignedToName(UUID staffId) {
        if (staffId == null) return "Unassigned";
        return users.findById(staffId).map(UserAccount::getFullName).orElse("Staff");
    }

    private UUID resolveResidentFlat(UUID societyId, UUID residentId, UUID requestedFlat) {
        if (requestedFlat != null) {
            structure.requireFlat(requestedFlat, societyId);
            if (!structure.occupiedBy(requestedFlat, residentId)) {
                throw new IllegalArgumentException("Resident does not occupy the selected space");
            }
            return requestedFlat;
        }
        return flats.findAll().stream()
                .filter(flat -> structure.belongsToSociety(flat.getId(), societyId))
                .filter(
                        flat ->
                                residentId.equals(flat.getActiveOwnerId())
                                        || residentId.equals(flat.getActiveTenantId()))
                .map(Flat::getId)
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Resident is not assigned to a space"));
    }

    private void validateTransition(ComplaintStatus from, ComplaintStatus to) {
        if (from == to) return;
        if (!from.allowedTransitions().contains(to)) {
            throw new IllegalStateException("Invalid complaint status transition");
        }
    }
}
