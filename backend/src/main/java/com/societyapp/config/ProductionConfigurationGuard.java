package com.societyapp.config;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile({"production", "prod"})
public class ProductionConfigurationGuard {
    private static final String LOCAL_JWT_SECRET =
            "local-development-secret-key-must-be-at-least-32-bytes";

    @Bean
    InitializingBean validateProductionConfiguration(
            @Value("${spring.datasource.username}") String databaseUsername,
            @Value("${spring.datasource.password}") String databasePassword,
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${app.bootstrap.super-admin-email}") String superAdminEmail,
            @Value("${app.bootstrap.super-admin-password}") String superAdminPassword,
            @Value("${app.cors-allowed-origins}") String corsAllowedOrigins,
            @Value("${app.seed-demo-data}") boolean seedDemoData,
            @Value("${app.razorpay.key-id:}") String razorpayKeyId,
            @Value("${app.razorpay.key-secret:}") String razorpayKeySecret,
            @Value("${app.razorpay.webhook-secret:}") String razorpayWebhookSecret,
            @Value("${app.kafka.enabled:false}") boolean kafkaEnabled,
            @Value("${spring.kafka.bootstrap-servers:}") String kafkaBootstrapServers,
            @Value("${app.kafka.notification-topic:}") String kafkaNotificationTopic) {
        return () ->
                validate(
                        databaseUsername,
                        databasePassword,
                        jwtSecret,
                        superAdminEmail,
                        superAdminPassword,
                        corsAllowedOrigins,
                        seedDemoData,
                        razorpayKeyId,
                        razorpayKeySecret,
                        razorpayWebhookSecret,
                        kafkaEnabled,
                        kafkaBootstrapServers,
                        kafkaNotificationTopic);
    }

    static void validate(
            String databaseUsername,
            String databasePassword,
            String jwtSecret,
            String superAdminEmail,
            String superAdminPassword,
            String corsAllowedOrigins,
            boolean seedDemoData,
            String razorpayKeyId,
            String razorpayKeySecret,
            String razorpayWebhookSecret,
            boolean kafkaEnabled,
            String kafkaBootstrapServers,
            String kafkaNotificationTopic) {
        var errors = new ArrayList<String>();
        rejectPlaceholder("database username", databaseUsername, errors);
        rejectSecret("database password", databasePassword, 16, errors);
        rejectSecret("JWT secret", jwtSecret, 32, errors);
        if (LOCAL_JWT_SECRET.equals(jwtSecret)) errors.add("JWT secret uses the local default");
        rejectPlaceholder("Super Admin email", superAdminEmail, errors);
        if (superAdminEmail != null && superAdminEmail.trim().toLowerCase().endsWith(".local")) {
            errors.add("Super Admin email must not use the local development domain");
        }
        rejectSecret("Super Admin password", superAdminPassword, 12, errors);
        if ("Admin@12345".equals(superAdminPassword)) {
            errors.add("Super Admin password uses the local default");
        }
        validateCors(corsAllowedOrigins, errors);
        if (seedDemoData) errors.add("demo-data seeding must be disabled");
        validateRazorpay(razorpayKeyId, razorpayKeySecret, razorpayWebhookSecret, errors);
        validateKafka(kafkaEnabled, kafkaBootstrapServers, kafkaNotificationTopic, errors);
        if (!errors.isEmpty()) {
            throw new IllegalStateException(
                    "Unsafe production configuration: " + String.join("; ", errors));
        }
    }

    private static void rejectPlaceholder(String name, String value, List<String> errors) {
        if (isBlankOrPlaceholder(value)) errors.add(name + " is missing or is a placeholder");
    }

    private static void rejectSecret(
            String name, String value, int minimumBytes, List<String> errors) {
        if (isBlankOrPlaceholder(value)) {
            errors.add(name + " is missing or is a placeholder");
        } else if (value.getBytes(StandardCharsets.UTF_8).length < minimumBytes) {
            errors.add(name + " must contain at least " + minimumBytes + " bytes");
        }
    }

    private static boolean isBlankOrPlaceholder(String value) {
        if (value == null || value.isBlank()) return true;
        var normalized = value.trim().toLowerCase();
        return normalized.contains("replace-me")
                || normalized.contains("replace-with")
                || normalized.contains("changeme")
                || normalized.equals("postgres")
                || normalized.equals("password");
    }

    private static void validateCors(String origins, List<String> errors) {
        if (origins == null || origins.isBlank()) {
            errors.add("CORS allowed origins are missing");
            return;
        }
        for (String origin : origins.split(",")) {
            var normalized = origin.trim().toLowerCase();
            if (normalized.isEmpty()
                    || normalized.equals("*")
                    || normalized.contains("localhost")
                    || normalized.contains("127.0.0.1")
                    || !normalized.startsWith("https://")) {
                errors.add("CORS origins must be explicit production HTTPS origins");
                return;
            }
        }
    }

    private static void validateRazorpay(
            String keyId, String keySecret, String webhookSecret, List<String> errors) {
        int configured = 0;
        if (keyId != null && !keyId.isBlank()) configured++;
        if (keySecret != null && !keySecret.isBlank()) configured++;
        if (webhookSecret != null && !webhookSecret.isBlank()) configured++;
        if (configured > 0 && configured < 3) {
            errors.add(
                    "Razorpay must be disabled or configured with key ID, key secret, and webhook secret");
        }
        if (configured == 3 && !keyId.startsWith("rzp_live_")) {
            errors.add("production Razorpay key ID must use live mode");
        }
        if (configured == 3) {
            rejectSecret("Razorpay key secret", keySecret, 16, errors);
            rejectSecret("Razorpay webhook secret", webhookSecret, 16, errors);
        }
    }

    private static void validateKafka(
            boolean enabled, String bootstrapServers, String topic, List<String> errors) {
        if (!enabled) return;
        if (isBlankOrPlaceholder(bootstrapServers)
                || bootstrapServers.toLowerCase().contains("localhost")
                || bootstrapServers.contains("127.0.0.1")) {
            errors.add("Kafka bootstrap servers must identify production brokers");
        }
        if (isBlankOrPlaceholder(topic)) {
            errors.add("Kafka notification topic is missing or is a placeholder");
        }
    }
}
