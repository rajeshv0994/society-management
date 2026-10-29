package com.societyapp.application.mapper;

import com.societyapp.application.model.complaint.ComplaintModels;
import com.societyapp.application.port.in.StructureAccessUseCase;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.time.OffsetDateTime;
import java.util.*;

public class ComplaintViewMapper {
    private final UserAccountJpaRepository users;
    private final ComplaintCategoryRepository categories;
    private final FlatRepository flats;
    private final FloorRepository floors;
    private final BlockRepository blocks;
    private final StructureAccessUseCase structure;

    public ComplaintViewMapper(
            UserAccountJpaRepository users,
            ComplaintCategoryRepository categories,
            FlatRepository flats,
            FloorRepository floors,
            BlockRepository blocks,
            StructureAccessUseCase structure) {
        this.users = users;
        this.categories = categories;
        this.flats = flats;
        this.floors = floors;
        this.blocks = blocks;
        this.structure = structure;
    }

    public List<ComplaintModels.ResidentOption> residentOptions(UUID societyId) {
        var result = new ArrayList<ComplaintModels.ResidentOption>();
        for (var resident :
                users.findBySocietyIdAndRoleOrderByFullName(societyId, UserRole.RESIDENT.code())) {
            if (!AccountStatus.ACTIVE.matches(resident.getStatus())) continue;
            for (var space : residentSpaces(societyId, resident.getId())) {
                result.add(
                        new ComplaintModels.ResidentOption(
                                resident.getId(),
                                resident.getFullName(),
                                resident.getMobile(),
                                space.spaceId(),
                                space.spaceNumber(),
                                space.block()));
            }
        }
        return List.copyOf(result);
    }

    public List<ComplaintModels.Space> residentSpaces(UUID societyId, UUID residentId) {
        return flats.findAll().stream()
                .filter(flat -> structure.belongsToSociety(flat.getId(), societyId))
                .filter(
                        flat ->
                                residentId.equals(flat.getActiveOwnerId())
                                        || residentId.equals(flat.getActiveTenantId()))
                .map(
                        flat ->
                                new ComplaintModels.Space(
                                        flat.getId(),
                                        flat.getFlatNumber(),
                                        floors.findById(flat.getFloorId())
                                                .flatMap(
                                                        floor ->
                                                                blocks.findById(floor.getBlockId()))
                                                .map(Block::getName)
                                                .orElse(null)))
                .toList();
    }

    public ComplaintModels.View complaint(Complaint item) {
        String spaceNumber = null;
        String blockName = null;
        if (item.getFlatId() != null) {
            var flat = flats.findById(item.getFlatId()).orElse(null);
            if (flat != null) {
                spaceNumber = flat.getFlatNumber();
                blockName =
                        floors.findById(flat.getFloorId())
                                .flatMap(floor -> blocks.findById(floor.getBlockId()))
                                .map(Block::getName)
                                .orElse(null);
            }
        }
        return new ComplaintModels.View(
                item.getId(),
                "CMP-" + item.getId().toString().substring(0, 8).toUpperCase(),
                item.getResidentId(),
                item.getFlatId(),
                item.getCategoryId(),
                item.getTitle(),
                item.getDescription(),
                WorkPriority.from(item.getPriority()),
                ComplaintStatus.from(item.getStatus()),
                item.getAssignedTo(),
                item.getResolution(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getDueAt(),
                item.getClosedAt(),
                item.getDueAt() != null
                        && item.getDueAt().isBefore(OffsetDateTime.now())
                        && !ComplaintStatus.from(item.getStatus()).isTerminal(),
                item.getResidentId() == null
                        ? null
                        : users.findById(item.getResidentId())
                                .map(UserAccount::getFullName)
                                .orElse(null),
                users.findById(item.getCreatedBy()).map(UserAccount::getFullName).orElse(null),
                item.getAssignedTo() == null
                        ? null
                        : users.findById(item.getAssignedTo())
                                .map(UserAccount::getFullName)
                                .orElse(null),
                item.getCategoryId() == null
                        ? "General"
                        : categories
                                .findById(item.getCategoryId())
                                .map(ComplaintCategory::getName)
                                .orElse("General"),
                spaceNumber,
                blockName);
    }

    public ComplaintModels.TimelineEntry update(ComplaintUpdate update) {
        return new ComplaintModels.TimelineEntry(
                update.getId(),
                update.getEventType(),
                status(update.getFromStatus()),
                status(update.getToStatus()),
                update.getNote(),
                update.getCreatedAt(),
                users.findById(update.getActorUserId()).map(UserAccount::getFullName).orElse(null));
    }

    private ComplaintStatus status(String value) {
        return value == null ? null : ComplaintStatus.from(value);
    }
}
