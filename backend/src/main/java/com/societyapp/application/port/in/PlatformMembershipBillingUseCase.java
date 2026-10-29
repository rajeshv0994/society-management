package com.societyapp.application.port.in;

import com.societyapp.application.model.platformbilling.PlatformBillingModels;
import com.societyapp.domain.type.PaymentMethod;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PlatformMembershipBillingUseCase {
    List<PlatformBillingModels.OrderView> societyOrders(UUID societyId);

    List<PlatformBillingModels.OrderView> allOrders();

    PlatformBillingModels.PaymentSettingView paymentSettings();

    PlatformBillingModels.PaymentSettingView updatePaymentSettings(
            UUID actorId, PlatformBillingModels.PaymentSettingsCommand command);

    PlatformBillingModels.OrderView issue(
            UUID societyId, LocalDate billingMonth, LocalDate dueDate);

    PlatformBillingModels.OrderView submit(
            UUID societyId, UUID orderId, PlatformBillingModels.SubmitCommand command);

    PlatformBillingModels.Checkout checkout(UUID societyId, UUID orderId, PaymentMethod method);

    PlatformBillingModels.OrderView verifyCheckout(
            UUID societyId, UUID orderId, PlatformBillingModels.VerifyCommand command);

    PlatformBillingModels.OrderView reconcile(UUID orderId);

    PlatformBillingModels.OrderView cancel(UUID actorId, UUID orderId, String reason);

    boolean processWebhook(String eventId, String signature, String rawBody);

    PlatformBillingModels.GatewayStatus gatewayStatus();

    byte[] societyInvoice(UUID societyId, UUID orderId);

    byte[] adminInvoice(UUID orderId);

    PlatformBillingModels.OrderView review(
            UUID actorId, UUID orderId, PlatformBillingModels.ReviewCommand command);
}
