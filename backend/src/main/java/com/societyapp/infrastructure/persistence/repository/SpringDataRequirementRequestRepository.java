package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.RequirementRequest;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataRequirementRequestRepository
        extends JpaRepository<RequirementRequest, UUID>,
                com.societyapp.application.port.out.persistence.RequirementRequestRepository {
    List<RequirementRequest> findAllByOrderByCreatedAtDesc();

    List<RequirementRequest> findBySocietyIdOrderByCreatedAtDesc(UUID societyId);
}
