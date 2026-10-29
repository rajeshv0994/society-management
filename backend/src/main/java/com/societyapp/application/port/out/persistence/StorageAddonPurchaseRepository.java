package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.StorageAddonPurchase;
import java.util.*;

public interface StorageAddonPurchaseRepository
        extends CrudRepositoryPort<StorageAddonPurchase, UUID> {
    List<StorageAddonPurchase> findBySocietyIdOrderByCreatedAtDesc(UUID societyId);

    List<StorageAddonPurchase> findBySocietyIdAndStatus(UUID societyId, String status);
}
