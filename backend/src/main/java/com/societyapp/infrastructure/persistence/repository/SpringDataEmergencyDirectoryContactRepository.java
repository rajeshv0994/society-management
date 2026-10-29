package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.EmergencyDirectoryContact;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataEmergencyDirectoryContactRepository
        extends JpaRepository<EmergencyDirectoryContact, UUID>,
                com.societyapp.application.port.out.persistence
                        .EmergencyDirectoryContactRepository {
    List<EmergencyDirectoryContact> findBySocietyIdAndActiveTrueOrderByContactTypeAscPriorityAsc(
            UUID societyId);

    Optional<EmergencyDirectoryContact> findByIdAndSocietyId(UUID id, UUID societyId);
}
