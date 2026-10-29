package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.Payment;
import java.util.*;

public interface PaymentRepository extends CrudRepositoryPort<Payment, UUID> {
    List<Payment> findBySocietyIdOrderByPaidAtDesc(UUID societyId);

    List<Payment> findBySocietyIdAndInvoiceIdInOrderByPaidAtDesc(
            UUID societyId, Collection<UUID> invoiceIds);

    List<Payment> findByInvoiceIdOrderByPaidAtDesc(UUID invoiceId);

    Optional<Payment> findByIdAndSocietyId(UUID id, UUID societyId);
}
