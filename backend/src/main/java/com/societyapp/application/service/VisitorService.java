package com.societyapp.application.service;

import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.model.visitor.VisitorModels;
import com.societyapp.application.port.in.StructureAccessUseCase;
import com.societyapp.application.port.in.VisitorUseCase;
import com.societyapp.application.port.out.DocumentStorage;
import com.societyapp.application.port.out.UploadedFile;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.query.VisitorQueryCoordinator;
import com.societyapp.application.usecase.*;
import com.societyapp.domain.model.Visitor;
import com.societyapp.domain.type.ApprovalAction;
import com.societyapp.domain.type.UserRole;
import com.societyapp.domain.type.VisitorStatus;
import com.societyapp.domain.type.VisitorTargetType;
import java.time.OffsetDateTime;
import java.util.*;

public class VisitorService implements VisitorUseCase {
    private final VisitorRepository visitors;
    private final StructureAccessUseCase access;
    private final UserAccountJpaRepository users;
    private final DocumentStorage storage;
    private final VisitorPhotoRepository visitorPhotos;
    private final VisitorQueryCoordinator queries;

    public VisitorService(
            VisitorRepository visitors,
            FlatRepository flats,
            FloorRepository floors,
            BlockRepository blocks,
            StructureAccessUseCase access,
            UserAccountJpaRepository users,
            DocumentStorage storage,
            VisitorPhotoRepository visitorPhotos,
            StaffPermissionRepository staffPermissions) {
        this.visitors = visitors;
        this.access = access;
        this.users = users;
        this.storage = storage;
        this.visitorPhotos = visitorPhotos;
        this.queries =
                new VisitorQueryCoordinator(
                        visitors,
                        flats,
                        floors,
                        blocks,
                        access,
                        users,
                        visitorPhotos,
                        staffPermissions);
    }

    public List<VisitorModels.VisitorView> list(UUID societyId, VisitorModels.Actor actor) {
        return queries.list(societyId, actor);
    }

    public VisitorModels.PermissionView permissions(VisitorModels.Actor actor) {
        return queries.permissions(actor);
    }

    public VisitorModels.MemberOptions memberOptions(UUID societyId, UUID residentId) {
        return queries.memberOptions(societyId, residentId);
    }

    public void requireGate(VisitorModels.Actor actor) {
        queries.requireGate(actor);
    }

    public List<VisitorModels.HostOption> hosts(UUID societyId) {
        return queries.hosts(societyId);
    }

    public List<VisitorModels.TargetOption> targets(UUID societyId) {
        return queries.targets(societyId);
    }

    public VisitorModels.Mutation create(
            UUID societyId, VisitorModels.Actor actor, VisitorModels.CreateCommand command) {
        UUID flatId = command.spaceId();
        UUID hostResidentId = command.hostResidentId();
        UUID targetUserId = command.targetUserId();
        String targetName = command.targetName();
        VisitorTargetType requestedTargetType = command.targetType();
        if (actor.role() == UserRole.STAFF) requireGate(actor);
        if (actor.role() == UserRole.RESIDENT) {
            requestedTargetType = VisitorTargetType.RESIDENT;
            hostResidentId = actor.userId();
            targetUserId = actor.userId();
        }
        requestedTargetType =
                requestedTargetType == null ? VisitorTargetType.RESIDENT : requestedTargetType;
        String targetType = requestedTargetType.code();
        com.societyapp.domain.model.UserAccount host = null;
        if (UserRole.RESIDENT.matches(targetType)) {
            if (flatId == null || hostResidentId == null) {
                throw new IllegalArgumentException("Resident and space are required");
            }
            access.requireFlat(flatId, societyId);
            host =
                    users.findById(hostResidentId)
                            .filter(user -> societyId.equals(user.getSocietyId()))
                            .filter(user -> UserRole.RESIDENT.matches(user.getRole()))
                            .orElseThrow(
                                    () -> new ResourceNotFoundException("Host resident not found"));
            if (!access.occupiedBy(flatId, hostResidentId)) {
                throw new IllegalArgumentException(
                        "Host resident does not occupy the selected space");
            }
            targetUserId = hostResidentId;
            targetName = host.getFullName();
        } else if (UserRole.STAFF.matches(targetType)) {
            String requiredRole = requestedTargetType.requiredRole().code();
            var target =
                    users.findById(targetUserId)
                            .filter(user -> societyId.equals(user.getSocietyId()))
                            .filter(user -> requiredRole.equals(user.getRole()))
                            .orElseThrow(
                                    () -> new ResourceNotFoundException("Visit target not found"));
            targetName = target.getFullName();
            flatId = null;
            hostResidentId = null;
        } else {
            if (targetName == null || targetName.isBlank()) {
                throw new IllegalArgumentException("Person or destination name is required");
            }
            targetUserId = null;
            flatId = null;
            hostResidentId = null;
        }
        boolean residentPreApproval =
                host != null
                        && actor.role() == UserRole.RESIDENT
                        && actor.userId().equals(host.getId());
        var visitor = new Visitor();
        visitor.setSocietyId(societyId);
        visitor.setFlatId(flatId);
        visitor.setVisitorName(command.visitorName());
        visitor.setMobile(command.mobile());
        visitor.setPurpose(command.purpose());
        visitor.setVehicleNumber(command.vehicleNumber());
        visitor.setExpectedAt(command.expectedAt());
        visitor.setHostResidentId(hostResidentId);
        visitor.setTargetType(targetType);
        visitor.setTargetUserId(targetUserId);
        visitor.setTargetName(targetName);
        visitor.setAccompanyingMembers(command.accompanyingMembers());
        visitor.setApprovedBy(residentPreApproval ? actor.userId() : null);
        visitor.setResidentDecisionAt(residentPreApproval ? OffsetDateTime.now() : null);
        visitor.setStatus(
                residentPreApproval
                        ? VisitorStatus.APPROVED.code()
                        : UserRole.RESIDENT.matches(targetType)
                                ? VisitorStatus.PENDING_RESIDENT_APPROVAL.code()
                                : VisitorStatus.PENDING_STAFF_APPROVAL.code());
        visitor.setCreatedAt(OffsetDateTime.now());
        visitor = visitors.save(visitor);
        return mutation(visitor);
    }

