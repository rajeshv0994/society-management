package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.EmergencyIncident;
import java.util.*;

public interface EmergencyIncidentRepository extends CrudRepositoryPort<EmergencyIncident, UUID> {
    List<EmergencyIncident> findBySocietyIdOrderByCreatedAtDesc(UUID societyId);

    List<EmergencyIncident> findBySocietyIdAndResidentIdOrderByCreatedAtDesc(
            UUID societyId, UUID residentId);

    Optional<EmergencyIncident> findByIdAndSocietyId(UUID id, UUID societyId);

    long countBySocietyId(UUID societyId);

    long countBySocietyIdAndStatusIn(UUID societyId, Collection<String> statuses);

    long countBySocietyIdAndSeverityAndStatusIn(
            UUID societyId, String severity, Collection<String> statuses);
}
