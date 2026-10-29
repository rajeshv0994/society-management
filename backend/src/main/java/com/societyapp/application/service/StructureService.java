package com.societyapp.application.service;

import com.societyapp.application.exception.ConflictException;
import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.model.structure.StructureModels;
import com.societyapp.application.port.in.StructureUseCase;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.usecase.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.FlatOccupancyStatus;
import com.societyapp.domain.type.LifecycleStatus;
import java.math.BigDecimal;
import java.util.*;

public class StructureService implements StructureUseCase {
    private final BlockRepository blocks;
    private final FloorRepository floors;
    private final FlatRepository flats;
    private final SocietyRepository societies;
    private final SubscriptionPlanRepository plans;
    private final SocietyCustomPlanRepository customPlans;

    public StructureService(
            BlockRepository blocks,
            FloorRepository floors,
            FlatRepository flats,
            SocietyRepository societies,
            SubscriptionPlanRepository plans,
            SocietyCustomPlanRepository customPlans) {
        this.blocks = blocks;
        this.floors = floors;
        this.flats = flats;
        this.societies = societies;
        this.plans = plans;
        this.customPlans = customPlans;
    }

    @Override
    public StructureModels.View get(UUID societyId) {
        var blockViews = new ArrayList<StructureModels.Block>();
        var sectionViews = new ArrayList<StructureModels.Section>();
        var spaceViews = new ArrayList<StructureModels.Space>();
        for (var block : blocks.findBySocietyIdOrderByName(societyId)) {
            var blockSections = floors.findByBlockIdOrderByFloorNumber(block.getId());
            var blockSpaces =
                    blockSections.stream()
                            .flatMap(
                                    section ->
                                            flats
                                                    .findByFloorIdOrderByFlatNumber(section.getId())
                                                    .stream())
                            .toList();
            blockViews.add(
                    new StructureModels.Block(
                            block.getId(),
                            block.getName(),
                            blockSections.size(),
                            blockSpaces.size(),
                            blockSpaces.stream()
                                    .filter(
                                            space ->
                                                    !FlatOccupancyStatus.VACANT.matches(
                                                            space.getOccupancyStatus()))
                                    .count()));
            for (var section : blockSections) {
                var sectionSpaces = flats.findByFloorIdOrderByFlatNumber(section.getId());
                sectionViews.add(
                        new StructureModels.Section(
                                section.getId(),
                                block.getId(),
                                section.getName(),
                                section.getFloorNumber(),
                                block.getName(),
                                sectionSpaces.size()));
                for (var space : sectionSpaces) {
                    spaceViews.add(
                            new StructureModels.Space(
                                    space.getId(),
                                    section.getId(),
                                    block.getName(),
                                    section.getName(),
                                    space.getFlatNumber(),
                                    FlatOccupancyStatus.from(space.getOccupancyStatus()),
                                    space.getAreaSqft()));
                }
            }
        }
        return new StructureModels.View(blockViews, sectionViews, spaceViews);
    }

    @Override
    public StructureModels.Created addBlock(UUID societyId, StructureModels.BlockCommand command) {
        String name = command.name();
        var block = new Block();
        block.setSocietyId(societyId);
        block.setName(name);
        return new StructureModels.Created(blocks.save(block).getId());
    }

    @Override
    public StructureModels.BlockUpdated updateBlock(
            UUID societyId, UUID blockId, StructureModels.BlockCommand command) {
        String name = command.name();
        var block = requireBlock(societyId, blockId);
        block.setName(name.trim());
        blocks.save(block);
        return new StructureModels.BlockUpdated(blockId, block.getName());
    }

    @Override
    public StructureModels.Deleted deleteBlock(UUID societyId, UUID blockId) {
        var block = requireBlock(societyId, blockId);
        if (!floors.findByBlockIdOrderByFloorNumber(blockId).isEmpty()) {
            throw new ConflictException("Remove all sections before deleting this group");
        }
        blocks.delete(block);
        return new StructureModels.Deleted(blockId, true);
    }

    @Override
    public StructureModels.Created addSection(
            UUID societyId, UUID blockId, StructureModels.SectionCommand command) {
        String name = command.name();
        int floorNumber = command.sectionOrder();
        if (!blocks.existsByIdAndSocietyId(blockId, societyId)) {
            throw new ResourceNotFoundException("Block not found");
        }
        var floor = new Floor();
        floor.setBlockId(blockId);
        floor.setName(name);
        floor.setFloorNumber(floorNumber);
        return new StructureModels.Created(floors.save(floor).getId());
    }

    @Override
    public StructureModels.SectionUpdated updateSection(
            UUID societyId, UUID floorId, StructureModels.SectionCommand command) {
        String name = command.name();
        int floorNumber = command.sectionOrder();
        var floor = requireFloor(societyId, floorId);
        floor.setName(name.trim());
        floor.setFloorNumber(floorNumber);
        floors.save(floor);
        return new StructureModels.SectionUpdated(floorId, floor.getName(), floorNumber);
    }

