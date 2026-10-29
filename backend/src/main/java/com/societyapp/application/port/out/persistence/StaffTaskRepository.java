package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.StaffTask;
import java.util.*;

public interface StaffTaskRepository extends CrudRepositoryPort<StaffTask, UUID> {
    List<StaffTask> findBySocietyIdOrderByCreatedAtDesc(UUID societyId);

    Optional<StaffTask> findByIdAndSocietyId(UUID id, UUID societyId);
}
