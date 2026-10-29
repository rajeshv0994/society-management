package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.MaintenanceInvoice;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface SpringDataMaintenanceInvoiceRepository
        extends JpaRepository<MaintenanceInvoice, UUID>,
                com.societyapp.application.port.out.persistence.MaintenanceInvoiceRepository {
    List<MaintenanceInvoice> findBySocietyIdOrderByBillingMonthDesc(UUID societyId);

    List<MaintenanceInvoice> findBySocietyIdAndFlatIdOrderByBillingMonthDesc(
            UUID societyId, UUID flatId);

    Optional<MaintenanceInvoice> findByIdAndSocietyId(UUID id, UUID societyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<MaintenanceInvoice> findForUpdateByIdAndSocietyId(UUID id, UUID societyId);
}
