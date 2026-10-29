package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.StaffSalaryPayment;
import java.time.LocalDate;
import java.util.*;

public interface StaffSalaryPaymentRepository extends CrudRepositoryPort<StaffSalaryPayment, UUID> {
    List<StaffSalaryPayment> findByStaffUserIdOrderBySalaryMonthDesc(UUID staffUserId);

    boolean existsByStaffUserIdAndSalaryMonth(UUID staffUserId, LocalDate salaryMonth);

    List<StaffSalaryPayment> findBySocietyIdAndPaidOnBetweenOrderByPaidOnDesc(
            UUID societyId, LocalDate from, LocalDate to);
}
