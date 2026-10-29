package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.Vendor;
import java.util.*;

public interface VendorRepository extends CrudRepositoryPort<Vendor, UUID> {
    List<Vendor> findBySocietyIdOrderByName(UUID societyId);

    Optional<Vendor> findByIdAndSocietyId(UUID id, UUID societyId);

    boolean existsBySocietyIdAndNameIgnoreCaseAndServiceTypeIgnoreCase(
            UUID societyId, String name, String serviceType);

    boolean existsBySocietyIdAndNameIgnoreCaseAndServiceTypeIgnoreCaseAndIdNot(
            UUID societyId, String name, String serviceType, UUID id);
}
