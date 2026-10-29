package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.StorageObject;
import java.util.*;

public interface StorageObjectRepository extends CrudRepositoryPort<StorageObject, UUID> {
    Optional<StorageObject> findByStorageKey(String storageKey);

    Optional<StorageObject> findByIdAndSocietyIdAndStatus(UUID id, UUID societyId, String status);

    List<StorageObject> findBySocietyIdAndStatusOrderByCreatedAtDesc(UUID societyId, String status);

    long usedBytes(UUID societyId);

    List<Object[]> usageByCategory(UUID societyId);
}