    @Override
    public StructureModels.Deleted deleteSection(UUID societyId, UUID floorId) {
        var floor = requireFloor(societyId, floorId);
        if (!flats.findByFloorIdOrderByFlatNumber(floorId).isEmpty()) {
            throw new ConflictException("Remove all spaces before deleting this section");
        }
        floors.delete(floor);
        return new StructureModels.Deleted(floorId, true);
    }

    @Override
    public StructureModels.Created addSpace(
            UUID societyId, UUID floorId, StructureModels.SpaceCommand command) {
        String flatNumber = command.spaceNumber();
        BigDecimal areaSqft = command.areaSqft();
        var floor = requireFloor(societyId, floorId);
        ensurePlanCapacity(societyId);
        var flat = new Flat();
        flat.setFloorId(floor.getId());
        flat.setFlatNumber(flatNumber.trim());
        flat.setOccupancyStatus(FlatOccupancyStatus.VACANT.code());
        flat.setAreaSqft(areaSqft);
        return new StructureModels.Created(flats.save(flat).getId());
    }

    @Override
    public StructureModels.Created addSpace(UUID societyId, StructureModels.SpaceCommand command) {
        String spaceNumber = command.spaceNumber();
        BigDecimal areaSqft = command.areaSqft();
        var block =
                blocks.findBySocietyIdOrderByName(societyId).stream()
                        .findFirst()
                        .orElseGet(
                                () -> {
                                    var created = new Block();
                                    created.setSocietyId(societyId);
                                    created.setName("General");
                                    return blocks.save(created);
                                });
        var section =
                floors.findByBlockIdOrderByFloorNumber(block.getId()).stream()
                        .findFirst()
                        .orElseGet(
                                () -> {
                                    var created = new Floor();
                                    created.setBlockId(block.getId());
                                    created.setName("General");
                                    created.setFloorNumber(0);
                                    return floors.save(created);
                                });
        return addSpace(societyId, section.getId(), command);
    }

    @Override
    public StructureModels.SpaceUpdated updateSpace(
            UUID societyId, UUID flatId, StructureModels.SpaceCommand command) {
        String flatNumber = command.spaceNumber();
        BigDecimal areaSqft = command.areaSqft();
        var flat = requireFlat(societyId, flatId);
        flat.setFlatNumber(flatNumber.trim());
        flat.setAreaSqft(areaSqft);
        flats.save(flat);
        return new StructureModels.SpaceUpdated(flatId, flat.getFlatNumber());
    }

    @Override
    public StructureModels.Deleted deleteSpace(UUID societyId, UUID flatId) {
        var flat = requireFlat(societyId, flatId);
        if (!FlatOccupancyStatus.VACANT.matches(flat.getOccupancyStatus())
                || flat.getActiveOwnerId() != null
                || flat.getActiveTenantId() != null) {
            throw new ConflictException("Only vacant, unassigned spaces can be deleted");
        }
        flats.delete(flat);
        return new StructureModels.Deleted(flatId, true);
    }

    private Block requireBlock(UUID societyId, UUID blockId) {
        return blocks.findById(blockId)
                .filter(block -> societyId.equals(block.getSocietyId()))
                .orElseThrow(() -> new ResourceNotFoundException("Block not found"));
    }

    private Floor requireFloor(UUID societyId, UUID floorId) {
        return floors.findById(floorId)
                .filter(floor -> blocks.existsByIdAndSocietyId(floor.getBlockId(), societyId))
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));
    }

    private Flat requireFlat(UUID societyId, UUID flatId) {
        return flats.findById(flatId)
                .filter(
                        flat ->
                                floors.findById(flat.getFloorId())
                                        .filter(
                                                floor ->
                                                        blocks.existsByIdAndSocietyId(
                                                                floor.getBlockId(), societyId))
                                        .isPresent())
                .orElseThrow(() -> new ResourceNotFoundException("Space not found"));
    }

    private void ensurePlanCapacity(UUID societyId) {
        var society =
                societies
                        .findById(societyId)
                        .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
        Integer maximum =
                plans.findById(society.getPlanCode())
                        .map(SubscriptionPlan::getMaxFlats)
                        .orElse(null);
        var customMaximum =
                customPlans.findAllByOrderByCreatedAtDesc().stream()
                        .filter(plan -> societyId.equals(plan.getSocietyId()))
                        .filter(plan -> LifecycleStatus.ACTIVE.matches(plan.getStatus()))
                        .map(SocietyCustomPlan::getMaxFlats)
                        .findFirst()
                        .orElse(null);
        if (customMaximum != null) maximum = customMaximum;
        int purchasedSpaces = society.getTotalFlats();
        int allowed = maximum == null ? purchasedSpaces : Math.min(purchasedSpaces, maximum);
        long configuredSpaces =
                blocks.findBySocietyIdOrderByName(societyId).stream()
                        .flatMap(
                                block ->
                                        floors
                                                .findByBlockIdOrderByFloorNumber(block.getId())
                                                .stream())
                        .mapToLong(
                                section ->
                                        flats.findByFloorIdOrderByFlatNumber(section.getId())
                                                .size())
                        .sum();
        if (configuredSpaces >= allowed) {
            throw new ConflictException(
                    "Your subscription includes "
                            + allowed
                            + " spaces. Increase the selected space quantity first.");
        }
    }
}
