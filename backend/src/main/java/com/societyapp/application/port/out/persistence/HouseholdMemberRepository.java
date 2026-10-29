package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.HouseholdMember;
import java.util.*;

public interface HouseholdMemberRepository extends CrudRepositoryPort<HouseholdMember, UUID> {
    List<HouseholdMember> findBySocietyIdAndPrimaryResidentIdAndActiveTrue(
            UUID societyId, UUID primaryResidentId);

    Optional<HouseholdMember> findByIdAndSocietyIdAndPrimaryResidentId(
            UUID id, UUID societyId, UUID primaryResidentId);

    long countBySocietyIdAndActiveTrue(UUID societyId);

    long countBySocietyIdAndActiveTrueAndVerificationStatus(UUID societyId, String status);
}
