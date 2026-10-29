package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.RequirementRequest;
import java.util.*;

public interface RequirementRequestRepository extends CrudRepositoryPort<RequirementRequest, UUID> {
    List<RequirementRequest> findAllByOrderByCreatedAtDesc();

    List<RequirementRequest> findBySocietyIdOrderByCreatedAtDesc(UUID societyId);
}
