package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.StaffTask;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataStaffTaskRepository
        extends JpaRepository<StaffTask, UUID>,
                com.societyapp.application.port.out.persistence.StaffTaskRepository {
    List<StaffTask> findBySocietyIdOrderByCreatedAtDesc(UUID societyId);

    Optional<StaffTask> findByIdAndSocietyId(UUID id, UUID societyId);
}
