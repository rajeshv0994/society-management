package com.societyapp.application.model.billing;

import com.societyapp.domain.type.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class BillingModels {
    private BillingModels() {}

    public record Actor(UUID userId, UserRole role) {}

    public record InvoiceCommand(
            UUID spaceId,
            String invoiceNumber,
            LocalDate billingMonth,
            LocalDate dueDate,
            BigDecimal membershipFee,
            BigDecimal otherCharges,
            String otherChargeNote,
            BigDecimal legacySubtotal) {}

    public record PaymentCommand(
            BigDecimal amount,
            PaymentMethod paymentMethod,
            String providerReference,
            String note) {}

    public record CheckoutCommand(BigDecimal amount, PaymentMethod paymentMethod) {}

    public record PaymentSettingsCommand(
            String beneficiaryName,
            String bankName,
            String accountNumber,
            String ifscCode,
            String upiId,
            String razorpayKeyId,
            boolean testMode) {}

    public record Space(
            UUID id,
            String spaceNumber,
            UUID residentId,
            String residentName,
            String residentMobile,
            String block,
            String floorNumber) {}

    public record InvoiceView(
            UUID id,
            UUID societyId,
            Space space,
            String invoiceNumber,
            LocalDate billingMonth,
            LocalDate dueDate,
            BigDecimal subtotal,
            BigDecimal membershipFee,
            BigDecimal otherCharges,
            String otherChargeNote,
            BigDecimal lateFee,
            BigDecimal total,
            BigDecimal paidAmount,
            BigDecimal outstanding,
            PaymentStatus status,
            boolean overdue,
            OffsetDateTime createdAt,
            int paymentCount) {}

    public record PaymentView(
            UUID id,
            UUID invoiceId,
            String invoiceNumber,
            Space space,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            PaymentChannel paymentChannel,
            String providerReference,
            String receiptNumber,
            String note,
            PaymentStatus status,
            OffsetDateTime paidAt,
            UUID paidBy,
            UUID recordedBy,
            String payerName,
            String recordedByName) {}

    public record PaymentSettingView(
            String beneficiaryName,
            String bankName,
            String accountNumberMasked,
            String upiId,
            String razorpayKeyId,
            boolean testMode,
            boolean configured,
            OffsetDateTime updatedAt,
            String accountNumber,
            String ifscCode) {}

    public record Options(
            UUID currentUserId,
            UserRole role,
            Set<PaymentMethod> paymentMethods,
            PaymentSettingView onlinePayment,
            List<Space> spaces) {}

    public record InvoiceCreated(UUID id, String invoiceNumber, PaymentStatus status) {}

    public record PaymentRecorded(
            UUID paymentId,
            String receiptNumber,
            PaymentStatus invoiceStatus,
            BigDecimal paidAmount) {}

    public record Receipt(
            PaymentView payment,
            String societyName,
            String societyRegistration,
            LocalDate billingMonth,
            LocalDate dueDate,
            BigDecimal membershipFee,
            BigDecimal otherCharges,
            String otherChargeNote,
            BigDecimal invoiceTotal,
            BigDecimal invoicePaidAmount,
            PaymentStatus invoiceStatus) {}
}
