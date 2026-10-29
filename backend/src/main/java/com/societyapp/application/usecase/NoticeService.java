package com.societyapp.application.usecase;

import com.societyapp.application.exception.AuthorizationException;
import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.model.notice.NoticeModels;
import com.societyapp.application.port.in.NoticeUseCase;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.NoticeAudience;
import com.societyapp.domain.type.NoticePriority;
import com.societyapp.domain.type.NoticeStatus;
import com.societyapp.domain.type.StaffPermissionCode;
import com.societyapp.domain.type.UserRole;
import java.time.OffsetDateTime;
import java.util.*;

public class NoticeService implements NoticeUseCase {
    private final NoticeRepository notices;
    private final NoticeReadRepository reads;
    private final UserAccountJpaRepository users;
    private final StaffPermissionRepository permissions;

    public NoticeService(
            NoticeRepository notices,
            NoticeReadRepository reads,
            UserAccountJpaRepository users,
            StaffPermissionRepository permissions) {
        this.notices = notices;
        this.reads = reads;
        this.users = users;
        this.permissions = permissions;
    }

    public List<NoticeModels.View> list(UUID societyId, UUID userId, UserRole role) {
        boolean manager = canManage(userId, role);
        var now = OffsetDateTime.now();
        return notices.findBySocietyIdOrderByPublishedAtDesc(societyId).stream()
                .filter(
                        notice ->
                                manager
                                        || (NoticeStatus.PUBLISHED.matches(notice.getStatus())
                                                && (notice.getExpiresAt() == null
                                                        || notice.getExpiresAt().isAfter(now))
                                                && visibleTo(notice.getAudience(), role)))
                .sorted(
                        Comparator.comparing(Notice::isPinned)
                                .reversed()
                                .thenComparing(
                                        notice ->
                                                notice.getPublishedAt() == null
                                                        ? OffsetDateTime.MIN
                                                        : notice.getPublishedAt(),
                                        Comparator.reverseOrder()))
                .map(notice -> view(notice, userId, manager))
                .toList();
    }

    public NoticeModels.Options options(UUID userId, UserRole role) {
        return new NoticeModels.Options(
                canManage(userId, role),
                List.of(NoticeAudience.values()),
                List.of(NoticePriority.values()),
                List.of(NoticeStatus.values()));
    }

    public NoticeModels.Status create(
            UUID societyId, UUID publisherId, UserRole role, NoticeModels.SaveCommand command) {
        requireManager(publisherId, role);
        var requestedStatus = command.status() == null ? NoticeStatus.PUBLISHED : command.status();
        var requestedPriority =
                command.priority() == null ? NoticePriority.NORMAL : command.priority();
        validate(requestedStatus, command.expiresAt());
        var now = OffsetDateTime.now();
        var notice = new Notice();
        notice.setSocietyId(societyId);
        notice.setTitle(command.title().trim());
        notice.setBody(command.body().trim());
        notice.setAudience(command.audience().code());
        notice.setPriority(requestedPriority.code());
        notice.setPinned(command.pinned());
        notice.setRequiresAcknowledgement(command.requiresAcknowledgement());
        notice.setStatus(requestedStatus.code());
        notice.setPublishedBy(publisherId);
        notice.setPublishedAt(requestedStatus == NoticeStatus.PUBLISHED ? now : null);
        notice.setExpiresAt(command.expiresAt());
        notice.setUpdatedAt(now);
        notice = notices.save(notice);
        return new NoticeModels.Status(notice.getId(), NoticeStatus.from(notice.getStatus()));
    }

    public NoticeModels.Status update(
            UUID societyId,
            UUID actorId,
            UserRole role,
            UUID noticeId,
            NoticeModels.SaveCommand command) {
        requireManager(actorId, role);
        var notice =
                notices.findById(noticeId)
                        .filter(item -> societyId.equals(item.getSocietyId()))
                        .orElseThrow(() -> new ResourceNotFoundException("Notice not found"));
        var requestedStatus = command.status();
        var previousStatus = NoticeStatus.from(notice.getStatus());
        validate(requestedStatus, command.expiresAt());
        notice.setTitle(command.title().trim());
        notice.setBody(command.body().trim());
        notice.setAudience(command.audience().code());
        notice.setPriority(command.priority().code());
        notice.setPinned(command.pinned());
        notice.setRequiresAcknowledgement(command.requiresAcknowledgement());
        notice.setStatus(requestedStatus.code());
        notice.setExpiresAt(command.expiresAt());
        notice.setUpdatedAt(OffsetDateTime.now());
        if (requestedStatus == NoticeStatus.PUBLISHED && previousStatus != NoticeStatus.PUBLISHED) {
            notice.setPublishedAt(OffsetDateTime.now());
        }
        notices.save(notice);
        return new NoticeModels.Status(noticeId, requestedStatus);
    }

