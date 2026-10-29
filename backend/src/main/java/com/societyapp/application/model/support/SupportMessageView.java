package com.societyapp.application.model.support;

import com.societyapp.domain.type.MessageType;
import com.societyapp.domain.type.UserRole;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SupportMessageView(
        UUID id,
        String message,
        MessageType messageType,
        String attachmentKey,
        OffsetDateTime createdAt,
        OffsetDateTime readAt,
        UUID senderId,
        String senderName,
        UserRole senderRole) {}
