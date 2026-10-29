package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.MaintenanceInvoice;
import java.util.*;

public interface MaintenanceInvoiceRepository extends CrudRepositoryPort<MaintenanceInvoice, UUID> {
    List<MaintenanceInvoice> findBySocietyIdOrderByBillingMonthDesc(UUID societyId);

    List<MaintenanceInvoice> findBySocietyIdAndFlatIdOrderByBillingMonthDesc(
            UUID societyId, UUID flatId);

    Optional<MaintenanceInvoice> findByIdAndSocietyId(UUID id, UUID societyId);

    Optional<MaintenanceInvoice> findForUpdateByIdAndSocietyId(UUID id, UUID societyId);
}