    public NoticeModels.Read markRead(UUID societyId, UUID userId, UserRole role, UUID noticeId) {
        requireVisible(societyId, userId, role, noticeId);
        var read =
                reads.findByNoticeIdAndUserId(noticeId, userId)
                        .orElseGet(
                                () -> {
                                    var created = new NoticeRead();
                                    created.setNoticeId(noticeId);
                                    created.setUserId(userId);
                                    created.setReadAt(OffsetDateTime.now());
                                    return created;
                                });
        reads.save(read);
        return new NoticeModels.Read(noticeId, true);
    }

    public NoticeModels.Acknowledgement acknowledge(
            UUID societyId, UUID userId, UserRole role, UUID noticeId) {
        var notice = requireVisible(societyId, userId, role, noticeId);
        if (!notice.isRequiresAcknowledgement()) {
            throw new IllegalStateException("This notice does not require acknowledgement");
        }
        var read =
                reads.findByNoticeIdAndUserId(noticeId, userId)
                        .orElseGet(
                                () -> {
                                    var created = new NoticeRead();
                                    created.setNoticeId(noticeId);
                                    created.setUserId(userId);
                                    created.setReadAt(OffsetDateTime.now());
                                    return created;
                                });
        read.setAcknowledgedAt(OffsetDateTime.now());
        reads.save(read);
        return new NoticeModels.Acknowledgement(noticeId, true);
    }

    private Notice requireVisible(UUID societyId, UUID userId, UserRole role, UUID noticeId) {
        boolean manager = canManage(userId, role);
        var now = OffsetDateTime.now();
        return notices.findById(noticeId)
                .filter(notice -> societyId.equals(notice.getSocietyId()))
                .filter(
                        notice ->
                                manager
                                        || (NoticeStatus.PUBLISHED.matches(notice.getStatus())
                                                && (notice.getExpiresAt() == null
                                                        || notice.getExpiresAt().isAfter(now))
                                                && visibleTo(notice.getAudience(), role)))
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found"));
    }

    private boolean canManage(UUID userId, UserRole role) {
        return role == UserRole.SOCIETY_ADMIN
                || (role == UserRole.STAFF
                        && permissions.existsByStaffUserIdAndPermissionCode(
                                userId, StaffPermissionCode.NOTICES.code()));
    }

    private void requireManager(UUID userId, UserRole role) {
        if (!canManage(userId, role)) {
            throw new AuthorizationException("Notices permission is required");
        }
    }

    private void validate(NoticeStatus status, OffsetDateTime expiresAt) {
        if (status == NoticeStatus.PUBLISHED
                && expiresAt != null
                && !expiresAt.isAfter(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Published notice expiry must be in the future");
        }
    }

    private boolean visibleTo(String audience, UserRole role) {
        return role == UserRole.SOCIETY_ADMIN
                || NoticeAudience.ALL.matches(audience)
                || (role == UserRole.STAFF && UserRole.STAFF.matches(audience))
                || (role == UserRole.RESIDENT && NoticeAudience.RESIDENTS.matches(audience));
    }

    private NoticeModels.View view(Notice notice, UUID userId, boolean manager) {
        var read = reads.findByNoticeIdAndUserId(notice.getId(), userId).orElse(null);
        return new NoticeModels.View(
                notice.getId(),
                notice.getTitle(),
                notice.getBody(),
                NoticeAudience.from(notice.getAudience()),
                NoticePriority.from(notice.getPriority()),
                notice.isPinned(),
                notice.isRequiresAcknowledgement(),
                NoticeStatus.from(notice.getStatus()),
                notice.getPublishedAt(),
                notice.getExpiresAt(),
                notice.getUpdatedAt(),
                notice.getExpiresAt() != null
                        && notice.getExpiresAt().isBefore(OffsetDateTime.now()),
                read != null,
                read == null ? null : read.getReadAt(),
                read != null && read.getAcknowledgedAt() != null,
                read == null ? null : read.getAcknowledgedAt(),
                notice.getPublishedBy(),
                users.findById(notice.getPublishedBy())
                        .map(UserAccount::getFullName)
                        .orElse("Society Admin"),
                manager ? reads.countByNoticeId(notice.getId()) : null,
                manager ? reads.countByNoticeIdAndAcknowledgedAtIsNotNull(notice.getId()) : null);
    }
}
