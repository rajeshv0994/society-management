package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.HouseholdEmergencyContact;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataHouseholdEmergencyContactRepository
        extends JpaRepository<HouseholdEmergencyContact, UUID>,
                com.societyapp.application.port.out.persistence
                        .HouseholdEmergencyContactRepository {
    List<HouseholdEmergencyContact> findBySocietyIdAndPrimaryResidentIdAndActiveTrueOrderByPriority(
            UUID societyId, UUID primaryResidentId);

    Optional<HouseholdEmergencyContact> findByIdAndSocietyIdAndPrimaryResidentId(
            UUID id, UUID societyId, UUID primaryResidentId);

    long countBySocietyIdAndActiveTrue(UUID societyId);
}
