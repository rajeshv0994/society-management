package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.StaffSalaryPayment;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataStaffSalaryPaymentRepository
        extends JpaRepository<StaffSalaryPayment, UUID>,
                com.societyapp.application.port.out.persistence.StaffSalaryPaymentRepository {
    List<StaffSalaryPayment> findByStaffUserIdOrderBySalaryMonthDesc(UUID staffUserId);

    boolean existsByStaffUserIdAndSalaryMonth(UUID staffUserId, LocalDate salaryMonth);

    List<StaffSalaryPayment> findBySocietyIdAndPaidOnBetweenOrderByPaidOnDesc(
            UUID societyId, LocalDate from, LocalDate to);
}
