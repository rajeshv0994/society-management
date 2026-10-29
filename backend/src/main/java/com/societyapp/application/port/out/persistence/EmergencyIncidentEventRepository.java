package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.EmergencyIncidentEvent;
import java.util.*;

public interface EmergencyIncidentEventRepository
        extends CrudRepositoryPort<EmergencyIncidentEvent, UUID> {
    List<EmergencyIncidentEvent> findByIncidentIdOrderByOccurredAt(UUID incidentId);
}
