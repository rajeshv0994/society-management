package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.HouseholdMember;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataHouseholdMemberRepository
        extends JpaRepository<HouseholdMember, UUID>,
                com.societyapp.application.port.out.persistence.HouseholdMemberRepository {
    List<HouseholdMember> findBySocietyIdAndPrimaryResidentIdAndActiveTrue(
            UUID societyId, UUID primaryResidentId);

    Optional<HouseholdMember> findByIdAndSocietyIdAndPrimaryResidentId(
            UUID id, UUID societyId, UUID primaryResidentId);

    long countBySocietyIdAndActiveTrue(UUID societyId);

    long countBySocietyIdAndActiveTrueAndVerificationStatus(UUID societyId, String status);
}
