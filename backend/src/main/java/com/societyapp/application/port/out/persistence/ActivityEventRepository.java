package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.ActivityEvent;
import java.util.*;

public interface ActivityEventRepository extends CrudRepositoryPort<ActivityEvent, UUID> {
    List<ActivityEvent> findByActivityIdOrderByOccurredAt(UUID activityId);
}
