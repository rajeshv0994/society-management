package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class StructureResponse {
    private StructureResponse() {}

    public record View(
            List<Block> blocks,
            List<Section> sections,
            List<Section> floors,
            List<Space> spaces,
            List<Space> units,
            List<Space> flats) {}

    public record Block(
            UUID id,
            String name,
            @JsonProperty("section_count") int sectionCount,
            @JsonProperty("floor_count") int floorCount,
            @JsonProperty("space_count") int spaceCount,
            @JsonProperty("unit_count") int unitCount,
            @JsonProperty("flat_count") int flatCount,
            @JsonProperty("occupied_count") long occupiedCount) {}

    public record Section(
            UUID id,
            @JsonProperty("block_id") UUID blockId,
            String name,
            @JsonProperty("section_order") int sectionOrder,
            @JsonProperty("floor_number") int floorNumber,
            String block,
            @JsonProperty("space_count") int spaceCount,
            @JsonProperty("unit_count") int unitCount,
            @JsonProperty("flat_count") int flatCount) {}

    public record Space(
            UUID id,
            @JsonProperty("section_id") UUID sectionId,
            @JsonProperty("floor_id") UUID floorId,
            String block,
            String section,
            String floor,
            @JsonProperty("space_number") String spaceNumber,
            @JsonProperty("unit_number") String unitNumber,
            @JsonProperty("flat_number") String flatNumber,
            @JsonProperty("occupancy_status") String occupancyStatus,
            @JsonProperty("area_sqft") BigDecimal areaSqft) {}

    public record Created(UUID id) {}

    public record BlockUpdated(UUID id, String name) {}

    public record SectionUpdated(
            UUID id,
            String name,
            @JsonProperty("sectionOrder") int sectionOrder,
            @JsonProperty("floorNumber") int floorNumber) {}

    public record SpaceUpdated(
            UUID id,
            @JsonProperty("spaceNumber") String spaceNumber,
            @JsonProperty("unitNumber") String unitNumber,
            @JsonProperty("flatNumber") String flatNumber) {}

    public record Deleted(UUID id, boolean deleted) {}
}
