package com.societyapp.controller;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.societyapp.application.model.structure.StructureModels;
import com.societyapp.application.port.in.StructureUseCase;
import com.societyapp.controller.mapper.StructureHttpMapper;
import com.societyapp.controller.model.StructureResponse;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/society/structure")
@PreAuthorize("hasRole('SOCIETY_ADMIN')")
public class StructureController {
    private final StructureUseCase service;
    private final StructureHttpMapper mapper = new StructureHttpMapper();

    public StructureController(StructureUseCase service) {
        this.service = service;
    }

    public record BlockRequest(@NotBlank String name) {}

    public record FloorRequest(@NotBlank String name, @JsonAlias("sectionOrder") int floorNumber) {}

    public record FlatRequest(
            @JsonAlias({"spaceNumber", "unitNumber"}) @NotBlank String flatNumber,
            @DecimalMin("0") BigDecimal areaSqft) {}

    @GetMapping
    StructureResponse.View get() {
        return mapper.toResponse(service.get(CurrentUser.societyId()));
    }

    @PostMapping("/blocks")
    StructureResponse.Created block(@Valid @RequestBody BlockRequest r) {
        return mapper.toResponse(
                service.addBlock(
                        CurrentUser.societyId(), new StructureModels.BlockCommand(r.name())));
    }

    @PutMapping("/blocks/{blockId}")
    StructureResponse.BlockUpdated updateBlock(
            @PathVariable UUID blockId, @Valid @RequestBody BlockRequest r) {
        return mapper.toResponse(
                service.updateBlock(
                        CurrentUser.societyId(),
                        blockId,
                        new StructureModels.BlockCommand(r.name())));
    }

    @DeleteMapping("/blocks/{blockId}")
    StructureResponse.Deleted deleteBlock(@PathVariable UUID blockId) {
        return mapper.toResponse(service.deleteBlock(CurrentUser.societyId(), blockId));
    }

    @PostMapping({"/blocks/{blockId}/sections", "/blocks/{blockId}/floors"})
    StructureResponse.Created floor(
            @PathVariable UUID blockId, @Valid @RequestBody FloorRequest r) {
        return mapper.toResponse(
                service.addSection(
                        CurrentUser.societyId(),
                        blockId,
                        new StructureModels.SectionCommand(r.name(), r.floorNumber())));
    }

    @PutMapping({"/sections/{floorId}", "/floors/{floorId}"})
    StructureResponse.SectionUpdated updateFloor(
            @PathVariable UUID floorId, @Valid @RequestBody FloorRequest r) {
        return mapper.toResponse(
                service.updateSection(
                        CurrentUser.societyId(),
                        floorId,
                        new StructureModels.SectionCommand(r.name(), r.floorNumber())));
    }

    @DeleteMapping({"/sections/{floorId}", "/floors/{floorId}"})
    StructureResponse.Deleted deleteFloor(@PathVariable UUID floorId) {
        return mapper.toResponse(service.deleteSection(CurrentUser.societyId(), floorId));
    }

    @PostMapping({
        "/sections/{floorId}/spaces",
        "/floors/{floorId}/units",
        "/floors/{floorId}/flats"
    })
    StructureResponse.Created flat(@PathVariable UUID floorId, @Valid @RequestBody FlatRequest r) {
        return mapper.toResponse(
                service.addSpace(
                        CurrentUser.societyId(),
                        floorId,
                        new StructureModels.SpaceCommand(r.flatNumber(), r.areaSqft())));
    }

    @PostMapping("/spaces")
    StructureResponse.Created space(@Valid @RequestBody FlatRequest r) {
        return mapper.toResponse(
                service.addSpace(
                        CurrentUser.societyId(),
                        new StructureModels.SpaceCommand(r.flatNumber(), r.areaSqft())));
    }

    @PutMapping({"/spaces/{flatId}", "/units/{flatId}", "/flats/{flatId}"})
    StructureResponse.SpaceUpdated updateFlat(
            @PathVariable UUID flatId, @Valid @RequestBody FlatRequest r) {
        return mapper.toResponse(
                service.updateSpace(
                        CurrentUser.societyId(),
                        flatId,
                        new StructureModels.SpaceCommand(r.flatNumber(), r.areaSqft())));
    }

    @DeleteMapping({"/spaces/{flatId}", "/units/{flatId}", "/flats/{flatId}"})
    StructureResponse.Deleted deleteFlat(@PathVariable UUID flatId) {
        return mapper.toResponse(service.deleteSpace(CurrentUser.societyId(), flatId));
    }
}
