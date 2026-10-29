package com.societyapp.application.usecase;

import com.societyapp.application.port.in.StructureAccessUseCase;
import com.societyapp.application.port.out.persistence.*;
import java.util.UUID;

public class StructureAccessService implements StructureAccessUseCase {
    private final FlatRepository flats;
    private final FloorRepository floors;
    private final BlockRepository blocks;

    public StructureAccessService(
            FlatRepository flats, FloorRepository floors, BlockRepository blocks) {
        this.flats = flats;
        this.floors = floors;
        this.blocks = blocks;
    }

    public boolean belongsToSociety(UUID flatId, UUID societyId) {
        return flats.findById(flatId)
                .flatMap(flat -> floors.findById(flat.getFloorId()))
                .flatMap(floor -> blocks.findById(floor.getBlockId()))
                .filter(block -> block.getSocietyId().equals(societyId))
                .isPresent();
    }

    public boolean occupiedBy(UUID flatId, UUID userId) {
        return flats.findById(flatId)
                .filter(
                        flat ->
                                userId.equals(flat.getActiveOwnerId())
                                        || userId.equals(flat.getActiveTenantId()))
                .isPresent();
    }

    public void requireFlat(UUID flatId, UUID societyId) {
        if (!belongsToSociety(flatId, societyId)) {
            throw new IllegalArgumentException("Space not found");
        }
    }
}
