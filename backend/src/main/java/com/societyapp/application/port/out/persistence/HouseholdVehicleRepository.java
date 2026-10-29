package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.HouseholdVehicle;
import java.util.*;

public interface HouseholdVehicleRepository extends CrudRepositoryPort<HouseholdVehicle, UUID> {
    List<HouseholdVehicle>
            findBySocietyIdAndPrimaryResidentIdAndActiveTrueOrderByRegistrationNumber(
                    UUID societyId, UUID primaryResidentId);

    Optional<HouseholdVehicle> findByIdAndSocietyIdAndPrimaryResidentId(
            UUID id, UUID societyId, UUID primaryResidentId);

    long countBySocietyIdAndActiveTrue(UUID societyId);

    long countBySocietyIdAndActiveTrueAndVerificationStatus(UUID societyId, String status);
}
