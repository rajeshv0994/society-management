package com.societyapp.application.command;

import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.mapper.PlatformBillingViewMapper;
import com.societyapp.application.model.platformbilling.PlatformBillingModels;
import com.societyapp.application.port.out.JsonCodec;
import com.societyapp.application.port.out.JsonCodecException;
import com.societyapp.application.port.out.RazorpayGateway;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.time.OffsetDateTime;
import java.util.*;

public final class PlatformPaymentCoordinator {
    private final SubscriptionOrderRepository orders;
    private final SocietyRepository societies;
    private final PlatformPaymentAttemptRepository attempts;
    private final RazorpayWebhookEventRepository webhookEvents;
    private final RazorpayGateway razorpay;
    private final JsonCodec json;
    private final PlatformBillingViewMapper views;

    public PlatformPaymentCoordinator(
            SubscriptionOrderRepository orders,
            SocietyRepository societies,
            PlatformPaymentAttemptRepository attempts,
            RazorpayWebhookEventRepository webhookEvents,
            RazorpayGateway razorpay,
            JsonCodec json,
            PlatformBillingViewMapper views) {
        this.orders = orders;
        this.societies = societies;
        this.attempts = attempts;
        this.webhookEvents = webhookEvents;
        this.razorpay = razorpay;
        this.json = json;
        this.views = views;
    }

    public PlatformBillingModels.Checkout checkout(
            UUID societyId, UUID orderId, PaymentMethod requestedMethod) {
        var order = requireOrder(societyId, orderId);
        if (!Set.of(
                        PaymentStatus.DUE.code(),
                        PaymentStatus.REJECTED.code(),
                        PaymentStatus.PAYMENT_PENDING.code())
                .contains(order.getStatus())) {
            throw new IllegalStateException("This invoice cannot start a new payment");
        }
        if (!Set.of(PaymentMethod.UPI, PaymentMethod.CARD, PaymentMethod.NET_BANKING)
                .contains(requestedMethod)) {
            throw new IllegalArgumentException("Invalid Razorpay payment method");
        }
        if (!razorpay.configured()) throw new IllegalStateException("Razorpay is not configured");
        long amountPaise = order.getAmount().movePointRight(2).longValueExact();
        if (amountPaise < 100)
            throw new IllegalStateException("Invoice amount is too low for online payment");
        var providerOrder =
                razorpay.createOrder(
                        amountPaise,
                        order.getCurrency(),
                        order.getInvoiceNumber(),
                        Map.of(
                                "subscription_order_id",
                                order.getId().toString(),
                                "society_id",
                                societyId.toString()));
        if (providerOrder.amount() != amountPaise
                || !order.getCurrency().equals(providerOrder.currency())) {
            throw new IllegalStateException("Razorpay created an order with unexpected totals");
        }
        var now = OffsetDateTime.now();
        var attempt = new PlatformPaymentAttempt();
        attempt.setSubscriptionOrderId(order.getId());
        attempt.setProviderOrderId(providerOrder.id());
        attempt.setAmount(order.getAmount());
        attempt.setCurrency(order.getCurrency());
        attempt.setRequestedMethod(requestedMethod.code());
        attempt.setStatus(PaymentAttemptStatus.CREATED.code());
        attempt.setCreatedAt(now);
        attempt.setUpdatedAt(now);
        attempts.save(attempt);
        order.setProvider(PaymentProvider.RAZORPAY.code());
        order.setProviderReference(providerOrder.id());
        order.setPaymentMethod(requestedMethod.razorpayCode());
        order.setPaymentNote(null);
        order.setSubmittedAt(now);
        order.setReviewedAt(null);
        order.setRejectionReason(null);
        order.setPaidAt(null);
        order.setStatus(PaymentStatus.PAYMENT_PENDING.code());
        orders.save(order);
        var society = societies.findById(societyId).orElseThrow();
        return new PlatformBillingModels.Checkout(
                razorpay.keyId(),
                razorpay.mode(),
                providerOrder.id(),
                amountPaise,
                order.getCurrency(),
                "SocietyHub",
                "Platform membership · " + order.getInvoiceNumber(),
                new PlatformBillingModels.Prefill(
                        nullSafe(society.getContactPerson()),
                        nullSafe(society.getEmail()),
                        nullSafe(society.getMobile())));
    }

    public PlatformBillingModels.OrderView verify(
            UUID societyId,
            UUID orderId,
            String providerOrderId,
            String paymentId,
            String signature) {
        var order = requireOrder(societyId, orderId);
        var attempt =
                attempts.findByProviderOrderId(providerOrderId)
                        .filter(value -> value.getSubscriptionOrderId().equals(orderId))
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Payment attempt not found"));
        if (!razorpay.verifyPaymentSignature(attempt.getProviderOrderId(), paymentId, signature)) {
            throw new IllegalArgumentException("Razorpay payment signature is invalid");
        }
        return apply(order, attempt, razorpay.fetchPayment(paymentId));
    }

