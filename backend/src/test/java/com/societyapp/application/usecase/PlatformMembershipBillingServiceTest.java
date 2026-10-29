package com.societyapp.application.usecase;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.societyapp.application.exception.ConflictException;
import com.societyapp.application.model.platformbilling.PlatformBillingModels;
import com.societyapp.application.model.subscription.SubscriptionQuantityModels;
import com.societyapp.application.port.in.SubscriptionQuantityUseCase;
import com.societyapp.application.port.out.PlatformInvoiceRenderer;
import com.societyapp.application.port.out.RazorpayGateway;
import com.societyapp.application.port.out.persistence.PlatformPaymentAttemptRepository;
import com.societyapp.application.port.out.persistence.PlatformPaymentSettingRepository;
import com.societyapp.application.port.out.persistence.RazorpayWebhookEventRepository;
import com.societyapp.application.port.out.persistence.SocietyRepository;
import com.societyapp.application.port.out.persistence.SubscriptionOrderRepository;
import com.societyapp.application.service.PlatformMembershipBillingService;
import com.societyapp.domain.model.PlatformPaymentAttempt;
import com.societyapp.domain.model.Society;
import com.societyapp.domain.model.SubscriptionOrder;
import com.societyapp.domain.type.BillingCycle;
import com.societyapp.domain.type.PaymentMethod;
import com.societyapp.domain.type.PricingModel;
import com.societyapp.domain.type.SubscriptionStatus;
import com.societyapp.infrastructure.serialization.JacksonJsonCodec;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

class PlatformMembershipBillingServiceTest {
    private final UUID societyId = UUID.randomUUID();
    private SubscriptionOrderRepository orders;
    private SocietyRepository societies;
    private SubscriptionQuantityUseCase quantities;
    private PlatformMembershipBillingService service;
    private StorageMeterService storage;
    private PlatformPaymentAttemptRepository attempts;
    private RazorpayGateway razorpay;
    private RazorpayWebhookEventRepository webhookEvents;

