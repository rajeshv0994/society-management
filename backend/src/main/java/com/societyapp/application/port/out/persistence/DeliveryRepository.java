package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.Delivery;
import java.util.*;

public interface DeliveryRepository extends CrudRepositoryPort<Delivery, UUID> {
    List<Delivery> findBySocietyIdOrderByCreatedAtDesc(UUID societyId);

    List<Delivery> findBySocietyIdAndResidentIdOrderByCreatedAtDesc(
            UUID societyId, UUID residentId);

    Optional<Delivery> findByIdAndSocietyId(UUID id, UUID societyId);

    long countBySocietyId(UUID societyId);

    long countBySocietyIdAndStatusIn(UUID societyId, Collection<String> statuses);
}
