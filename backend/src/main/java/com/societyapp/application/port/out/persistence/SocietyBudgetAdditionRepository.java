package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.SocietyBudgetAddition;
import java.util.*;

public interface SocietyBudgetAdditionRepository
        extends CrudRepositoryPort<SocietyBudgetAddition, UUID> {
    List<SocietyBudgetAddition> findBySocietyIdAndBudgetYearOrderByAddedAtDesc(
            UUID societyId, int budgetYear);
}
