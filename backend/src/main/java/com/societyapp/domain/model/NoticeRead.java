package com.societyapp.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class NoticeRead {
    private UUID noticeId;
    private UUID userId;
    private OffsetDateTime readAt;
    private OffsetDateTime acknowledgedAt;

    @java.lang.SuppressWarnings("all")
    public UUID getNoticeId() {
        return this.noticeId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getUserId() {
        return this.userId;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getReadAt() {
        return this.readAt;
    }

    @java.lang.SuppressWarnings("all")
    public OffsetDateTime getAcknowledgedAt() {
        return this.acknowledgedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setNoticeId(final UUID noticeId) {
        this.noticeId = noticeId;
    }

    @java.lang.SuppressWarnings("all")
    public void setUserId(final UUID userId) {
        this.userId = userId;
    }

    @java.lang.SuppressWarnings("all")
    public void setReadAt(final OffsetDateTime readAt) {
        this.readAt = readAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setAcknowledgedAt(final OffsetDateTime acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    @java.lang.SuppressWarnings("all")
    public NoticeRead() {}
}
