package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.NotificationOutbox;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataNotificationOutboxRepository
        extends JpaRepository<NotificationOutbox, UUID>,
                com.societyapp.application.port.out.persistence.NotificationOutboxRepository {
    @Override
    @Query(
            value =
                    "select * from notification_outbox where status='PENDING' order by created_at for update skip locked limit :limit",
            nativeQuery = true)
    List<NotificationOutbox> findPendingForDispatch(@Param("limit") int limit);

    List<NotificationOutbox> findAllByOrderByCreatedAtDesc();

    List<NotificationOutbox> findBySocietyIdOrderByCreatedAtDesc(UUID societyId);
}
