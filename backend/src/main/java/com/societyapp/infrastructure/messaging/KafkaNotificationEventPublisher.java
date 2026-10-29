package com.societyapp.infrastructure.messaging;

import com.societyapp.application.port.out.JsonCodec;
import com.societyapp.application.port.out.NotificationEventPublisher;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaNotificationEventPublisher implements NotificationEventPublisher {
    private final KafkaTemplate<String, String> kafka;
    private final JsonCodec json;
    private final String topic;
    private final Duration sendTimeout;

    public KafkaNotificationEventPublisher(
            KafkaTemplate<String, String> kafka,
            JsonCodec json,
            String topic,
            Duration sendTimeout) {
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("Kafka notification topic is required");
        }
        if (sendTimeout == null || sendTimeout.isZero() || sendTimeout.isNegative()) {
            throw new IllegalArgumentException("Kafka send timeout must be positive");
        }
        this.kafka = kafka;
        this.json = json;
        this.topic = topic;
        this.sendTimeout = sendTimeout;
    }

    @Override
    public void publish(NotificationEvent event) {
        var body = new LinkedHashMap<String, Object>();
        body.put("event_id", event.eventId());
        body.put("event_type", "notification.requested.v1");
        body.put("society_id", event.societyId());
        body.put("channel", event.channel());
        body.put("destination", event.destination());
        body.put("template_code", event.templateCode());
        body.put("payload", json.readObject(event.payload()));
        body.put("created_at", event.createdAt());
        String key =
                event.societyId() == null
                        ? event.eventId().toString()
                        : event.societyId().toString();
        try {
            kafka.send(topic, key, json.write(body))
                    .get(sendTimeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Kafka notification publish failed", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Kafka notification publish failed", exception);
        }
    }
}
