package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.SocietyBudget;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSocietyBudgetRepository
        extends JpaRepository<SocietyBudget, UUID>,
                com.societyapp.application.port.out.persistence.SocietyBudgetRepository {
    Optional<SocietyBudget> findBySocietyIdAndBudgetYear(UUID societyId, int budgetYear);
}
