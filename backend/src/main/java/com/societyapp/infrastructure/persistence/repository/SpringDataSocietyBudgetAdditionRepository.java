package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.SocietyBudgetAddition;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSocietyBudgetAdditionRepository
        extends JpaRepository<SocietyBudgetAddition, UUID>,
                com.societyapp.application.port.out.persistence.SocietyBudgetAdditionRepository {
    List<SocietyBudgetAddition> findBySocietyIdAndBudgetYearOrderByAddedAtDesc(
            UUID societyId, int budgetYear);
}
