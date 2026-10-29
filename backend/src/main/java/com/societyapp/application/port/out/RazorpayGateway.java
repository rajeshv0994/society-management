package com.societyapp.application.port.out;

import java.util.Map;

public interface RazorpayGateway {
    record ProviderOrder(String id, long amount, String currency, String status) {}

    record ProviderPayment(
            String id,
            String orderId,
            long amount,
            String currency,
            String status,
            String method,
            boolean captured,
            String errorCode,
            String errorDescription) {}

    boolean configured();

    String keyId();

    String mode();

    ProviderOrder createOrder(
            long amountPaise, String currency, String receipt, Map<String, String> notes);

    ProviderPayment fetchPayment(String paymentId);

    boolean verifyPaymentSignature(String orderId, String paymentId, String signature);

    boolean verifyWebhookSignature(String rawBody, String signature);
}
