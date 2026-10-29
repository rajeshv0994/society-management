package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.MaintenanceChargeRule;
import java.util.*;

public interface MaintenanceChargeRuleRepository
        extends CrudRepositoryPort<MaintenanceChargeRule, UUID> {
    List<MaintenanceChargeRule> findBySocietyIdOrderByName(UUID societyId);

    Optional<MaintenanceChargeRule> findByIdAndSocietyId(UUID id, UUID societyId);

    boolean existsBySocietyIdAndNameIgnoreCaseAndEffectiveFrom(
            UUID societyId, String name, java.time.LocalDate effectiveFrom);

    boolean existsBySocietyIdAndNameIgnoreCaseAndEffectiveFromAndIdNot(
            UUID societyId, String name, java.time.LocalDate effectiveFrom, UUID id);
}
