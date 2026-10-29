package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.SocietyExpense;
import java.util.*;

public interface SocietyExpenseRepository extends CrudRepositoryPort<SocietyExpense, UUID> {
    List<SocietyExpense> findBySocietyIdOrderByExpenseDateDescCreatedAtDesc(UUID societyId);
}
