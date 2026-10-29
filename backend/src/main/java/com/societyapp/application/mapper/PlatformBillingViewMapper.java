package com.societyapp.application.mapper;

import com.societyapp.application.model.platformbilling.PlatformBillingModels;
import com.societyapp.application.port.out.*;
import com.societyapp.application.port.out.persistence.PlatformPaymentAttemptRepository;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public final class PlatformBillingViewMapper {
    private final PlatformPaymentAttemptRepository attempts;
    private final RazorpayGateway razorpay;
    private final JsonCodec json;

    public PlatformBillingViewMapper(
            PlatformPaymentAttemptRepository attempts, RazorpayGateway razorpay, JsonCodec json) {
        this.attempts = attempts;
        this.razorpay = razorpay;
        this.json = json;
    }

    public PlatformBillingModels.OrderView order(SubscriptionOrder order) {
        return order(order, null);
    }

    public PlatformBillingModels.OrderView order(
            SubscriptionOrder order, PlatformBillingModels.SocietySummary society) {
        return new PlatformBillingModels.OrderView(
                order.getId(),
                order.getSocietyId(),
                society,
                order.getInvoiceNumber(),
                order.getBillingMonth(),
                order.getDueDate(),
                BillingCycle.from(order.getBillingCycle()),
                order.getPeriodStart(),
                order.getPeriodEnd(),
                order.getPlanCode(),
                order.getAmount(),
                order.getCurrency(),
                PaymentStatus.from(order.getStatus()),
                paymentMethod(order.getPaymentMethod()),
                order.getProviderReference(),
                order.getPaymentNote(),
                order.getSubmittedAt(),
                order.getPaidAt(),
                order.getReviewedAt(),
                order.getRejectionReason(),
                order.getCreatedAt(),
                readBreakdown(order.getPricingBreakdown()),
                !Set.of(PaymentStatus.PAID, PaymentStatus.CANCELLED)
                                .contains(PaymentStatus.from(order.getStatus()))
                        && order.getDueDate().isBefore(LocalDate.now()),
                attempts.findBySubscriptionOrderIdOrderByCreatedAtDesc(order.getId()).stream()
                        .map(this::attempt)
                        .toList(),
                PaymentProvider.RAZORPAY.matches(order.getProvider()) ? razorpay.mode() : null);
    }

    public PlatformBillingModels.PaymentSettingView paymentSetting(PlatformPaymentSetting setting) {
        return new PlatformBillingModels.PaymentSettingView(
                setting.getBeneficiaryName(),
                setting.getBankName(),
                setting.getAccountNumber(),
                setting.getIfscCode(),
                setting.getUpiId(),
                setting.getPaymentInstructions(),
                setting.getUpdatedAt());
    }

    public List<PlatformBillingModels.PricingLine> readBreakdown(String value) {
        try {
            return json.readObjectList(value == null ? "[]" : value).stream()
                    .map(this::pricingLine)
                    .toList();
        } catch (JsonCodecException exception) {
            throw new IllegalStateException("Invoice pricing snapshot is invalid", exception);
        }
    }

    private PlatformBillingModels.PricingLine pricingLine(Map<String, Object> value) {
        return new PlatformBillingModels.PricingLine(
                text(value.get("code")),
                text(value.get("name")),
                integer(value.get("quantity")),
                integer(value.get("spaces")),
                decimal(value.get("rate")),
                decimal(first(value, "price_per_space", "pricePerSpace")),
                decimal(first(value, "monthly_amount", "monthlyAmount")),
                decimal(first(value, "cycle_amount", "cycleAmount")));
    }

    private PlatformBillingModels.PaymentAttempt attempt(PlatformPaymentAttempt value) {
        return new PlatformBillingModels.PaymentAttempt(
                value.getProviderOrderId(),
                value.getProviderPaymentId(),
                value.getAmount(),
                value.getCurrency(),
                paymentMethod(value.getRequestedMethod()),
                PaymentAttemptStatus.from(value.getStatus()),
                value.getFailureCode(),
                value.getFailureDescription(),
                value.getCreatedAt(),
                value.getUpdatedAt());
    }

    private PaymentMethod paymentMethod(String value) {
        if (value == null || value.isBlank()) return null;
        return PaymentMethod.from(value.replaceFirst("^RAZORPAY_", ""));
    }

    private String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer integer(Object value) {
        return value == null ? null : new BigDecimal(String.valueOf(value)).intValueExact();
    }

    private BigDecimal decimal(Object value) {
        return value == null ? null : new BigDecimal(String.valueOf(value));
    }

    private Object first(Map<String, Object> value, String first, String second) {
        return value.containsKey(first) ? value.get(first) : value.get(second);
    }
}
