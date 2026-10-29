package com.societyapp.application.model.audit;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AuditModels {
    private AuditModels() {}

    public record Details(Map<String, ?> values) {
        public Details {
            values =
                    values == null
                            ? Map.of()
                            : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(values));
        }
    }

    public record RecordCommand(
            UUID societyId,
            UUID actorId,
            String action,
            String entityType,
            String entityId,
            boolean sensitive,
            Details details) {}

    public record SearchCriteria(
            int page,
            int size,
            String action,
            String entityType,
            UUID societyId,
            UUID actorId,
            OffsetDateTime from,
            OffsetDateTime to,
            String search,
            Boolean sensitive) {}

    public record Entry(
            Long id,
            UUID societyId,
            String societyName,
            UUID actorUserId,
            String actorName,
            String action,
            String entityType,
            String entityId,
            boolean sensitive,
            Details details,
            OffsetDateTime occurredAt) {}

    public record Page(List<Entry> items, int page, int size, long total, int totalPages) {
        public Page {
            items = List.copyOf(items);
        }
    }
}
