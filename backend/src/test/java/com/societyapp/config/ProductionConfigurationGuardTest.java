package com.societyapp.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ProductionConfigurationGuardTest {
    @Test
    void acceptsSafeProductionConfigurationWithPaymentsDisabled() {
        assertThatCode(
                        () ->
                                validate(
                                        "society_service",
                                        "a-long-random-database-password",
                                        "a-production-jwt-secret-with-more-than-32-bytes",
                                        "admin@example.com",
                                        "a-long-random-admin-password",
                                        "https://society.example.com",
                                        false,
                                        "",
                                        "",
                                        "",
                                        false,
                                        "localhost:9092",
                                        "society.notifications.v1"))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsDevelopmentDefaultsAndDemoData() {
        assertThatThrownBy(
                        () ->
                                validate(
                                        "postgres",
                                        "postgres",
                                        "local-development-secret-key-must-be-at-least-32-bytes",
                                        "admin@society.local",
                                        "Admin@12345",
                                        "http://localhost:[*]",
                                        true,
                                        "",
                                        "",
                                        "",
                                        false,
                                        "localhost:9092",
                                        "society.notifications.v1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("database username")
                .hasMessageContaining("local default")
                .hasMessageContaining("local development domain")
                .hasMessageContaining("CORS")
                .hasMessageContaining("demo-data");
    }

    @Test
    void rejectsPartialOrTestModePaymentConfiguration() {
        assertThatThrownBy(
                        () ->
                                validate(
                                        "society_service",
                                        "a-long-random-database-password",
                                        "a-production-jwt-secret-with-more-than-32-bytes",
                                        "admin@example.com",
                                        "a-long-random-admin-password",
                                        "https://society.example.com",
                                        false,
                                        "rzp_test_example",
                                        "a-long-provider-secret",
                                        "a-long-webhook-secret",
                                        false,
                                        "localhost:9092",
                                        "society.notifications.v1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("live mode");

        assertThatThrownBy(
                        () ->
                                validate(
                                        "society_service",
                                        "a-long-random-database-password",
                                        "a-production-jwt-secret-with-more-than-32-bytes",
                                        "admin@example.com",
                                        "a-long-random-admin-password",
                                        "https://society.example.com",
                                        false,
                                        "rzp_live_example",
                                        "",
                                        "",
                                        false,
                                        "localhost:9092",
                                        "society.notifications.v1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("disabled or configured");
    }

    @Test
    void rejectsLocalKafkaBrokersInProductionWhenPublishingIsEnabled() {
        assertThatThrownBy(
                        () ->
                                validate(
                                        "society_service",
                                        "a-long-random-database-password",
                                        "a-production-jwt-secret-with-more-than-32-bytes",
                                        "admin@example.com",
                                        "a-long-random-admin-password",
                                        "https://society.example.com",
                                        false,
                                        "",
                                        "",
                                        "",
                                        true,
                                        "localhost:9092",
                                        "society.notifications.v1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("production brokers");
    }

    @Test
    void defaultsToLocalProfileAndJdbcDatabaseSettings() throws IOException {
        String config = Files.readString(Path.of("src/main/resources/application.yml"));

        assertThat(config)
                .contains("spring:")
                .contains("profiles:")
                .contains("default: local")
                .contains(
                        "url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/societydb}")
                .contains("username: ${SPRING_DATASOURCE_USERNAME:societyapp}")
                .contains("password: ${SPRING_DATASOURCE_PASSWORD:societyapp-dev-password}");
    }

    private static void validate(
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
        ProductionConfigurationGuard.validate(
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
}
