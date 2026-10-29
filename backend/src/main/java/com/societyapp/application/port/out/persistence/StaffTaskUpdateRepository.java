package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.StaffTaskUpdate;
import java.util.*;

public interface StaffTaskUpdateRepository extends CrudRepositoryPort<StaffTaskUpdate, UUID> {
    List<StaffTaskUpdate> findByTaskIdOrderByCreatedAt(UUID taskId);
}
