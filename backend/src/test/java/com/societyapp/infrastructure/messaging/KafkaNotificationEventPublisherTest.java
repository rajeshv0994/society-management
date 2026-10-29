package com.societyapp.infrastructure.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.societyapp.application.port.out.JsonCodec;
import com.societyapp.application.port.out.NotificationEventPublisher.NotificationEvent;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

class KafkaNotificationEventPublisherTest {
    private final KafkaTemplate<String, String> kafka = mock(KafkaTemplate.class);
    private final JsonCodec json = mock(JsonCodec.class);
    private final KafkaNotificationEventPublisher publisher =
            new KafkaNotificationEventPublisher(
                    kafka, json, "society.notifications.v1", Duration.ofSeconds(2));

    @Test
    void publishesSerializedEventUsingSocietyAsOrderingKey() {
        UUID societyId = UUID.randomUUID();
        when(json.readObject("{}")).thenReturn(Map.of());
        when(json.write(any())).thenReturn("serialized-event");
        CompletableFuture<SendResult<String, String>> acknowledged =
                CompletableFuture.completedFuture(null);
        when(kafka.send("society.notifications.v1", societyId.toString(), "serialized-event"))
                .thenReturn(acknowledged);

        publisher.publish(event(societyId));

        verify(kafka).send("society.notifications.v1", societyId.toString(), "serialized-event");
    }

    @Test
    void translatesBrokerFailureWithoutMarkingTheThreadInterrupted() {
        UUID societyId = UUID.randomUUID();
        when(json.readObject("{}")).thenReturn(Map.of());
        when(json.write(any())).thenReturn("serialized-event");
        CompletableFuture<SendResult<String, String>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new IllegalStateException("broker unavailable"));
        when(kafka.send("society.notifications.v1", societyId.toString(), "serialized-event"))
                .thenReturn(failed);

        assertThatThrownBy(() -> publisher.publish(event(societyId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Kafka notification publish failed");
    }

    private NotificationEvent event(UUID societyId) {
        return new NotificationEvent(
                UUID.randomUUID(),
                societyId,
                "EMAIL",
                "resident@example.com",
                "DELIVERY_RECEIVED",
                "{}",
                OffsetDateTime.now());
    }
}
