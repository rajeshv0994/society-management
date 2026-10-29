package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.SubscriptionOrder;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSubscriptionOrderRepository
        extends JpaRepository<SubscriptionOrder, UUID>,
                com.societyapp.application.port.out.persistence.SubscriptionOrderRepository {
    List<SubscriptionOrder> findAllByOrderByCreatedAtDesc();

    List<SubscriptionOrder> findBySocietyIdOrderByBillingMonthDesc(UUID societyId);

    Optional<SubscriptionOrder> findByIdAndSocietyId(UUID id, UUID societyId);

    boolean existsBySocietyIdAndBillingMonthAndStatusNot(
            UUID societyId, java.time.LocalDate billingMonth, String status);

    boolean existsBySocietyIdAndDueDateLessThanEqualAndStatusIn(
            UUID societyId, LocalDate date, Collection<String> statuses);
}
