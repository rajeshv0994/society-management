package com.societyapp.application.port.in;

import java.util.UUID;

public interface StructureAccessUseCase {
    boolean belongsToSociety(UUID flatId, UUID societyId);

    boolean occupiedBy(UUID flatId, UUID userId);

    void requireFlat(UUID flatId, UUID societyId);
}
