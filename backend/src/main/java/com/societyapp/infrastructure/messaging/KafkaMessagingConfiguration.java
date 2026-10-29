package com.societyapp.infrastructure.messaging;

import com.societyapp.application.port.in.NotificationOutboxDispatchUseCase;
import com.societyapp.application.port.out.JsonCodec;
import com.societyapp.application.port.out.NotificationEventPublisher;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class KafkaMessagingConfiguration {
    @Bean
    @ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
    NotificationEventPublisher kafkaNotificationEventPublisher(
            KafkaTemplate<String, String> kafka,
            JsonCodec json,
            @Value("${app.kafka.notification-topic}") String topic,
            @Value("${app.kafka.send-timeout-ms}") long sendTimeoutMs) {
        return new KafkaNotificationEventPublisher(
                kafka, json, topic, Duration.ofMillis(sendTimeoutMs));
    }

    @Bean
    @ConditionalOnMissingBean(NotificationEventPublisher.class)
    NotificationEventPublisher disabledNotificationEventPublisher() {
        return event -> {
            throw new IllegalStateException("Kafka notification publishing is disabled");
        };
    }

    @Bean
    @ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
    NotificationOutboxScheduler notificationOutboxScheduler(
            NotificationOutboxDispatchUseCase dispatcher,
            @Value("${app.kafka.batch-size}") int batchSize,
            @Value("${app.kafka.max-attempts}") int maxAttempts) {
        return new NotificationOutboxScheduler(dispatcher, batchSize, maxAttempts);
    }
}
