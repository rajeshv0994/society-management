package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.HouseholdVehicle;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataHouseholdVehicleRepository
        extends JpaRepository<HouseholdVehicle, UUID>,
                com.societyapp.application.port.out.persistence.HouseholdVehicleRepository {
    List<HouseholdVehicle>
            findBySocietyIdAndPrimaryResidentIdAndActiveTrueOrderByRegistrationNumber(
                    UUID societyId, UUID primaryResidentId);

    Optional<HouseholdVehicle> findByIdAndSocietyIdAndPrimaryResidentId(
            UUID id, UUID societyId, UUID primaryResidentId);

    long countBySocietyIdAndActiveTrue(UUID societyId);

    long countBySocietyIdAndActiveTrueAndVerificationStatus(UUID societyId, String status);
}
