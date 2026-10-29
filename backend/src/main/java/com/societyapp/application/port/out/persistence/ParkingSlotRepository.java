package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.ParkingSlot;
import java.util.*;

public interface ParkingSlotRepository extends CrudRepositoryPort<ParkingSlot, UUID> {
    List<ParkingSlot> findBySocietyIdOrderBySlotNumber(UUID societyId);

    Optional<ParkingSlot> findByIdAndSocietyId(UUID id, UUID societyId);

    boolean existsBySocietyIdAndSlotNumberIgnoreCase(UUID societyId, String slotNumber);

    boolean existsBySocietyIdAndSlotNumberIgnoreCaseAndIdNot(
            UUID societyId, String slotNumber, UUID id);
}
