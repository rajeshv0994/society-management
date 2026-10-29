package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.Delivery;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataDeliveryRepository
        extends JpaRepository<Delivery, UUID>,
                com.societyapp.application.port.out.persistence.DeliveryRepository {
    List<Delivery> findBySocietyIdOrderByCreatedAtDesc(UUID societyId);

    List<Delivery> findBySocietyIdAndResidentIdOrderByCreatedAtDesc(
            UUID societyId, UUID residentId);

    Optional<Delivery> findByIdAndSocietyId(UUID id, UUID societyId);

    long countBySocietyId(UUID societyId);

    long countBySocietyIdAndStatusIn(UUID societyId, Collection<String> statuses);
}
