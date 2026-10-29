package com.societyapp.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.societyapp.application.mapper.PlatformNotificationViewMapper;
import com.societyapp.application.port.out.persistence.NotificationOutboxRepository;
import com.societyapp.application.port.out.persistence.SocietyRepository;
import com.societyapp.domain.model.NotificationOutbox;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlatformNotificationServiceTest {
    private final NotificationOutboxRepository notifications =
            mock(NotificationOutboxRepository.class);
    private final SocietyRepository societies = mock(SocietyRepository.class);
    private final PlatformNotificationService service =
            new PlatformNotificationService(
                    notifications, societies, new PlatformNotificationViewMapper());

    @Test
    void filtersNotificationsAndReturnsTypedViews() {
        var failed = notification("FAILED");
        var sent = notification("SENT");
        when(notifications.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(failed, sent));
        when(societies.findAllById(List.of())).thenReturn(List.of());

        var result = service.find(com.societyapp.domain.type.NotificationStatus.FAILED);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status())
                .isEqualTo(com.societyapp.domain.type.NotificationStatus.FAILED);
        assertThat(result.getFirst().templateCode()).isEqualTo("DELIVERY_RECEIVED");
    }

    @Test
    void retriesOnlyFailedNotifications() {
        UUID id = UUID.randomUUID();
        var failed = notification("FAILED");
        failed.setLastError("broker unavailable");
        when(notifications.findById(id)).thenReturn(Optional.of(failed));

        var result = service.retry(id);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.status())
                .isEqualTo(com.societyapp.domain.type.NotificationStatus.PENDING);
        assertThat(failed.getStatus()).isEqualTo("PENDING");
        assertThat(failed.getLastError()).isNull();
        verify(notifications).save(failed);
    }

    @Test
    void rejectsRetryForNonFailedNotification() {
        UUID id = UUID.randomUUID();
        when(notifications.findById(id)).thenReturn(Optional.of(notification("SENT")));

        assertThatThrownBy(() -> service.retry(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only failed notifications can be retried");
    }

    private NotificationOutbox notification(String status) {
        var notification = new NotificationOutbox();
        notification.setChannel("EMAIL");
        notification.setDestination("resident@example.com");
        notification.setTemplateCode("DELIVERY_RECEIVED");
        notification.setPayload("{}");
        notification.setStatus(status);
        return notification;
    }
}
