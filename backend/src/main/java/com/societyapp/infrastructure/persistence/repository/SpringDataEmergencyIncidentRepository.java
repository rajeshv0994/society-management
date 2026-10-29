package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.EmergencyIncident;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataEmergencyIncidentRepository
        extends JpaRepository<EmergencyIncident, UUID>,
                com.societyapp.application.port.out.persistence.EmergencyIncidentRepository {
    List<EmergencyIncident> findBySocietyIdOrderByCreatedAtDesc(UUID societyId);

    List<EmergencyIncident> findBySocietyIdAndResidentIdOrderByCreatedAtDesc(
            UUID societyId, UUID residentId);

    Optional<EmergencyIncident> findByIdAndSocietyId(UUID id, UUID societyId);

    long countBySocietyId(UUID societyId);

    long countBySocietyIdAndStatusIn(UUID societyId, Collection<String> statuses);

    long countBySocietyIdAndSeverityAndStatusIn(
            UUID societyId, String severity, Collection<String> statuses);
}
