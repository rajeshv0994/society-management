package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.MaintenanceChargeRule;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataMaintenanceChargeRuleRepository
        extends JpaRepository<MaintenanceChargeRule, UUID>,
                com.societyapp.application.port.out.persistence.MaintenanceChargeRuleRepository {
    List<MaintenanceChargeRule> findBySocietyIdOrderByName(UUID societyId);

    Optional<MaintenanceChargeRule> findByIdAndSocietyId(UUID id, UUID societyId);

    boolean existsBySocietyIdAndNameIgnoreCaseAndEffectiveFrom(
            UUID societyId, String name, java.time.LocalDate effectiveFrom);

    boolean existsBySocietyIdAndNameIgnoreCaseAndEffectiveFromAndIdNot(
            UUID societyId, String name, java.time.LocalDate effectiveFrom, UUID id);
}
