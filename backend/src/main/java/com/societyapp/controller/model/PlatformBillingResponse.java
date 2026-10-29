package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class PlatformBillingResponse {
    private PlatformBillingResponse() {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PricingLine(
            String code,
            String name,
            Integer quantity,
            Integer spaces,
            BigDecimal rate,
            @JsonProperty("price_per_space") BigDecimal pricePerSpace,
            @JsonProperty("monthly_amount") BigDecimal monthlyAmount,
            @JsonProperty("cycle_amount") BigDecimal cycleAmount) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PaymentAttempt(
            @JsonProperty("provider_order_id") String providerOrderId,
            @JsonProperty("provider_payment_id") String providerPaymentId,
            BigDecimal amount,
            String currency,
            @JsonProperty("requested_method") String requestedMethod,
            String status,
            @JsonProperty("failure_code") String failureCode,
            @JsonProperty("failure_description") String failureDescription,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("updated_at") OffsetDateTime updatedAt) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Order(
            UUID id,
            @JsonProperty("society_id") UUID societyId,
            @JsonProperty("society_name") String societyName,
            @JsonProperty("society_email") String societyEmail,
            @JsonProperty("society_mobile") String societyMobile,
            @JsonProperty("society_code") String societyCode,
            @JsonProperty("invoice_number") String invoiceNumber,
            @JsonProperty("billing_month") LocalDate billingMonth,
            @JsonProperty("due_date") LocalDate dueDate,
            @JsonProperty("billing_cycle") String billingCycle,
            @JsonProperty("period_start") LocalDate periodStart,
            @JsonProperty("period_end") LocalDate periodEnd,
            @JsonProperty("plan_code") String planCode,
            BigDecimal amount,
            String currency,
            String status,
            @JsonProperty("payment_method") String paymentMethod,
            @JsonProperty("provider_reference") String providerReference,
            @JsonProperty("payment_note") String paymentNote,
            @JsonProperty("submitted_at") OffsetDateTime submittedAt,
            @JsonProperty("paid_at") OffsetDateTime paidAt,
            @JsonProperty("reviewed_at") OffsetDateTime reviewedAt,
            @JsonProperty("rejection_reason") String rejectionReason,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("pricing_breakdown") List<PricingLine> pricingBreakdown,
            boolean overdue,
            @JsonProperty("payment_attempts") List<PaymentAttempt> paymentAttempts,
            @JsonProperty("gateway_mode") String gatewayMode) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PaymentSetting(
            @JsonProperty("beneficiary_name") String beneficiaryName,
            @JsonProperty("bank_name") String bankName,
            @JsonProperty("account_number") String accountNumber,
            @JsonProperty("ifsc_code") String ifscCode,
            @JsonProperty("upi_id") String upiId,
            @JsonProperty("payment_instructions") String paymentInstructions,
            @JsonProperty("updated_at") OffsetDateTime updatedAt) {}

    public record Prefill(String name, String email, String contact) {}

    public record Checkout(
            @JsonProperty("key_id") String keyId,
            @JsonProperty("gateway_mode") String gatewayMode,
            @JsonProperty("provider_order_id") String providerOrderId,
            @JsonProperty("amount_paise") long amountPaise,
            String currency,
            String name,
            String description,
            Prefill prefill) {}

    public record GatewayStatus(String provider, boolean configured, String mode) {}
}
