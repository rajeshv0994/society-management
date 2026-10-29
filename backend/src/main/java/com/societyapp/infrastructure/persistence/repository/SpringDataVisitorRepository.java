package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.Visitor;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataVisitorRepository
        extends JpaRepository<Visitor, UUID>,
                com.societyapp.application.port.out.persistence.VisitorRepository {
    List<Visitor> findBySocietyIdOrderByCreatedAtDesc(UUID societyId);

    List<Visitor> findBySocietyIdAndFlatIdOrderByCreatedAtDesc(UUID societyId, UUID flatId);

    List<Visitor> findBySocietyIdAndHostResidentIdOrderByCreatedAtDesc(
            UUID societyId, UUID hostResidentId);

    Optional<Visitor> findByIdAndSocietyId(UUID id, UUID societyId);
}
