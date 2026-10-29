package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.StorageAddonPurchase;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataStorageAddonPurchaseRepository
        extends JpaRepository<StorageAddonPurchase, UUID>,
                com.societyapp.application.port.out.persistence.StorageAddonPurchaseRepository {
    List<StorageAddonPurchase> findBySocietyIdOrderByCreatedAtDesc(UUID societyId);

    List<StorageAddonPurchase> findBySocietyIdAndStatus(UUID societyId, String status);
}
