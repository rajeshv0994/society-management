package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.SocietyExpense;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSocietyExpenseRepository
        extends JpaRepository<SocietyExpense, UUID>,
                com.societyapp.application.port.out.persistence.SocietyExpenseRepository {
    List<SocietyExpense> findBySocietyIdOrderByExpenseDateDescCreatedAtDesc(UUID societyId);
}
