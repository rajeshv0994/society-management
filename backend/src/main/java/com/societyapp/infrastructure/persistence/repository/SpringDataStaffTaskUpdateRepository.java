package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.StaffTaskUpdate;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataStaffTaskUpdateRepository
        extends JpaRepository<StaffTaskUpdate, UUID>,
                com.societyapp.application.port.out.persistence.StaffTaskUpdateRepository {
    List<StaffTaskUpdate> findByTaskIdOrderByCreatedAt(UUID taskId);
}
