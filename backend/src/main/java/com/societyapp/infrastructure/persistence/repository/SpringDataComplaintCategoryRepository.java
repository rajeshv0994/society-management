package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.ComplaintCategory;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataComplaintCategoryRepository
        extends JpaRepository<ComplaintCategory, UUID>,
                com.societyapp.application.port.out.persistence.ComplaintCategoryRepository {
    List<ComplaintCategory> findBySocietyIdOrderByName(UUID societyId);

    Optional<ComplaintCategory> findByIdAndSocietyId(UUID id, UUID societyId);

    boolean existsBySocietyIdAndNameIgnoreCase(UUID societyId, String name);

    boolean existsBySocietyIdAndNameIgnoreCaseAndIdNot(UUID societyId, String name, UUID id);
}
