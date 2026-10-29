package com.societyapp.controller.mapper;

import com.societyapp.application.model.audit.AuditModels;
import com.societyapp.controller.model.AuditPageResponse;

public final class AuditHttpMapper {
    public AuditPageResponse toResponse(AuditModels.Page page) {
        return new AuditPageResponse(
                page.items().stream().map(this::toEntry).toList(),
                page.page(),
                page.size(),
                page.total(),
                page.totalPages());
    }

    private AuditPageResponse.Entry toEntry(AuditModels.Entry entry) {
        return new AuditPageResponse.Entry(
                entry.id(),
                entry.societyId(),
                entry.societyName(),
                entry.actorUserId(),
                entry.actorName(),
                entry.action(),
                entry.entityType(),
                entry.entityId(),
                entry.sensitive(),
                entry.details().values(),
                entry.occurredAt());
    }
}
