package com.societyapp.application.model.notice;

import com.societyapp.domain.type.NoticeAudience;
import com.societyapp.domain.type.NoticePriority;
import com.societyapp.domain.type.NoticeStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class NoticeModels {
    private NoticeModels() {}

    public record SaveCommand(
            String title,
            String body,
            NoticeAudience audience,
            NoticePriority priority,
            boolean pinned,
            boolean requiresAcknowledgement,
            NoticeStatus status,
            OffsetDateTime expiresAt) {}

    public record View(
            UUID id,
            String title,
            String body,
            NoticeAudience audience,
            NoticePriority priority,
            boolean pinned,
            boolean requiresAcknowledgement,
            NoticeStatus status,
            OffsetDateTime publishedAt,
            OffsetDateTime expiresAt,
            OffsetDateTime updatedAt,
            boolean expired,
            boolean read,
            OffsetDateTime readAt,
            boolean acknowledged,
            OffsetDateTime acknowledgedAt,
            UUID publishedBy,
            String publishedByName,
            Long readCount,
            Long acknowledgementCount) {}

    public record Options(
            boolean canManage,
            List<NoticeAudience> audiences,
            List<NoticePriority> priorities,
            List<NoticeStatus> statuses) {}

    public record Status(UUID id, NoticeStatus status) {}

    public record Read(UUID id, boolean read) {}

    public record Acknowledgement(UUID id, boolean acknowledged) {}
}