    public VisitorModels.PhotoView uploadPhoto(
            UUID societyId, VisitorModels.Actor actor, UUID visitorId, UploadedFile file) {
        requireGate(actor);
        var visitor =
                visitors.findByIdAndSocietyId(visitorId, societyId)
                        .orElseThrow(() -> new ResourceNotFoundException("Visitor not found"));
        var photo = new com.societyapp.domain.model.VisitorPhoto();
        photo.setVisitorId(visitorId);
        photo.setStorageKey(storage.storeImage(societyId, file));
        photo.setContentType(file.contentType());
        photo.setCreatedAt(OffsetDateTime.now());
        photo = visitorPhotos.save(photo);
        storage.attach(photo.getStorageKey(), "VISITOR_PHOTO", photo.getId(), "Visitor photo");
        return new VisitorModels.PhotoView(
                photo.getId(), visitorId, "/api/visitors/photos/" + photo.getId());
    }

    public VisitorModels.PhotoContent photo(
            UUID societyId, VisitorModels.Actor actor, UUID photoId) {
        var photo =
                visitorPhotos
                        .findById(photoId)
                        .filter(
                                item ->
                                        visitors.findByIdAndSocietyId(
                                                        item.getVisitorId(), societyId)
                                                .filter(visitor -> queries.canView(visitor, actor))
                                                .isPresent())
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Visitor photo not found"));
        return new VisitorModels.PhotoContent(
                storage.load(photo.getStorageKey()), photo.getContentType());
    }

    public VisitorModels.Mutation residentDecision(
            UUID societyId,
            UUID residentId,
            UUID visitorId,
            VisitorModels.DecisionCommand command) {
        var visitor =
                visitors.findByIdAndSocietyId(visitorId, societyId)
                        .filter(item -> residentId.equals(item.getHostResidentId()))
                        .filter(
                                item ->
                                        VisitorStatus.PENDING_RESIDENT_APPROVAL.matches(
                                                item.getStatus()))
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Visitor is not awaiting this resident's approval"));
        boolean approved = command.decision() == ApprovalAction.APPROVE;
        if (approved && visitorPhotos.countByVisitorId(visitorId) == 0) {
            throw new IllegalStateException("Visitor photo is required before approval");
        }
        visitor.setStatus(approved ? VisitorStatus.APPROVED.code() : VisitorStatus.DENIED.code());
        visitor.setApprovedBy(approved ? residentId : null);
        visitor.setResidentDecisionAt(OffsetDateTime.now());
        visitor.setApprovalMethod("RESIDENT_APP");
        visitor.setApprovalNote(
                command.note() == null || command.note().isBlank()
                        ? approved ? "Approved by resident" : "Denied by resident"
                        : command.note().trim());
        visitors.save(visitor);
        return mutation(visitor);
    }

