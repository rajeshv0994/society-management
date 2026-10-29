package com.societyapp.application.service;

import com.societyapp.application.command.PlatformPaymentCoordinator;
import com.societyapp.application.command.PlatformPaymentSettingsCoordinator;
import com.societyapp.application.exception.ConflictException;
import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.mapper.PlatformBillingViewMapper;
import com.societyapp.application.model.platformbilling.PlatformBillingModels;
import com.societyapp.application.port.in.PlatformMembershipBillingUseCase;
import com.societyapp.application.port.in.StorageMeterUseCase;
import com.societyapp.application.port.in.SubscriptionQuantityUseCase;
import com.societyapp.application.port.out.JsonCodec;
import com.societyapp.application.port.out.JsonCodecException;
import com.societyapp.application.port.out.PlatformInvoiceRenderer;
import com.societyapp.application.port.out.RazorpayGateway;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.query.PlatformOrderQuery;
import com.societyapp.application.usecase.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.BillingCycle;
import com.societyapp.domain.type.PaymentMethod;
import com.societyapp.domain.type.PaymentProvider;
import com.societyapp.domain.type.PaymentStatus;
import com.societyapp.domain.type.SubscriptionStatus;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PlatformMembershipBillingService implements PlatformMembershipBillingUseCase {
    private static final Set<PaymentMethod> METHODS =
            Set.of(
                    PaymentMethod.BANK_TRANSFER,
                    PaymentMethod.UPI,
                    PaymentMethod.CARD,
                    PaymentMethod.CHEQUE);
    private final SubscriptionOrderRepository orders;
    private final SocietyRepository societies;
    private final SubscriptionQuantityUseCase quantities;
    private final PlatformPaymentSettingRepository paymentSettings;
    private final StorageMeterUseCase storage;
    private final JsonCodec json;
    private final PlatformPaymentAttemptRepository attempts;
    private final PlatformInvoiceRenderer invoiceRenderer;
    private final PlatformBillingViewMapper views;
    private final PlatformOrderQuery orderQueries;
    private final PlatformPaymentCoordinator onlinePayments;
    private final PlatformPaymentSettingsCoordinator paymentSettingsCommands;

    public PlatformMembershipBillingService(
            SubscriptionOrderRepository orders,
            SocietyRepository societies,
            SubscriptionQuantityUseCase quantities,
            PlatformPaymentSettingRepository paymentSettings,
            StorageMeterUseCase storage,
            JsonCodec json,
            PlatformPaymentAttemptRepository attempts,
            RazorpayWebhookEventRepository webhookEvents,
            RazorpayGateway razorpay,
            PlatformInvoiceRenderer invoiceRenderer) {
        this.orders = orders;
        this.societies = societies;
        this.quantities = quantities;
        this.paymentSettings = paymentSettings;
        this.storage = storage;
        this.json = json;
        this.attempts = attempts;
        this.invoiceRenderer = invoiceRenderer;
        this.views = new PlatformBillingViewMapper(attempts, razorpay, json);
        this.orderQueries = new PlatformOrderQuery(orders, societies, views);
        this.onlinePayments =
                new PlatformPaymentCoordinator(
                        orders, societies, attempts, webhookEvents, razorpay, json, views);
        this.paymentSettingsCommands =
                new PlatformPaymentSettingsCoordinator(paymentSettings, views);
    }

    @Override
    public List<PlatformBillingModels.OrderView> societyOrders(UUID societyId) {
        return orderQueries.forSociety(societyId);
    }

    @Override
    public List<PlatformBillingModels.OrderView> allOrders() {
        return orderQueries.all();
    }

    @Override
    public PlatformBillingModels.PaymentSettingView paymentSettings() {
        return paymentSettings
                .findFirstByOrderByUpdatedAtDesc()
                .map(views::paymentSetting)
                .orElse(null);
    }

    @Override
    public PlatformBillingModels.PaymentSettingView updatePaymentSettings(
            UUID actorId, PlatformBillingModels.PaymentSettingsCommand command) {
        return paymentSettingsCommands.update(actorId, command);
    }

    @Override
    public PlatformBillingModels.OrderView issue(
            UUID societyId, LocalDate billingMonth, LocalDate dueDate) {
        var society =
                societies
                        .findById(societyId)
                        .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
        billingMonth = billingMonth.withDayOfMonth(1);
        if (orders.existsBySocietyIdAndBillingMonthAndStatusNot(
                societyId, billingMonth, PaymentStatus.CANCELLED.code())) {
            throw new ConflictException(
                    "A platform membership invoice already exists for this month");
        }
        if (dueDate.isBefore(billingMonth)) {
            throw new IllegalArgumentException("Due date cannot be before the billing month");
        }
        var pricing = quantities.get(societyId);
        String billingCycle =
                society.getSubscriptionBillingCycle() == null
                        ? BillingCycle.MONTHLY.code()
                        : society.getSubscriptionBillingCycle();
        int cycleMonths = cycleMonths(billingCycle);
        var order = new SubscriptionOrder();
        order.setSocietyId(societyId);
        order.setPlanCode(society.getPlanCode());
        order.setInvoiceNumber(
                "SUB-"
                        + billingMonth.format(DateTimeFormatter.ofPattern("yyyyMM"))
                        + "-"
                        + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setBillingMonth(billingMonth);
        order.setDueDate(dueDate);
        order.setBillingCycle(billingCycle);
        order.setPeriodStart(billingMonth);
        order.setPeriodEnd(billingMonth.plusMonths(cycleMonths).minusDays(1));
        BigDecimal storageMonthly = storage.monthlyAmount(societyId);
        BigDecimal storageCycle = storageMonthly.multiply(BigDecimal.valueOf(cycleMonths));
        order.setAmount(pricing.billingCycleAmount().add(storageCycle));
        var breakdown = new ArrayList<Object>(pricing.pricingBreakdown());
        if (storageMonthly.signum() > 0) {
            breakdown.add(
                    Map.of(
                            "code",
                            "STORAGE_ADDON",
                            "name",
                            "Additional storage",
                            "monthly_amount",
                            storageMonthly,
                            "cycle_amount",
                            storageCycle));
        }
        order.setPricingBreakdown(writeJson(breakdown));
        order.setCurrency("INR");
        order.setProvider(PaymentProvider.MANUAL.code());
        order.setStatus(PaymentStatus.DUE.code());
        order.setCreatedAt(OffsetDateTime.now());
        order = orders.save(order);
        return views.order(order);
    }

    @Override
    public PlatformBillingModels.OrderView submit(
            UUID societyId, UUID orderId, PlatformBillingModels.SubmitCommand command) {
        var order =
                orders.findByIdAndSocietyId(orderId, societyId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Subscription invoice not found"));
        if (!Set.of(PaymentStatus.DUE.code(), PaymentStatus.REJECTED.code())
                .contains(order.getStatus())) {
            throw new IllegalStateException("Only due or rejected invoices can be submitted");
        }
        var requestedMethod = command.method();
        if (!METHODS.contains(requestedMethod)) {
            throw new IllegalArgumentException("Invalid payment method");
        }
        if (command.reference() == null || command.reference().isBlank()) {
            throw new IllegalArgumentException("Payment transaction reference is required");
        }
        order.setPaymentMethod(requestedMethod.code());
        order.setProvider(PaymentProvider.from(requestedMethod.code()).code());
        order.setProviderReference(command.reference().trim().toUpperCase());
        order.setPaymentNote(
                command.note() == null || command.note().isBlank() ? null : command.note().trim());
        order.setSubmittedAt(OffsetDateTime.now());
        order.setReviewedAt(null);
        order.setReviewedBy(null);
        order.setRejectionReason(null);
        order.setStatus(PaymentStatus.SUBMITTED.code());
        return views.order(orders.save(order));
    }

    @Override
    public PlatformBillingModels.Checkout checkout(
            UUID societyId, UUID orderId, PaymentMethod method) {
        return onlinePayments.checkout(societyId, orderId, method);
    }

    @Override
    public PlatformBillingModels.OrderView verifyCheckout(
            UUID societyId, UUID orderId, PlatformBillingModels.VerifyCommand command) {
        return onlinePayments.verify(
                societyId,
                orderId,
                command.providerOrderId(),
                command.paymentId(),
                command.signature());
    }

    @Override
    public PlatformBillingModels.OrderView reconcile(UUID orderId) {
        return onlinePayments.reconcile(orderId);
    }

    @Override
    public PlatformBillingModels.OrderView cancel(UUID actorId, UUID orderId, String reason) {
        var order =
                orders.findById(orderId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Subscription invoice not found"));
        if (!PaymentStatus.DUE.matches(order.getStatus())) {
            throw new IllegalStateException(
                    "Only a due invoice with no payment in progress can be cancelled");
        }
        if (!attempts.findBySubscriptionOrderIdOrderByCreatedAtDesc(orderId).isEmpty()) {
            throw new IllegalStateException(
                    "This invoice has a Razorpay attempt; reconcile it before cancellation");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Cancellation reason is required");
        }
        order.setStatus(PaymentStatus.CANCELLED.code());
        order.setReviewedBy(actorId);
        order.setReviewedAt(OffsetDateTime.now());
        order.setRejectionReason(reason.trim());
        return views.order(orders.save(order));
    }

    @Override
    public boolean processWebhook(String eventId, String signature, String rawBody) {
        return onlinePayments.processWebhook(eventId, signature, rawBody);
    }

    @Override
    public PlatformBillingModels.GatewayStatus gatewayStatus() {
        return onlinePayments.status();
    }

    @Override
    public byte[] societyInvoice(UUID societyId, UUID orderId) {
        var order =
                orders.findByIdAndSocietyId(orderId, societyId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Subscription invoice not found"));
        return invoicePdf(order);
    }

    @Override
    public byte[] adminInvoice(UUID orderId) {
        var order =
                orders.findById(orderId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Subscription invoice not found"));
        return invoicePdf(order);
    }

    @Override
    public PlatformBillingModels.OrderView review(
            UUID actorId, UUID orderId, PlatformBillingModels.ReviewCommand command) {
        var order =
                orders.findById(orderId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Subscription invoice not found"));
        if (!PaymentStatus.SUBMITTED.matches(order.getStatus())) {
            throw new IllegalStateException("Only submitted payments can be reviewed");
        }
        if (!Set.of(PaymentStatus.PAID, PaymentStatus.REJECTED).contains(command.status())) {
            throw new IllegalArgumentException("Review status must be PAID or REJECTED");
        }
        if (command.status() == PaymentStatus.REJECTED
                && (command.rejectionReason() == null || command.rejectionReason().isBlank())) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        order.setStatus(command.status().code());
        order.setReviewedBy(actorId);
        order.setReviewedAt(OffsetDateTime.now());
        order.setRejectionReason(
                command.status() == PaymentStatus.REJECTED
                        ? command.rejectionReason().trim()
                        : null);
        if (command.status() == PaymentStatus.PAID) {
            order.setPaidAt(OffsetDateTime.now());
            activateMembership(order, OffsetDateTime.now());
        }
        return views.order(orders.save(order));
    }

    private void activateMembership(SubscriptionOrder order, OffsetDateTime now) {
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

    private byte[] invoicePdf(SubscriptionOrder order) {
        var society =
                societies
                        .findById(order.getSocietyId())
                        .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
        return invoiceRenderer.render(
                order, society, views.readBreakdown(order.getPricingBreakdown()));
    }

    private String writeJson(Object value) {
        try {
            return json.write(value);
        } catch (JsonCodecException exception) {
            throw new IllegalStateException("Could not snapshot invoice pricing", exception);
        }
    }

    private int cycleMonths(String cycle) {
        return switch (BillingCycle.from(cycle == null ? BillingCycle.MONTHLY.code() : cycle)) {
            case QUARTERLY -> 3;
            case YEARLY -> 12;
            case MONTHLY -> 1;
        };
    }
}
