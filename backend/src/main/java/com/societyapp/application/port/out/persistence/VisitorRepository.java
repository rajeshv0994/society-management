package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.Visitor;
import java.util.*;

public interface VisitorRepository extends CrudRepositoryPort<Visitor, UUID> {
    List<Visitor> findBySocietyIdOrderByCreatedAtDesc(UUID societyId);

    List<Visitor> findBySocietyIdAndFlatIdOrderByCreatedAtDesc(UUID societyId, UUID flatId);

    List<Visitor> findBySocietyIdAndHostResidentIdOrderByCreatedAtDesc(
            UUID societyId, UUID hostResidentId);

    Optional<Visitor> findByIdAndSocietyId(UUID id, UUID societyId);
}
