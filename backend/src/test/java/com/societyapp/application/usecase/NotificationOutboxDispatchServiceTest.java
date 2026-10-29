package com.societyapp.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.societyapp.application.port.out.NotificationEventPublisher;
import com.societyapp.application.port.out.persistence.NotificationOutboxRepository;
import com.societyapp.domain.model.NotificationOutbox;
import java.util.List;
import org.junit.jupiter.api.Test;

class NotificationOutboxDispatchServiceTest {
    private final NotificationOutboxRepository repository =
            mock(NotificationOutboxRepository.class);
    private final NotificationEventPublisher publisher = mock(NotificationEventPublisher.class);
    private final NotificationOutboxDispatchService service =
            new NotificationOutboxDispatchService(repository, publisher);

    @Test
    void publishesAndMarksPendingNotificationSent() {
        var notification = notification(0);
        when(repository.findPendingForDispatch(25)).thenReturn(List.of(notification));

        var result = service.dispatchBatch(25, 5);

        assertThat(result.selected()).isEqualTo(1);
        assertThat(result.published()).isEqualTo(1);
        assertThat(result.failed()).isZero();
        assertThat(notification.getStatus()).isEqualTo("SENT");
        assertThat(notification.getSentAt()).isNotNull();
        verify(publisher).publish(any());
        verify(repository).save(notification);
    }

    @Test
    void recordsFailureAndStopsAutomaticRetriesAtConfiguredLimit() {
        var notification = notification(4);
        when(repository.findPendingForDispatch(25)).thenReturn(List.of(notification));
        doThrow(new IllegalStateException("broker unavailable")).when(publisher).publish(any());

        var result = service.dispatchBatch(25, 5);

        assertThat(result.failed()).isEqualTo(1);
        assertThat(notification.getAttempts()).isEqualTo(5);
        assertThat(notification.getStatus()).isEqualTo("FAILED");
        assertThat(notification.getLastError()).isEqualTo("broker unavailable");
    }

    @Test
    void rejectsUnsafeDispatcherLimits() {
        assertThatThrownBy(() -> service.dispatchBatch(0, 5))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.dispatchBatch(25, 0))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repository, publisher);
    }

    private NotificationOutbox notification(int attempts) {
        var notification = new NotificationOutbox();
        notification.setChannel("EMAIL");
        notification.setDestination("resident@example.com");
        notification.setTemplateCode("DELIVERY_RECEIVED");
        notification.setPayload("{}");
        notification.setStatus("PENDING");
        notification.setAttempts(attempts);
        return notification;
    }
}
