package com.societyapp.domain.model;

import java.io.Serializable;
import java.util.UUID;

public class NoticeReadId implements Serializable {
    private UUID noticeId;
    private UUID userId;

    @java.lang.SuppressWarnings("all")
    public UUID getNoticeId() {
        return this.noticeId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getUserId() {
        return this.userId;
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
    public NoticeReadId() {}

    @java.lang.SuppressWarnings("all")
    public NoticeReadId(final UUID noticeId, final UUID userId) {
        this.noticeId = noticeId;
        this.userId = userId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof NoticeReadId)) return false;
        final NoticeReadId other = (NoticeReadId) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        final java.lang.Object this$noticeId = this.getNoticeId();
        final java.lang.Object other$noticeId = other.getNoticeId();
        if (this$noticeId == null ? other$noticeId != null : !this$noticeId.equals(other$noticeId))
            return false;
        final java.lang.Object this$userId = this.getUserId();
        final java.lang.Object other$userId = other.getUserId();
        if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId))
            return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof NoticeReadId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $noticeId = this.getNoticeId();
        result = result * PRIME + ($noticeId == null ? 43 : $noticeId.hashCode());
        final java.lang.Object $userId = this.getUserId();
        result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
        return result;
    }
}
