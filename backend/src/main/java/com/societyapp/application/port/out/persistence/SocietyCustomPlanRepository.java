package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.SocietyCustomPlan;
import java.util.*;

public interface SocietyCustomPlanRepository extends CrudRepositoryPort<SocietyCustomPlan, UUID> {
    List<SocietyCustomPlan> findAllByOrderByCreatedAtDesc();
}
