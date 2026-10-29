package com.societyapp.application.model.support;

import com.societyapp.domain.type.SupportPriority;
import com.societyapp.domain.type.SupportTicketStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SupportTicketView(
        UUID id,
        UUID societyId,
        UUID createdBy,
        String subject,
        String description,
        SupportPriority priority,
        SupportTicketStatus status,
        UUID assignedTo,
        String resolution,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime closedAt,
        String createdByName,
        int messageCount) {}
