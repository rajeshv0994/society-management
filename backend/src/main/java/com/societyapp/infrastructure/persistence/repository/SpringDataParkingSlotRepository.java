package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.ParkingSlot;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataParkingSlotRepository
        extends JpaRepository<ParkingSlot, UUID>,
                com.societyapp.application.port.out.persistence.ParkingSlotRepository {
    List<ParkingSlot> findBySocietyIdOrderBySlotNumber(UUID societyId);

    Optional<ParkingSlot> findByIdAndSocietyId(UUID id, UUID societyId);

    boolean existsBySocietyIdAndSlotNumberIgnoreCase(UUID societyId, String slotNumber);

    boolean existsBySocietyIdAndSlotNumberIgnoreCaseAndIdNot(
            UUID societyId, String slotNumber, UUID id);
}
