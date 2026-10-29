package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.SocietyBudget;
import java.util.*;

public interface SocietyBudgetRepository extends CrudRepositoryPort<SocietyBudget, UUID> {
    Optional<SocietyBudget> findBySocietyIdAndBudgetYear(UUID societyId, int budgetYear);
}
