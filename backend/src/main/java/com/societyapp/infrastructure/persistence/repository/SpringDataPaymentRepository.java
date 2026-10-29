package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.Payment;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPaymentRepository
        extends JpaRepository<Payment, UUID>,
                com.societyapp.application.port.out.persistence.PaymentRepository {
    List<Payment> findBySocietyIdOrderByPaidAtDesc(UUID societyId);

    List<Payment> findBySocietyIdAndInvoiceIdInOrderByPaidAtDesc(
            UUID societyId, Collection<UUID> invoiceIds);

    List<Payment> findByInvoiceIdOrderByPaidAtDesc(UUID invoiceId);

    Optional<Payment> findByIdAndSocietyId(UUID id, UUID societyId);
}
