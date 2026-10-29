package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.Vendor;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataVendorRepository
        extends JpaRepository<Vendor, UUID>,
                com.societyapp.application.port.out.persistence.VendorRepository {
    List<Vendor> findBySocietyIdOrderByName(UUID societyId);

    Optional<Vendor> findByIdAndSocietyId(UUID id, UUID societyId);

    boolean existsBySocietyIdAndNameIgnoreCaseAndServiceTypeIgnoreCase(
            UUID societyId, String name, String serviceType);

    boolean existsBySocietyIdAndNameIgnoreCaseAndServiceTypeIgnoreCaseAndIdNot(
            UUID societyId, String name, String serviceType, UUID id);
}
