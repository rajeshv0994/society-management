package com.societyapp.payment;

import com.societyapp.application.port.out.RazorpayGateway;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class HttpRazorpayGateway implements RazorpayGateway {
    private final String keyId;
    private final String keySecret;
    private final String webhookSecret;
    private final String apiBase;
    private final HttpClient http;
    private final ObjectMapper json;

    public HttpRazorpayGateway(
            @Value("${app.razorpay.key-id:}") String keyId,
            @Value("${app.razorpay.key-secret:}") String keySecret,
            @Value("${app.razorpay.webhook-secret:}") String webhookSecret,
            @Value("${app.razorpay.api-base:https://api.razorpay.com/v1}") String apiBase,
            ObjectMapper json) {
        this.keyId = keyId == null ? "" : keyId.trim();
        this.keySecret = keySecret == null ? "" : keySecret.trim();
        this.webhookSecret = webhookSecret == null ? "" : webhookSecret.trim();
        this.apiBase = apiBase.replaceAll("/+$", "");
        this.json = json;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Override
    public boolean configured() {
        return !keyId.isBlank() && !keySecret.isBlank() && !webhookSecret.isBlank();
    }

    @Override
    public String keyId() {
        return keyId;
    }

    @Override
    public String mode() {
        return keyId.startsWith("rzp_live_")
                ? "LIVE"
                : keyId.startsWith("rzp_test_") ? "TEST" : "UNCONFIGURED";
    }

    @Override
    public ProviderOrder createOrder(
            long amountPaise, String currency, String receipt, Map<String, String> notes) {
        ensureConfigured();
        try {
            var body =
                    json.writeValueAsString(
                            Map.of(
                                    "amount", amountPaise,
                                    "currency", currency,
                                    "receipt", receipt,
                                    "notes", notes));
            JsonNode value = send("POST", "/orders", body);
            return new ProviderOrder(
                    required(value, "id"), value.path("amount").asLong(),
                    required(value, "currency"), required(value, "status"));
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to create Razorpay order", exception);
        }
    }

    @Override
    public ProviderPayment fetchPayment(String paymentId) {
        ensureConfigured();
        try {
            JsonNode value = send("GET", "/payments/" + paymentId, null);
            return new ProviderPayment(
                    required(value, "id"),
                    required(value, "order_id"),
                    value.path("amount").asLong(),
                    required(value, "currency"),
                    required(value, "status"),
                    value.path("method").asText(null),
                    value.path("captured").asBoolean(false),
                    value.path("error_code").asText(null),
                    value.path("error_description").asText(null));
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to fetch Razorpay payment", exception);
        }
    }

    @Override
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        return !keySecret.isBlank()
                && signatureMatches(orderId + "|" + paymentId, signature, keySecret);
    }

    @Override
    public boolean verifyWebhookSignature(String rawBody, String signature) {
        return !webhookSecret.isBlank() && signatureMatches(rawBody, signature, webhookSecret);
    }

    private JsonNode send(String method, String path, String body) throws Exception {
        var builder =
                HttpRequest.newBuilder(URI.create(apiBase + path))
                        .timeout(Duration.ofSeconds(20))
                        .header(
                                "Authorization",
                                "Basic "
                                        + Base64.getEncoder()
                                                .encodeToString(
                                                        (keyId + ":" + keySecret)
                                                                .getBytes(StandardCharsets.UTF_8)))
                        .header("Accept", "application/json");
        if (body == null) builder.GET();
        else
            builder.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(body));
        var response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException(
                    "Razorpay request failed (HTTP " + response.statusCode() + ")");
        }
        return json.readTree(response.body());
    }

    private void ensureConfigured() {
        if (!configured()) throw new IllegalStateException("Razorpay is not configured");
    }

    private String required(JsonNode node, String field) {
        var value = node.path(field).asText();
        if (value.isBlank())
            throw new IllegalStateException("Razorpay response is missing " + field);
        return value;
    }

    private boolean signatureMatches(String payload, String signature, String secret) {
        if (signature == null || signature.isBlank()) return false;
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            var expected =
                    java.util.HexFormat.of()
                            .formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
            return java.security.MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.US_ASCII),
                    signature.trim().toLowerCase().getBytes(StandardCharsets.US_ASCII));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to validate Razorpay signature", exception);
        }
    }
}
