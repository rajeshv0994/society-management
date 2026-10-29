package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.EmergencyDirectoryContact;
import java.util.*;

public interface EmergencyDirectoryContactRepository
        extends CrudRepositoryPort<EmergencyDirectoryContact, UUID> {
    List<EmergencyDirectoryContact> findBySocietyIdAndActiveTrueOrderByContactTypeAscPriorityAsc(
            UUID societyId);

    Optional<EmergencyDirectoryContact> findByIdAndSocietyId(UUID id, UUID societyId);
}
