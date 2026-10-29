package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.application.port.out.persistence.AuditEventRepository;
import com.societyapp.domain.model.AuditEvent;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class JpaAuditEventRepositoryAdapter implements AuditEventRepository {
    private final SpringDataAuditEventRepository repository;

    public JpaAuditEventRepositoryAdapter(SpringDataAuditEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public AuditEvent save(AuditEvent event) {
        return repository.save(event);
    }

    @Override
    public List<AuditEvent> findAll() {
        return repository.findAll();
    }

    @Override
    public List<AuditEvent> findBySocietyIdOrderByOccurredAtDesc(UUID societyId) {
        return repository.findBySocietyIdOrderByOccurredAtDesc(societyId);
    }

    @Override
    public AuditPage search(AuditQuery query) {
        Specification<AuditEvent> filters = Specification.unrestricted();
        if (hasText(query.action())) {
            filters =
                    filters.and(
                            (root, ignored, criteria) ->
                                    criteria.equal(root.get("action"), query.action().trim()));
        }
        if (hasText(query.entityType())) {
            filters =
                    filters.and(
                            (root, ignored, criteria) ->
                                    criteria.equal(
                                            root.get("entityType"), query.entityType().trim()));
        }
        if (query.societyId() != null) {
            filters =
                    filters.and(
                            (root, ignored, criteria) ->
                                    criteria.equal(root.get("societyId"), query.societyId()));
        }
        if (query.actorId() != null) {
            filters =
                    filters.and(
                            (root, ignored, criteria) ->
                                    criteria.equal(root.get("actorUserId"), query.actorId()));
        }
        if (query.sensitive() != null) {
            filters =
                    filters.and(
                            (root, ignored, criteria) ->
                                    criteria.equal(root.get("sensitive"), query.sensitive()));
        }
        if (query.from() != null) {
            filters =
                    filters.and(
                            (root, ignored, criteria) ->
                                    criteria.greaterThanOrEqualTo(
                                            root.get("occurredAt"), query.from()));
        }
        if (query.to() != null) {
            filters =
                    filters.and(
                            (root, ignored, criteria) ->
                                    criteria.lessThanOrEqualTo(root.get("occurredAt"), query.to()));
        }
        if (hasText(query.text())) {
            String term = "%" + query.text().trim().toLowerCase(Locale.ROOT) + "%";
            filters =
                    filters.and(
                            (root, ignored, criteria) ->
                                    criteria.or(
                                            criteria.like(criteria.lower(root.get("action")), term),
                                            criteria.like(
                                                    criteria.lower(root.get("entityType")), term),
                                            criteria.like(
                                                    criteria.lower(root.get("entityId")), term),
                                            criteria.like(
                                                    criteria.lower(
                                                            root.get("details").as(String.class)),
                                                    term)));
        }
        var page =
                repository.findAll(
                        filters,
                        PageRequest.of(
                                query.page(),
                                query.size(),
                                Sort.by(Sort.Order.desc("occurredAt"), Sort.Order.desc("id"))));
        return new AuditPage(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
