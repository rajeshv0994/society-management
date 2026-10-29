package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.EmergencyIncidentEvent;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataEmergencyIncidentEventRepository
        extends JpaRepository<EmergencyIncidentEvent, UUID>,
                com.societyapp.application.port.out.persistence.EmergencyIncidentEventRepository {
    List<EmergencyIncidentEvent> findByIncidentIdOrderByOccurredAt(UUID incidentId);
}
