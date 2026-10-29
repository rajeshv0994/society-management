package com.societyapp.application.query;

import com.societyapp.application.exception.AuthorizationException;
import com.societyapp.application.model.visitor.VisitorModels;
import com.societyapp.application.port.in.StructureAccessUseCase;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.Visitor;
import com.societyapp.domain.type.*;
import java.util.*;

public class VisitorQueryCoordinator {
    private final VisitorRepository visitors;
    private final FlatRepository flats;
    private final FloorRepository floors;
    private final BlockRepository blocks;
    private final StructureAccessUseCase access;
    private final UserAccountJpaRepository users;
    private final VisitorPhotoRepository photos;
    private final StaffPermissionRepository permissions;

    public VisitorQueryCoordinator(
            VisitorRepository visitors,
            FlatRepository flats,
            FloorRepository floors,
            BlockRepository blocks,
            StructureAccessUseCase access,
            UserAccountJpaRepository users,
            VisitorPhotoRepository photos,
            StaffPermissionRepository permissions) {
        this.visitors = visitors;
        this.flats = flats;
        this.floors = floors;
        this.blocks = blocks;
        this.access = access;
        this.users = users;
        this.photos = photos;
        this.permissions = permissions;
    }

    public List<VisitorModels.VisitorView> list(UUID societyId, VisitorModels.Actor actor) {
        boolean gateStaff = actor.role() == UserRole.STAFF && canManageGate(actor.userId());
        return visitors.findBySocietyIdOrderByCreatedAtDesc(societyId).stream()
                .filter(visitor -> canList(visitor, actor, gateStaff))
                .map(this::view)
                .toList();
    }

    private boolean canList(Visitor visitor, VisitorModels.Actor actor, boolean gateStaff) {
        return switch (actor.role()) {
            case RESIDENT ->
                    actor.userId().equals(visitor.getHostResidentId())
                            || (visitor.getHostResidentId() == null
                                    && visitor.getFlatId() != null
                                    && access.occupiedBy(visitor.getFlatId(), actor.userId()));
            case STAFF -> gateStaff || actor.userId().equals(visitor.getTargetUserId());
            default -> true;
        };
    }

    public VisitorModels.PermissionView permissions(VisitorModels.Actor actor) {
        return new VisitorModels.PermissionView(
                actor.userId(), actor.role() != UserRole.STAFF || canManageGate(actor.userId()));
    }

    public VisitorModels.MemberOptions memberOptions(UUID societyId, UUID residentId) {
        var spaces =
                flats.findAll().stream()
                        .filter(
                                flat ->
                                        access.belongsToSociety(flat.getId(), societyId)
                                                && access.occupiedBy(flat.getId(), residentId))
                        .map(
                                flat ->
                                        new VisitorModels.SpaceOption(
                                                flat.getId(),
                                                flat.getFlatNumber(),
                                                blockName(flat.getFloorId())))
                        .toList();
        return new VisitorModels.MemberOptions(residentId, spaces);
    }

    public List<VisitorModels.HostOption> hosts(UUID societyId) {
        var names = new HashMap<UUID, String>();
        users.findBySocietyIdAndRoleOrderByFullName(societyId, UserRole.RESIDENT.code()).stream()
                .filter(user -> AccountStatus.ACTIVE.matches(user.getStatus()))
                .forEach(user -> names.put(user.getId(), user.getFullName()));
        var result = new ArrayList<VisitorModels.HostOption>();
        for (var flat : flats.findAll()) {
            if (!access.belongsToSociety(flat.getId(), societyId)) continue;
            for (UUID residentId : new UUID[] {flat.getActiveOwnerId(), flat.getActiveTenantId()}) {
                if (residentId == null || !names.containsKey(residentId)) continue;
                String mobile =
                        users.findById(residentId).map(user -> user.getMobile()).orElse(null);
                result.add(
                        new VisitorModels.HostOption(
                                residentId,
                                names.get(residentId),
                                mobile,
                                flat.getId(),
                                flat.getFlatNumber(),
                                blockName(flat.getFloorId())));
            }
        }
        result.sort(Comparator.comparing(VisitorModels.HostOption::fullName));
        return result;
    }

