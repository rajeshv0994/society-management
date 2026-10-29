package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.SocietyCustomPlan;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSocietyCustomPlanRepository
        extends JpaRepository<SocietyCustomPlan, UUID>,
                com.societyapp.application.port.out.persistence.SocietyCustomPlanRepository {
    List<SocietyCustomPlan> findAllByOrderByCreatedAtDesc();
}
