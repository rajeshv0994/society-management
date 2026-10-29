package com.societyapp.application.usecase;

import com.societyapp.application.mapper.PlatformNotificationViewMapper;
import com.societyapp.application.model.platform.PlatformNotificationView;
import com.societyapp.application.model.platform.RetryNotificationResult;
import com.societyapp.application.port.in.PlatformNotificationUseCase;
import com.societyapp.application.port.out.persistence.NotificationOutboxRepository;
import com.societyapp.application.port.out.persistence.SocietyRepository;
import com.societyapp.domain.model.Society;
import com.societyapp.domain.type.NotificationStatus;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PlatformNotificationService implements PlatformNotificationUseCase {
    private static final int RESULT_LIMIT = 500;

    private final NotificationOutboxRepository notifications;
    private final SocietyRepository societies;
    private final PlatformNotificationViewMapper mapper;

    public PlatformNotificationService(
            NotificationOutboxRepository notifications,
            SocietyRepository societies,
            PlatformNotificationViewMapper mapper) {
        this.notifications = notifications;
        this.societies = societies;
        this.mapper = mapper;
    }

    @Override
    public List<PlatformNotificationView> find(NotificationStatus status) {
        var selected =
                notifications.findAllByOrderByCreatedAtDesc().stream()
                        .filter(item -> status == null || status.matches(item.getStatus()))
                        .limit(RESULT_LIMIT)
                        .toList();
        return withSocietyNames(selected);
    }

    @Override
    public List<PlatformNotificationView> findBySociety(UUID societyId) {
        String societyName = societies.findById(societyId).map(Society::getName).orElse(null);
        return notifications.findBySocietyIdOrderByCreatedAtDesc(societyId).stream()
                .map(item -> mapper.toView(item, societyName))
                .toList();
    }

    @Override
    public long failedCount() {
        return notifications.findAll().stream()
                .filter(item -> NotificationStatus.FAILED.matches(item.getStatus()))
                .count();
    }

    private List<PlatformNotificationView> withSocietyNames(
            List<com.societyapp.domain.model.NotificationOutbox> selected) {
        Map<UUID, Society> societyById =
                societies
                        .findAllById(
                                selected.stream()
                                        .map(item -> item.getSocietyId())
                                        .filter(Objects::nonNull)
                                        .distinct()
                                        .toList())
                        .stream()
                        .collect(Collectors.toMap(Society::getId, Function.identity()));
        return selected.stream()
                .map(
                        item -> {
                            var society = societyById.get(item.getSocietyId());
                            return mapper.toView(item, society == null ? null : society.getName());
                        })
                .toList();
    }

    @Override
    public RetryNotificationResult retry(UUID notificationId) {
        var notification =
                notifications
                        .findById(notificationId)
                        .filter(item -> NotificationStatus.FAILED.matches(item.getStatus()))
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Only failed notifications can be retried"));
        notification.setStatus(NotificationStatus.PENDING.code());
        notification.setLastError(null);
        notifications.save(notification);
        return new RetryNotificationResult(notificationId, NotificationStatus.PENDING);
    }
}