    @BeforeEach
    void setUp() {
        orders = mock(SubscriptionOrderRepository.class);
        societies = mock(SocietyRepository.class);
        quantities = mock(SubscriptionQuantityUseCase.class);
        var paymentSettings = mock(PlatformPaymentSettingRepository.class);
        storage = mock(StorageMeterService.class);
        attempts = mock(PlatformPaymentAttemptRepository.class);
        razorpay = mock(RazorpayGateway.class);
        webhookEvents = mock(RazorpayWebhookEventRepository.class);
        service =
                new PlatformMembershipBillingService(
                        orders,
                        societies,
                        quantities,
                        paymentSettings,
                        storage,
                        new JacksonJsonCodec(new ObjectMapper()),
                        attempts,
                        webhookEvents,
                        razorpay,
                        mock(PlatformInvoiceRenderer.class));
        var society = new Society();
        society.setPlanCode("STANDARD");
        society.setSubscriptionBillingCycle("QUARTERLY");
        when(societies.findById(societyId)).thenReturn(Optional.of(society));
        when(orders.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(storage.monthlyAmount(societyId)).thenReturn(new BigDecimal("50"));
        when(attempts.findBySubscriptionOrderIdOrderByCreatedAtDesc(any())).thenReturn(List.of());
    }

    @Test
    void invoiceUsesCompleteCycleTotalAndSnapshotsServiceLines() {
        var breakdown =
                List.of(
                        new SubscriptionQuantityModels.PricingLine(
                                "BASE_SPACE",
                                "Base space subscription",
                                10,
                                null,
                                new BigDecimal("10"),
                                null,
                                new BigDecimal("100")),
                        new SubscriptionQuantityModels.PricingLine(
                                "AMENITIES",
                                "Amenities",
                                null,
                                10,
                                null,
                                new BigDecimal("2.50"),
                                new BigDecimal("25")));
        when(quantities.get(societyId))
                .thenReturn(
                        new SubscriptionQuantityModels.View(
                                "STANDARD",
                                10,
                                2,
                                10,
                                2,
                                BigDecimal.TEN,
                                new BigDecimal("50"),
                                BigDecimal.ZERO,
                                PricingModel.USAGE_BASED,
                                100,
                                20,
                                new BigDecimal("125"),
                                BigDecimal.ZERO,
                                new BigDecimal("125"),
                                BillingCycle.QUARTERLY,
                                3,
                                new BigDecimal("375"),
                                SubscriptionStatus.ACTIVE,
                                null,
                                List.of(),
                                List.of(),
                                breakdown));

        var result = service.issue(societyId, LocalDate.of(2026, 8, 15), LocalDate.of(2026, 8, 20));

        var captor = ArgumentCaptor.forClass(SubscriptionOrder.class);
        verify(orders).save(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.getBillingMonth()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(saved.getPeriodEnd()).isEqualTo(LocalDate.of(2026, 10, 31));
        assertThat(saved.getAmount()).isEqualByComparingTo("525");
        assertThat(saved.getPricingBreakdown()).contains("AMENITIES", "STORAGE_ADDON", "150");
        assertThat(result.pricingBreakdown()).hasSize(3);
    }

    @Test
    void duplicateBillingMonthIsRejectedBeforeRecalculation() {
        var month = LocalDate.of(2026, 8, 1);
        when(orders.existsBySocietyIdAndBillingMonthAndStatusNot(societyId, month, "CANCELLED"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.issue(societyId, month, month.plusDays(5)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
        verifyNoInteractions(quantities);
    }

    @Test
    void dueDateBeforeBillingMonthIsRejected() {
        var month = LocalDate.of(2026, 8, 1);

        assertThatThrownBy(() -> service.issue(societyId, month, month.minusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be before");
    }

    @Test
    void dueInvoiceCanBeCancelledAndRetainedAsHistory() {
        var order = paymentOrder("DUE");
        var actorId = UUID.randomUUID();
        when(orders.findById(order.getId())).thenReturn(Optional.of(order));

        service.cancel(actorId, order.getId(), "Incorrect quantities");

        assertThat(order.getStatus()).isEqualTo("CANCELLED");
        assertThat(order.getReviewedBy()).isEqualTo(actorId);
        assertThat(order.getReviewedAt()).isNotNull();
        assertThat(order.getRejectionReason()).isEqualTo("Incorrect quantities");
        verify(orders).save(order);
    }

    @Test
    void paymentPendingInvoiceCannotBeCancelled() {
        var order = paymentOrder("PAYMENT_PENDING");
        when(orders.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.cancel(UUID.randomUUID(), order.getId(), "Cancel"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("payment in progress");
    }

    @Test
    void checkoutCreatesProviderOrderButDoesNotActivateMembership() {
        var order = paymentOrder("DUE");
        when(orders.findByIdAndSocietyId(order.getId(), societyId)).thenReturn(Optional.of(order));
        when(razorpay.configured()).thenReturn(true);
        when(razorpay.keyId()).thenReturn("rzp_test_public");
        when(razorpay.mode()).thenReturn("TEST");
        when(razorpay.createOrder(anyLong(), eq("INR"), eq("SUB-TEST"), any()))
                .thenReturn(
                        new RazorpayGateway.ProviderOrder("order_123", 12500, "INR", "created"));
        when(attempts.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.checkout(societyId, order.getId(), PaymentMethod.UPI);

        assertThat(order.getStatus()).isEqualTo("PAYMENT_PENDING");
        assertThat(order.getPaidAt()).isNull();
        assertThat(result.providerOrderId()).isEqualTo("order_123");
        verify(societies, never()).save(any());
    }

    @Test
    void capturedVerificationActivatesExactlyOnce() {
        var order = paymentOrder("PAYMENT_PENDING");
        var attempt = new PlatformPaymentAttempt();
        attempt.setSubscriptionOrderId(order.getId());
        attempt.setProviderOrderId("order_123");
        attempt.setAmount(order.getAmount());
        attempt.setCurrency("INR");
        attempt.setStatus("CREATED");
        when(orders.findByIdAndSocietyId(order.getId(), societyId)).thenReturn(Optional.of(order));
        when(attempts.findByProviderOrderId("order_123")).thenReturn(Optional.of(attempt));
        when(attempts.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(razorpay.verifyPaymentSignature("order_123", "pay_123", "valid")).thenReturn(true);
        when(razorpay.fetchPayment("pay_123"))
                .thenReturn(
                        new RazorpayGateway.ProviderPayment(
                                "pay_123",
                                "order_123",
                                12500,
                                "INR",
                                "captured",
                                "upi",
                                true,
                                null,
                                null));

        var verification = new PlatformBillingModels.VerifyCommand("order_123", "pay_123", "valid");
        service.verifyCheckout(societyId, order.getId(), verification);
        service.verifyCheckout(societyId, order.getId(), verification);

        assertThat(order.getStatus()).isEqualTo("PAID");
        assertThat(order.getProviderReference()).isEqualTo("pay_123");
        var societyCaptor = ArgumentCaptor.forClass(Society.class);
        verify(societies, times(1)).save(societyCaptor.capture());
        assertThat(societyCaptor.getValue().getSubscriptionStatus()).isEqualTo("ACTIVE");
        assertThat(societyCaptor.getValue().getSubscriptionExpiresAt()).isNotNull();
    }

    @Test
    void invalidSignatureCannotMarkInvoicePaid() {
        var order = paymentOrder("PAYMENT_PENDING");
        var attempt = new PlatformPaymentAttempt();
        attempt.setSubscriptionOrderId(order.getId());
        attempt.setProviderOrderId("order_123");
        when(orders.findByIdAndSocietyId(order.getId(), societyId)).thenReturn(Optional.of(order));
        when(attempts.findByProviderOrderId("order_123")).thenReturn(Optional.of(attempt));

        assertThatThrownBy(
                        () ->
                                service.verifyCheckout(
                                        societyId,
                                        order.getId(),
                                        new PlatformBillingModels.VerifyCommand(
                                                "order_123", "pay_123", "invalid")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("signature");
        assertThat(order.getStatus()).isEqualTo("PAYMENT_PENDING");
        verify(razorpay, never()).fetchPayment(any());
    }

    @Test
    void capturedWebhookIsIdempotent() {
        var order = paymentOrder("PAYMENT_PENDING");
        var attempt = new PlatformPaymentAttempt();
        attempt.setSubscriptionOrderId(order.getId());
        attempt.setProviderOrderId("order_123");
        attempt.setAmount(order.getAmount());
        attempt.setCurrency("INR");
        attempt.setStatus("CREATED");
        when(razorpay.verifyWebhookSignature(any(), eq("valid"))).thenReturn(true);
        when(webhookEvents.claim("event_1", "payment.captured")).thenReturn(1, 0);
        when(attempts.findByProviderOrderId("order_123")).thenReturn(Optional.of(attempt));
        when(orders.findById(order.getId())).thenReturn(Optional.of(order));
        when(attempts.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        String body =
                """
                {"event":"payment.captured","payload":{"payment":{"entity":{
                  "id":"pay_123","order_id":"order_123","amount":12500,
                  "currency":"INR","status":"captured","method":"upi","captured":true
                }}}}
                """;

        assertThat(service.processWebhook("event_1", "valid", body)).isTrue();
        assertThat(service.processWebhook("event_1", "valid", body)).isFalse();

        assertThat(order.getStatus()).isEqualTo("PAID");
        verify(societies, times(1)).save(any());
        verify(webhookEvents, times(1)).markProcessed("event_1");
    }

    private SubscriptionOrder paymentOrder(String status) {
        var order = new SubscriptionOrder();
        ReflectionTestUtils.setField(order, "id", UUID.randomUUID());
        order.setSocietyId(societyId);
        order.setInvoiceNumber("SUB-TEST");
        order.setBillingMonth(LocalDate.of(2026, 8, 1));
        order.setDueDate(LocalDate.of(2026, 8, 15));
        order.setBillingCycle("MONTHLY");
        order.setPeriodStart(LocalDate.of(2026, 8, 1));
        order.setPeriodEnd(LocalDate.of(2026, 8, 31));
        order.setPlanCode("STANDARD");
        order.setAmount(new BigDecimal("125.00"));
        order.setCurrency("INR");
        order.setProvider("RAZORPAY");
        order.setStatus(status);
        order.setCreatedAt(java.time.OffsetDateTime.now());
        return order;
    }
}
