package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.StorageObject;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface SpringDataStorageObjectRepository
        extends JpaRepository<StorageObject, UUID>,
                com.societyapp.application.port.out.persistence.StorageObjectRepository {
    Optional<StorageObject> findByStorageKey(String storageKey);

    Optional<StorageObject> findByIdAndSocietyIdAndStatus(UUID id, UUID societyId, String status);

    List<StorageObject> findBySocietyIdAndStatusOrderByCreatedAtDesc(UUID societyId, String status);

    @Query(
            "select coalesce(sum(o.sizeBytes),0) from StorageObject o where o.societyId=:societyId and o.status='ACTIVE'")
    long usedBytes(@Param("societyId") UUID societyId);

    @Query(
            "select o.category, coalesce(sum(o.sizeBytes),0) from StorageObject o where o.societyId=:societyId and o.status='ACTIVE' group by o.category")
    List<Object[]> usageByCategory(@Param("societyId") UUID societyId);
}
