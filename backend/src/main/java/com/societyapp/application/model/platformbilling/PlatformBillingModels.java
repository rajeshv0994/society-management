package com.societyapp.application.model.platformbilling;

import com.societyapp.domain.type.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class PlatformBillingModels {
    private PlatformBillingModels() {}

    public record PaymentSettingsCommand(
            String beneficiaryName,
            String bankName,
            String accountNumber,
            String ifscCode,
            String upiId,
            String instructions) {}

    public record SubmitCommand(PaymentMethod method, String reference, String note) {}

    public record VerifyCommand(String providerOrderId, String paymentId, String signature) {}

    public record ReviewCommand(PaymentStatus status, String rejectionReason) {}

    public record PricingLine(
            String code,
            String name,
            Integer quantity,
            Integer spaces,
            BigDecimal rate,
            BigDecimal pricePerSpace,
            BigDecimal monthlyAmount,
            BigDecimal cycleAmount) {}

    public record PaymentAttempt(
            String providerOrderId,
            String providerPaymentId,
            BigDecimal amount,
            String currency,
            PaymentMethod requestedMethod,
            PaymentAttemptStatus status,
            String failureCode,
            String failureDescription,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {}

    public record SocietySummary(String name, String email, String mobile, String code) {}

    public record OrderView(
            UUID id,
            UUID societyId,
            SocietySummary society,
            String invoiceNumber,
            LocalDate billingMonth,
            LocalDate dueDate,
            BillingCycle billingCycle,
            LocalDate periodStart,
            LocalDate periodEnd,
            String planCode,
            BigDecimal amount,
            String currency,
            PaymentStatus status,
            PaymentMethod paymentMethod,
            String providerReference,
            String paymentNote,
            OffsetDateTime submittedAt,
            OffsetDateTime paidAt,
            OffsetDateTime reviewedAt,
            String rejectionReason,
            OffsetDateTime createdAt,
            List<PricingLine> pricingBreakdown,
            boolean overdue,
            List<PaymentAttempt> paymentAttempts,
            String gatewayMode) {}

    public record PaymentSettingView(
            String beneficiaryName,
            String bankName,
            String accountNumber,
            String ifscCode,
            String upiId,
            String paymentInstructions,
            OffsetDateTime updatedAt) {}

    public record Checkout(
            String keyId,
            String gatewayMode,
            String providerOrderId,
            long amountPaise,
            String currency,
            String name,
            String description,
            Prefill prefill) {}

    public record Prefill(String name, String email, String contact) {}

    public record GatewayStatus(PaymentProvider provider, boolean configured, String mode) {}
}
