package com.societyapp.application.service;

import com.societyapp.application.model.audit.AuditModels;
import com.societyapp.application.port.in.AuditUseCase;
import com.societyapp.application.port.out.JsonCodec;
import com.societyapp.application.port.out.JsonCodecException;
import com.societyapp.application.port.out.persistence.AuditEventRepository;
import com.societyapp.application.port.out.persistence.AuditEventRepository.AuditQuery;
import com.societyapp.application.port.out.persistence.SocietyRepository;
import com.societyapp.application.port.out.persistence.UserAccountJpaRepository;
import com.societyapp.application.usecase.*;
import com.societyapp.domain.model.AuditEvent;
import java.time.OffsetDateTime;
import java.util.*;

public class AuditService implements AuditUseCase {
    private final AuditEventRepository events;
    private final SocietyRepository societies;
    private final UserAccountJpaRepository users;
    private final JsonCodec json;

    public AuditService(
            AuditEventRepository events,
            SocietyRepository societies,
            UserAccountJpaRepository users,
            JsonCodec json) {
        this.events = events;
        this.societies = societies;
        this.users = users;
        this.json = json;
    }

    @Override
    public void record(AuditModels.RecordCommand command) {
        var event = new AuditEvent();
        event.setSocietyId(command.societyId());
        event.setActorUserId(command.actorId());
        event.setAction(limit(command.action(), 100));
        event.setEntityType(limit(command.entityType(), 80));
        event.setEntityId(command.entityId() == null ? null : limit(command.entityId(), 100));
        event.setSensitive(command.sensitive());
        var storedDetails = new LinkedHashMap<String, Object>();
        if (command.details() != null) {
            command.details().values().forEach(storedDetails::put);
        }
        if (command.actorId() != null) {
            users.findById(command.actorId())
                    .ifPresent(
                            actor -> {
                                storedDetails.put("actor_name_snapshot", actor.getFullName());
                                storedDetails.put("actor_email_snapshot", actor.getEmail());
                            });
        }
        try {
            event.setDetails(json.write(storedDetails));
        } catch (JsonCodecException exception) {
            throw new IllegalStateException("Could not serialize audit details", exception);
        }
        event.setOccurredAt(OffsetDateTime.now());
        events.save(event);
    }

    @Override
    public AuditModels.Page search(AuditModels.SearchCriteria criteria) {
        int safePage = Math.max(criteria.page(), 0);
        int safeSize = Math.min(Math.max(criteria.size(), 1), 100);
        var result =
                events.search(
                        new AuditQuery(
                                safePage,
                                safeSize,
                                criteria.action(),
                                criteria.entityType(),
                                criteria.societyId(),
                                criteria.actorId(),
                                criteria.from(),
                                criteria.to(),
                                criteria.search(),
                                criteria.sensitive()));
        var societyNames = new HashMap<UUID, String>();
        var actorNames = new HashMap<UUID, String>();
        societies
                .findAllById(
                        result.items().stream()
                                .map(AuditEvent::getSocietyId)
                                .filter(Objects::nonNull)
                                .collect(java.util.stream.Collectors.toSet()))
                .forEach(item -> societyNames.put(item.getId(), item.getName()));
        users.findAllById(
                        result.items().stream()
                                .map(AuditEvent::getActorUserId)
                                .filter(Objects::nonNull)
                                .collect(java.util.stream.Collectors.toSet()))
                .forEach(item -> actorNames.put(item.getId(), item.getFullName()));
        return new AuditModels.Page(
                result.items().stream()
                        .map(event -> view(event, societyNames, actorNames))
                        .toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }

    private AuditModels.Entry view(
            AuditEvent event, Map<UUID, String> societyNames, Map<UUID, String> actorNames) {
        var details = parseDetails(event.getDetails());
        String actorName = actorNames.get(event.getActorUserId());
        if (actorName == null) {
            actorName = Objects.toString(details.values().get("actor_name_snapshot"), null);
        }
        return new AuditModels.Entry(
                event.getId(),
                event.getSocietyId(),
                societyNames.get(event.getSocietyId()),
                event.getActorUserId(),
                actorName,
                event.getAction(),
                event.getEntityType(),
                event.getEntityId(),
                event.isSensitive(),
                details,
                event.getOccurredAt());
    }

    private AuditModels.Details parseDetails(String details) {
        try {
            Object parsed = json.readObject(details);
            if (parsed instanceof Map<?, ?> values) {
                var normalized = new LinkedHashMap<String, Object>();
                values.forEach((key, value) -> normalized.put(String.valueOf(key), value));
                return new AuditModels.Details(normalized);
            }
            return new AuditModels.Details(Map.of("raw", parsed));
        } catch (JsonCodecException ignored) {
            return new AuditModels.Details(Map.of("raw", details));
        }
    }

    private String limit(String value, int maximum) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Audit action and entity type are required");
        }
        return value.length() <= maximum ? value : value.substring(0, maximum);
    }
}
