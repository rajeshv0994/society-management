package com.societyapp.controller.mapper;

import com.societyapp.application.model.structure.StructureModels;
import com.societyapp.controller.model.StructureResponse;

public final class StructureHttpMapper {
    public StructureResponse.View toResponse(StructureModels.View view) {
        var blocks = view.blocks().stream().map(this::block).toList();
        var sections = view.sections().stream().map(this::section).toList();
        var spaces = view.spaces().stream().map(this::space).toList();
        return new StructureResponse.View(blocks, sections, sections, spaces, spaces, spaces);
    }

    public StructureResponse.Created toResponse(StructureModels.Created result) {
        return new StructureResponse.Created(result.id());
    }

    public StructureResponse.BlockUpdated toResponse(StructureModels.BlockUpdated result) {
        return new StructureResponse.BlockUpdated(result.id(), result.name());
    }

    public StructureResponse.SectionUpdated toResponse(StructureModels.SectionUpdated result) {
        return new StructureResponse.SectionUpdated(
                result.id(), result.name(), result.sectionOrder(), result.sectionOrder());
    }

    public StructureResponse.SpaceUpdated toResponse(StructureModels.SpaceUpdated result) {
        return new StructureResponse.SpaceUpdated(
                result.id(), result.spaceNumber(), result.spaceNumber(), result.spaceNumber());
    }

    public StructureResponse.Deleted toResponse(StructureModels.Deleted result) {
        return new StructureResponse.Deleted(result.id(), result.deleted());
    }

    private StructureResponse.Block block(StructureModels.Block block) {
        return new StructureResponse.Block(
                block.id(),
                block.name(),
                block.sectionCount(),
                block.sectionCount(),
                block.spaceCount(),
                block.spaceCount(),
                block.spaceCount(),
                block.occupiedCount());
    }

    private StructureResponse.Section section(StructureModels.Section section) {
        return new StructureResponse.Section(
                section.id(),
                section.blockId(),
                section.name(),
                section.sectionOrder(),
                section.sectionOrder(),
                section.block(),
                section.spaceCount(),
                section.spaceCount(),
                section.spaceCount());
    }

    private StructureResponse.Space space(StructureModels.Space space) {
        return new StructureResponse.Space(
                space.id(),
                space.sectionId(),
                space.sectionId(),
                space.block(),
                space.section(),
                space.section(),
                space.spaceNumber(),
                space.spaceNumber(),
                space.spaceNumber(),
                space.occupancyStatus().code(),
                space.areaSqft());
    }
}