    public List<VisitorModels.TargetOption> targets(UUID societyId) {
        return users.findAll().stream()
                .filter(user -> societyId.equals(user.getSocietyId()))
                .filter(user -> AccountStatus.ACTIVE.matches(user.getStatus()))
                .filter(user -> UserRole.STAFF.matches(user.getRole()))
                .sorted(Comparator.comparing(user -> user.getFullName().toLowerCase()))
                .map(
                        user ->
                                new VisitorModels.TargetOption(
                                        user.getId(),
                                        user.getFullName(),
                                        UserRole.from(user.getRole()),
                                        user.getMobile()))
                .toList();
    }

    public VisitorModels.VisitorView view(Visitor visitor) {
        var urls =
                photos.findByVisitorIdOrderByCreatedAt(visitor.getId()).stream()
                        .map(photo -> "/api/visitors/photos/" + photo.getId())
                        .toList();
        String hostName =
                visitor.getHostResidentId() == null
                        ? null
                        : users.findById(visitor.getHostResidentId())
                                .map(user -> user.getFullName())
                                .orElse(null);
        String targetMobile =
                visitor.getTargetUserId() == null
                        ? null
                        : users.findById(visitor.getTargetUserId())
                                .map(user -> user.getMobile())
                                .orElse(null);
        String spaceNumber = null;
        String block = null;
        if (visitor.getFlatId() != null) {
            var flat = flats.findById(visitor.getFlatId()).orElse(null);
            if (flat != null) {
                spaceNumber = flat.getFlatNumber();
                block = blockName(flat.getFloorId());
            }
        }
        return new VisitorModels.VisitorView(
                visitor.getId(),
                visitor.getSocietyId(),
                visitor.getFlatId(),
                visitor.getVisitorName(),
                visitor.getMobile(),
                visitor.getPurpose(),
                visitor.getVehicleNumber(),
                visitor.getExpectedAt(),
                visitor.getApprovedBy(),
                visitor.getHostResidentId(),
                hostName,
                VisitorTargetType.from(visitor.getTargetType()),
                visitor.getTargetUserId(),
                visitor.getTargetName(),
                targetMobile,
                visitor.getAccompanyingMembers(),
                visitor.getAccompanyingMembers() + 1,
                urls,
                visitor.getResidentDecisionAt(),
                visitor.getStaffApprovedBy(),
                approvalMethod(visitor.getApprovalMethod()),
                visitor.getApprovalNote(),
                visitor.getCheckedInBy(),
                visitor.getCheckedInAt(),
                visitor.getCheckedOutAt(),
                VisitorStatus.from(visitor.getStatus()),
                visitor.getCreatedAt(),
                spaceNumber,
                block);
    }

    private VisitorApprovalMethod approvalMethod(String value) {
        return value == null ? null : VisitorApprovalMethod.from(value);
    }

    private String blockName(UUID floorId) {
        return floors.findById(floorId)
                .flatMap(floor -> blocks.findById(floor.getBlockId()))
                .map(block -> block.getName())
                .orElse(null);
    }

    public boolean canManageGate(UUID staffId) {
        return permissions.existsByStaffUserIdAndPermissionCode(
                staffId, StaffPermissionCode.VISITORS.code());
    }

    public void requireGate(VisitorModels.Actor actor) {
        if (actor.role() == UserRole.SOCIETY_ADMIN) return;
        if (actor.role() != UserRole.STAFF || !canManageGate(actor.userId()))
            throw new AuthorizationException("Visitor Gate permission is required");
    }

    public boolean canView(Visitor visitor, VisitorModels.Actor actor) {
        return switch (actor.role()) {
            case RESIDENT ->
                    actor.userId().equals(visitor.getHostResidentId())
                            || (visitor.getHostResidentId() == null
                                    && visitor.getFlatId() != null
                                    && access.occupiedBy(visitor.getFlatId(), actor.userId()));
            case STAFF ->
                    canManageGate(actor.userId())
                            || actor.userId().equals(visitor.getTargetUserId());
            default -> true;
        };
    }
}