    public VisitorModels.Mutation residentCancel(
            UUID societyId, UUID residentId, UUID visitorId, String note) {
        var visitor =
                visitors.findByIdAndSocietyId(visitorId, societyId)
                        .filter(item -> residentId.equals(item.getHostResidentId()))
                        .filter(
                                item ->
                                        Set.of(
                                                        VisitorStatus.PENDING_RESIDENT_APPROVAL
                                                                .code(),
                                                        VisitorStatus.APPROVED.code())
                                                .contains(item.getStatus()))
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Only your pending or approved visitor can be cancelled"));
        visitor.setStatus(VisitorStatus.CANCELLED.code());
        visitor.setResidentDecisionAt(OffsetDateTime.now());
        visitor.setApprovalMethod("RESIDENT_CANCELLED");
        visitor.setApprovalNote(
                note == null || note.isBlank() ? "Cancelled by resident" : note.trim());
        visitors.save(visitor);
        return mutation(visitor);
    }

    public VisitorModels.Mutation staffApproval(
            UUID societyId, VisitorModels.Actor actor, UUID visitorId, String note) {
        requireGate(actor);
        var visitor =
                visitors.findByIdAndSocietyId(visitorId, societyId)
                        .filter(
                                item ->
                                        Set.of(
                                                        VisitorStatus.PENDING_RESIDENT_APPROVAL
                                                                .code(),
                                                        VisitorStatus.PENDING_STAFF_APPROVAL.code())
                                                .contains(item.getStatus()))
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Visitor is not awaiting approval"));
        if (visitorPhotos.countByVisitorId(visitorId) == 0) {
            throw new IllegalStateException("Visitor photo is required before approval");
        }
        visitor.setStatus(VisitorStatus.APPROVED.code());
        visitor.setStaffApprovedBy(actor.userId());
        visitor.setApprovalMethod("PHONE_VERIFIED");
        visitor.setApprovalNote(note);
        visitor.setResidentDecisionAt(OffsetDateTime.now());
        visitors.save(visitor);
        return mutation(visitor);
    }

    public VisitorModels.Mutation targetStaffDecision(
            UUID societyId, UUID staffId, UUID visitorId, VisitorModels.DecisionCommand command) {
        var visitor =
                visitors.findByIdAndSocietyId(visitorId, societyId)
                        .filter(item -> staffId.equals(item.getTargetUserId()))
                        .filter(item -> UserRole.STAFF.matches(item.getTargetType()))
                        .filter(
                                item ->
                                        VisitorStatus.PENDING_STAFF_APPROVAL.matches(
                                                item.getStatus()))
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Visitor is not awaiting this staff member's approval"));
        if (visitorPhotos.countByVisitorId(visitorId) == 0) {
            throw new IllegalStateException("Visitor photo is required before approval");
        }
        boolean approved = command.decision() == ApprovalAction.APPROVE;
        visitor.setStatus(approved ? VisitorStatus.APPROVED.code() : VisitorStatus.DENIED.code());
        visitor.setStaffApprovedBy(staffId);
        visitor.setApprovalMethod("STAFF_SELF");
        visitor.setApprovalNote(
                approved ? "Approved by staff member being visited" : "Denied by staff member");
        visitor.setResidentDecisionAt(OffsetDateTime.now());
        visitors.save(visitor);
        return mutation(visitor);
    }

    public VisitorModels.Mutation updateStatus(
            UUID societyId,
            VisitorModels.Actor actor,
            UUID visitorId,
            VisitorStatus requestedStatus) {
        requireGate(actor);
        var visitor =
                visitors.findByIdAndSocietyId(visitorId, societyId)
                        .orElseThrow(() -> new ResourceNotFoundException("Visitor not found"));
        if (requestedStatus != VisitorStatus.CHECKED_IN
                && requestedStatus != VisitorStatus.CHECKED_OUT) {
            throw new IllegalArgumentException("Invalid visitor gate status");
        }
        if (requestedStatus == VisitorStatus.CHECKED_IN) {
            if (!VisitorStatus.APPROVED.matches(visitor.getStatus())) {
                throw new IllegalStateException("Visitor approval is required before check-in");
            }
            if (visitorPhotos.countByVisitorId(visitorId) == 0) {
                throw new IllegalStateException("Visitor photo is required before check-in");
            }
        }
        if (requestedStatus == VisitorStatus.CHECKED_OUT
                && !VisitorStatus.CHECKED_IN.matches(visitor.getStatus())) {
            throw new IllegalStateException("Only checked-in visitors can be checked out");
        }
        visitor.setStatus(requestedStatus.code());
        if (requestedStatus == VisitorStatus.CHECKED_IN) {
            visitor.setCheckedInBy(actor.userId());
            visitor.setCheckedInAt(OffsetDateTime.now());
        } else {
            visitor.setCheckedOutAt(OffsetDateTime.now());
        }
        visitors.save(visitor);
        return mutation(visitor);
    }

    private VisitorModels.Mutation mutation(Visitor visitor) {
        return new VisitorModels.Mutation(visitor.getId(), VisitorStatus.from(visitor.getStatus()));
    }
}
