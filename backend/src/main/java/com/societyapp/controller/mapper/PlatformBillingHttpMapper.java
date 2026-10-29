package com.societyapp.controller.mapper;

import com.societyapp.application.model.platformbilling.PlatformBillingModels;
import com.societyapp.controller.model.PlatformBillingResponse;

public final class PlatformBillingHttpMapper {
    public PlatformBillingResponse.Order toResponse(PlatformBillingModels.OrderView value) {
        var society = value.society();
        return new PlatformBillingResponse.Order(
                value.id(),
                value.societyId(),
                society == null ? null : society.name(),
                society == null ? null : society.email(),
                society == null ? null : society.mobile(),
                society == null ? null : society.code(),
                value.invoiceNumber(),
                value.billingMonth(),
                value.dueDate(),
                value.billingCycle().code(),
                value.periodStart(),
                value.periodEnd(),
                value.planCode(),
                value.amount(),
                value.currency(),
                value.status().code(),
                value.paymentMethod() == null ? null : paymentMethod(value),
                value.providerReference(),
                value.paymentNote(),
                value.submittedAt(),
                value.paidAt(),
                value.reviewedAt(),
                value.rejectionReason(),
                value.createdAt(),
                value.pricingBreakdown().stream().map(this::pricing).toList(),
                value.overdue(),
                value.paymentAttempts().stream().map(this::attempt).toList(),
                value.gatewayMode());
    }

    public PlatformBillingResponse.PaymentSetting toResponse(
            PlatformBillingModels.PaymentSettingView value) {
        if (value == null) return null;
        return new PlatformBillingResponse.PaymentSetting(
                value.beneficiaryName(),
                value.bankName(),
                value.accountNumber(),
                value.ifscCode(),
                value.upiId(),
                value.paymentInstructions(),
                value.updatedAt());
    }

    public PlatformBillingResponse.Checkout toResponse(PlatformBillingModels.Checkout value) {
        return new PlatformBillingResponse.Checkout(
                value.keyId(),
                value.gatewayMode(),
                value.providerOrderId(),
                value.amountPaise(),
                value.currency(),
                value.name(),
                value.description(),
                new PlatformBillingResponse.Prefill(
                        value.prefill().name(),
                        value.prefill().email(),
                        value.prefill().contact()));
    }

    public PlatformBillingResponse.GatewayStatus toResponse(
            PlatformBillingModels.GatewayStatus value) {
        return new PlatformBillingResponse.GatewayStatus(
                value.provider().code(), value.configured(), value.mode());
    }

    private PlatformBillingResponse.PricingLine pricing(PlatformBillingModels.PricingLine value) {
        return new PlatformBillingResponse.PricingLine(
                value.code(),
                value.name(),
                value.quantity(),
                value.spaces(),
                value.rate(),
                value.pricePerSpace(),
                value.monthlyAmount(),
                value.cycleAmount());
    }

    private PlatformBillingResponse.PaymentAttempt attempt(
            PlatformBillingModels.PaymentAttempt value) {
        return new PlatformBillingResponse.PaymentAttempt(
                value.providerOrderId(),
                value.providerPaymentId(),
                value.amount(),
                value.currency(),
                value.requestedMethod() == null ? null : value.requestedMethod().code(),
                value.status().code(),
                value.failureCode(),
                value.failureDescription(),
                value.createdAt(),
                value.updatedAt());
    }

    private String paymentMethod(PlatformBillingModels.OrderView value) {
        return value.gatewayMode() == null
                ? value.paymentMethod().code()
                : value.paymentMethod().razorpayCode();
    }
}
