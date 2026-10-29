package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.AuditEvent;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AuditEventRepository {
    AuditEvent save(AuditEvent event);

    List<AuditEvent> findAll();

    List<AuditEvent> findBySocietyIdOrderByOccurredAtDesc(UUID societyId);

    AuditPage search(AuditQuery query);

    record AuditQuery(
            int page,
            int size,
            String action,
            String entityType,
            UUID societyId,
            UUID actorId,
            OffsetDateTime from,
            OffsetDateTime to,
            String text,
            Boolean sensitive) {}

    record AuditPage(
            List<AuditEvent> items, int page, int size, long totalElements, int totalPages) {
        public AuditPage {
            items = List.copyOf(items);
        }
    }
}
