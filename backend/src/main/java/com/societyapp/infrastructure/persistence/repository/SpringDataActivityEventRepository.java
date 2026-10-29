package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.ActivityEvent;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataActivityEventRepository
        extends JpaRepository<ActivityEvent, UUID>,
                com.societyapp.application.port.out.persistence.ActivityEventRepository {
    List<ActivityEvent> findByActivityIdOrderByOccurredAt(UUID activityId);
}
