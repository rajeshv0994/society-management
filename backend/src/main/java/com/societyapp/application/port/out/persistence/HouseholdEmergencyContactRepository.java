package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.HouseholdEmergencyContact;
import java.util.*;

public interface HouseholdEmergencyContactRepository
        extends CrudRepositoryPort<HouseholdEmergencyContact, UUID> {
    List<HouseholdEmergencyContact> findBySocietyIdAndPrimaryResidentIdAndActiveTrueOrderByPriority(
            UUID societyId, UUID primaryResidentId);

    Optional<HouseholdEmergencyContact> findByIdAndSocietyIdAndPrimaryResidentId(
            UUID id, UUID societyId, UUID primaryResidentId);

    long countBySocietyIdAndActiveTrue(UUID societyId);
}
