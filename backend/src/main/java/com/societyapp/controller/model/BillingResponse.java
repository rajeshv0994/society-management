package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class BillingResponse {
    private BillingResponse() {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Space(
            @JsonProperty("space_id") UUID spaceId,
            @JsonProperty("space_number") String spaceNumber,
            @JsonProperty("resident_id") UUID residentId,
            @JsonProperty("resident_name") String residentName,
            @JsonProperty("resident_mobile") String residentMobile,
            String block,
            @JsonProperty("floor_number") String floorNumber) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Invoice(
            UUID id,
            @JsonProperty("society_id") UUID societyId,
            @JsonProperty("space_id") UUID spaceId,
            @JsonProperty("unit_id") UUID unitId,
            @JsonProperty("flat_id") UUID flatId,
            @JsonProperty("space_number") String spaceNumber,
            @JsonProperty("resident_id") UUID residentId,
            @JsonProperty("resident_name") String residentName,
            @JsonProperty("resident_mobile") String residentMobile,
            String block,
            @JsonProperty("floor_number") String floorNumber,
            @JsonProperty("invoice_number") String invoiceNumber,
            @JsonProperty("billing_month") LocalDate billingMonth,
            @JsonProperty("due_date") LocalDate dueDate,
            BigDecimal subtotal,
            @JsonProperty("membership_fee") BigDecimal membershipFee,
            @JsonProperty("other_charges") BigDecimal otherCharges,
            @JsonProperty("other_charge_note") String otherChargeNote,
            @JsonProperty("late_fee") BigDecimal lateFee,
            BigDecimal total,
            @JsonProperty("paid_amount") BigDecimal paidAmount,
            BigDecimal outstanding,
            String status,
            boolean overdue,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("payment_count") int paymentCount) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Payment(
            UUID id,
            @JsonProperty("invoice_id") UUID invoiceId,
            @JsonProperty("invoice_number") String invoiceNumber,
            @JsonProperty("space_id") UUID spaceId,
            @JsonProperty("space_number") String spaceNumber,
            @JsonProperty("resident_id") UUID residentId,
            @JsonProperty("resident_name") String residentName,
            @JsonProperty("resident_mobile") String residentMobile,
            String block,
            @JsonProperty("floor_number") String floorNumber,
            BigDecimal amount,
            @JsonProperty("payment_method") String paymentMethod,
            @JsonProperty("payment_channel") String paymentChannel,
            @JsonProperty("provider_reference") String providerReference,
            @JsonProperty("receipt_number") String receiptNumber,
            String note,
            String status,
            @JsonProperty("paid_at") OffsetDateTime paidAt,
            @JsonProperty("paid_by") UUID paidBy,
            @JsonProperty("recorded_by") UUID recordedBy,
            @JsonProperty("payer_name") String payerName,
            @JsonProperty("recorded_by_name") String recordedByName) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PaymentSetting(
            @JsonProperty("beneficiary_name") String beneficiaryName,
            @JsonProperty("bank_name") String bankName,
            @JsonProperty("account_number_masked") String accountNumberMasked,
            @JsonProperty("upi_id") String upiId,
            @JsonProperty("razorpay_key_id") String razorpayKeyId,
            @JsonProperty("test_mode") boolean testMode,
            boolean configured,
            @JsonProperty("updated_at") OffsetDateTime updatedAt,
            @JsonProperty("account_number") String accountNumber,
            @JsonProperty("ifsc_code") String ifscCode) {}

    public record Options(
            @JsonProperty("current_user_id") UUID currentUserId,
            String role,
            @JsonProperty("payment_methods") Set<String> paymentMethods,
            @JsonProperty("online_payment") PaymentSetting onlinePayment,
            List<Space> spaces) {}

    public record InvoiceCreated(UUID id, String invoiceNumber, String status) {}

    public record PaymentRecorded(
            UUID paymentId, String receiptNumber, String invoiceStatus, BigDecimal paidAmount) {}

    public record Receipt(
            @JsonUnwrapped Payment payment,
            @JsonProperty("society_name") String societyName,
            @JsonProperty("society_registration") String societyRegistration,
            @JsonProperty("billing_month") LocalDate billingMonth,
            @JsonProperty("due_date") LocalDate dueDate,
            @JsonProperty("membership_fee") BigDecimal membershipFee,
            @JsonProperty("other_charges") BigDecimal otherCharges,
            @JsonProperty("other_charge_note") String otherChargeNote,
            @JsonProperty("invoice_total") BigDecimal invoiceTotal,
            @JsonProperty("invoice_paid_amount") BigDecimal invoicePaidAmount,
            @JsonProperty("invoice_status") String invoiceStatus) {}
}
