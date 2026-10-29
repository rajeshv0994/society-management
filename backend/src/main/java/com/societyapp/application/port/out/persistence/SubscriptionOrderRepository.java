package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.SubscriptionOrder;
import java.time.LocalDate;
import java.util.*;

public interface SubscriptionOrderRepository extends CrudRepositoryPort<SubscriptionOrder, UUID> {
    List<SubscriptionOrder> findAllByOrderByCreatedAtDesc();

    List<SubscriptionOrder> findBySocietyIdOrderByBillingMonthDesc(UUID societyId);

    Optional<SubscriptionOrder> findByIdAndSocietyId(UUID id, UUID societyId);

    boolean existsBySocietyIdAndBillingMonthAndStatusNot(
            UUID societyId, java.time.LocalDate billingMonth, String status);

    boolean existsBySocietyIdAndDueDateLessThanEqualAndStatusIn(
            UUID societyId, LocalDate date, Collection<String> statuses);
}
