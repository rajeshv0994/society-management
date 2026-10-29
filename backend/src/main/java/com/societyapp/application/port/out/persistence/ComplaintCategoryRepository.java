package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.ComplaintCategory;
import java.util.*;

public interface ComplaintCategoryRepository extends CrudRepositoryPort<ComplaintCategory, UUID> {
    List<ComplaintCategory> findBySocietyIdOrderByName(UUID societyId);

    Optional<ComplaintCategory> findByIdAndSocietyId(UUID id, UUID societyId);

    boolean existsBySocietyIdAndNameIgnoreCase(UUID societyId, String name);

    boolean existsBySocietyIdAndNameIgnoreCaseAndIdNot(UUID societyId, String name, UUID id);
}
