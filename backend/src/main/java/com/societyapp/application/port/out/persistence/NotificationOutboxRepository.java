package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.NotificationOutbox;
import java.util.*;

public interface NotificationOutboxRepository extends CrudRepositoryPort<NotificationOutbox, UUID> {
    List<NotificationOutbox> findPendingForDispatch(int limit);

    List<NotificationOutbox> findAllByOrderByCreatedAtDesc();

    List<NotificationOutbox> findBySocietyIdOrderByCreatedAtDesc(UUID societyId);
}