    public PlatformBillingModels.OrderView reconcile(UUID orderId) {
        var order =
                orders.findById(orderId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Subscription invoice not found"));
        var attempt =
                attempts.findBySubscriptionOrderIdOrderByCreatedAtDesc(orderId).stream()
                        .filter(value -> value.getProviderPaymentId() != null)
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No Razorpay payment is available to reconcile"));
        return apply(order, attempt, razorpay.fetchPayment(attempt.getProviderPaymentId()));
    }

    public boolean processWebhook(String eventId, String signature, String rawBody) {
        if (eventId == null || eventId.isBlank())
            throw new IllegalArgumentException("Missing Razorpay event id");
        if (!razorpay.verifyWebhookSignature(rawBody, signature)) {
            throw new IllegalArgumentException("Razorpay webhook signature is invalid");
        }
        try {
            var root = json.readTree(rawBody);
            if (webhookEvents.claim(eventId, root.path("event").asText("unknown")) == 0)
                return false;
            var payment = root.path("payload").path("payment").path("entity");
            if (!payment.isMissingNode()
                    && payment.hasNonNull("id")
                    && payment.hasNonNull("order_id")) {
                attempts.findByProviderOrderId(payment.path("order_id").asText())
                        .ifPresent(
                                attempt ->
                                        apply(
                                                orders.findById(attempt.getSubscriptionOrderId())
                                                        .orElseThrow(),
                                                attempt,
                                                new RazorpayGateway.ProviderPayment(
                                                        payment.path("id").asText(),
                                                        payment.path("order_id").asText(),
                                                        payment.path("amount").asLong(),
                                                        payment.path("currency").asText(),
                                                        payment.path("status").asText(),
                                                        payment.path("method").asText(null),
                                                        payment.path("captured").asBoolean(false),
                                                        payment.path("error_code").asText(null),
                                                        payment.path("error_description")
                                                                .asText(null))));
            }
            webhookEvents.markProcessed(eventId);
            return true;
        } catch (JsonCodecException exception) {
            throw new IllegalArgumentException("Razorpay webhook payload is invalid", exception);
        }
    }

    public PlatformBillingModels.GatewayStatus status() {
        return new PlatformBillingModels.GatewayStatus(
                PaymentProvider.RAZORPAY, razorpay.configured(), razorpay.mode());
    }

    private PlatformBillingModels.OrderView apply(
            SubscriptionOrder order,
            PlatformPaymentAttempt attempt,
            RazorpayGateway.ProviderPayment payment) {
        if (!attempt.getProviderOrderId().equals(payment.orderId())
                || !attempt.getCurrency().equals(payment.currency())
                || attempt.getAmount().movePointRight(2).longValueExact() != payment.amount()) {
            throw new IllegalArgumentException("Razorpay payment does not match this invoice");
        }
        var now = OffsetDateTime.now();
        attempt.setProviderPaymentId(payment.id());
        attempt.setUpdatedAt(now);
        attempt.setFailureCode(payment.errorCode());
        attempt.setFailureDescription(payment.errorDescription());
        if (payment.captured() && "captured".equalsIgnoreCase(payment.status())) {
            attempt.setStatus(PaymentStatus.CAPTURED.code());
            if (!PaymentStatus.PAID.matches(order.getStatus())) {
                order.setStatus(PaymentStatus.PAID.code());
                order.setProvider(PaymentProvider.RAZORPAY.code());
                order.setProviderReference(payment.id());
                order.setPaymentMethod(
                        payment.method() == null
                                ? order.getPaymentMethod()
                                : "RAZORPAY_" + payment.method().toUpperCase(Locale.ROOT));
                order.setPaidAt(now);
                order.setReviewedAt(now);
                activate(order, now);
            }
        } else if ("failed".equalsIgnoreCase(payment.status())) {
            attempt.setStatus(PaymentStatus.FAILED.code());
            if (!PaymentStatus.PAID.matches(order.getStatus())) {
                order.setStatus(PaymentStatus.REJECTED.code());
                order.setRejectionReason(
                        payment.errorDescription() == null
                                ? "Razorpay payment failed"
                                : payment.errorDescription());
            }
        } else {
            attempt.setStatus(PaymentStatus.AUTHORIZED.code());
            if (!PaymentStatus.PAID.matches(order.getStatus()))
                order.setStatus(PaymentStatus.PAYMENT_PENDING.code());
        }
        attempts.save(attempt);
        return views.order(orders.save(order));
    }

    private void activate(SubscriptionOrder order, OffsetDateTime now) {
        var society = societies.findById(order.getSocietyId()).orElseThrow();
        var base =
                society.getSubscriptionExpiresAt() != null
                                && society.getSubscriptionExpiresAt().isAfter(now)
                        ? society.getSubscriptionExpiresAt()
                        : now;
        society.setSubscriptionStatus(SubscriptionStatus.ACTIVE.code());
        society.setSubscriptionExpiresAt(base.plusMonths(cycleMonths(order.getBillingCycle())));
        society.setUpdatedAt(now);
        societies.save(society);
    }

    private SubscriptionOrder requireOrder(UUID societyId, UUID orderId) {
        return orders.findByIdAndSocietyId(orderId, societyId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription invoice not found"));
    }

    private int cycleMonths(String cycle) {
        return switch (BillingCycle.from(cycle == null ? BillingCycle.MONTHLY.code() : cycle)) {
            case QUARTERLY -> 3;
            case YEARLY -> 12;
            case MONTHLY -> 1;
        };
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
