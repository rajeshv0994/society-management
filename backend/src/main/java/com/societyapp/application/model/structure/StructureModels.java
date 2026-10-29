package com.societyapp.application.model.structure;

import com.societyapp.domain.type.FlatOccupancyStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class StructureModels {
    private StructureModels() {}

    public record Block(
            UUID id, String name, int sectionCount, int spaceCount, long occupiedCount) {}

    public record Section(
            UUID id, UUID blockId, String name, int sectionOrder, String block, int spaceCount) {}

    public record Space(
            UUID id,
            UUID sectionId,
            String block,
            String section,
            String spaceNumber,
            FlatOccupancyStatus occupancyStatus,
            BigDecimal areaSqft) {}

    public record View(List<Block> blocks, List<Section> sections, List<Space> spaces) {
        public View {
            blocks = List.copyOf(blocks);
            sections = List.copyOf(sections);
            spaces = List.copyOf(spaces);
        }
    }

    public record BlockCommand(String name) {}

    public record SectionCommand(String name, int sectionOrder) {}

    public record SpaceCommand(String spaceNumber, BigDecimal areaSqft) {}

    public record Created(UUID id) {}

    public record BlockUpdated(UUID id, String name) {}

    public record SectionUpdated(UUID id, String name, int sectionOrder) {}

    public record SpaceUpdated(UUID id, String spaceNumber) {}

    public record Deleted(UUID id, boolean deleted) {}
}
