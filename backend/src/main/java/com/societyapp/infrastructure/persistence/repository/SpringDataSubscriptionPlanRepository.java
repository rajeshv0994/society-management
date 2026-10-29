package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSubscriptionPlanRepository
        extends JpaRepository<SubscriptionPlan, String>,
                com.societyapp.application.port.out.persistence.SubscriptionPlanRepository {}
