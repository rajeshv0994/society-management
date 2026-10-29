package com.societyapp.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class HttpRazorpayGatewayTest {
    @Test
    void verifiesCheckoutAndWebhookHmacWithoutExposingSecrets() throws Exception {
        var gateway =
                new HttpRazorpayGateway(
                        "rzp_test_public",
                        "checkout-secret",
                        "webhook-secret",
                        "https://unused",
                        new ObjectMapper());

        assertThat(
                        gateway.verifyPaymentSignature(
                                "order_1", "pay_1", hmac("order_1|pay_1", "checkout-secret")))
                .isTrue();
        assertThat(
                        gateway.verifyWebhookSignature(
                                "{\"event\":\"payment.captured\"}",
                                hmac("{\"event\":\"payment.captured\"}", "webhook-secret")))
                .isTrue();
        assertThat(gateway.verifyPaymentSignature("order_1", "pay_1", "bad")).isFalse();
        assertThat(gateway.mode()).isEqualTo("TEST");
    }

    private String hmac(String value, String secret) throws Exception {
        var mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }
}
