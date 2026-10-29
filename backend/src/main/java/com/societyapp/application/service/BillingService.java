package com.societyapp.application.service;

import com.societyapp.application.exception.AuthorizationException;
import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.model.billing.BillingModels;
import com.societyapp.application.port.in.BillingUseCase;
import com.societyapp.application.port.in.StructureAccessUseCase;
import com.societyapp.application.port.out.ReceiptRenderer;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.query.BillingQueryCoordinator;
import com.societyapp.application.usecase.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.PaymentChannel;
import com.societyapp.domain.type.PaymentMethod;
import com.societyapp.domain.type.PaymentStatus;
import com.societyapp.domain.type.UserRole;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BillingService implements BillingUseCase {
    private static final Set<PaymentMethod> OFFICE_METHODS = Set.of(PaymentMethod.CASH);
    private static final Set<PaymentMethod> ONLINE_METHODS =
            Set.of(PaymentMethod.UPI, PaymentMethod.CARD, PaymentMethod.NET_BANKING);

    private final MaintenanceInvoiceRepository invoices;
    private final PaymentRepository payments;
    private final FlatRepository flats;
    private final StructureAccessUseCase access;
    private final SocietyPaymentSettingRepository paymentSettings;
    private final ReceiptRenderer receiptRenderer;
    private final BillingQueryCoordinator queries;

    public BillingService(
            MaintenanceInvoiceRepository invoices,
            PaymentRepository payments,
            FlatRepository flats,
            FloorRepository floors,
            BlockRepository blocks,
            StructureAccessUseCase access,
            StaffPermissionRepository permissions,
            UserAccountJpaRepository users,
            SocietyRepository societies,
            SocietyPaymentSettingRepository paymentSettings,
            ReceiptRenderer receiptRenderer) {
        this.invoices = invoices;
        this.payments = payments;
        this.flats = flats;
        this.access = access;
        this.paymentSettings = paymentSettings;
        this.receiptRenderer = receiptRenderer;
        this.queries =
                new BillingQueryCoordinator(
                        invoices,
                        payments,
                        flats,
                        floors,
                        blocks,
                        access,
                        permissions,
                        users,
                        societies,
                        paymentSettings);
    }

    @Override
    public List<BillingModels.InvoiceView> list(UUID societyId, BillingModels.Actor actor) {
        return queries.list(societyId, actor);
    }

    @Override
    public List<BillingModels.PaymentView> payments(UUID societyId, BillingModels.Actor actor) {
        return queries.payments(societyId, actor);
    }

    @Override
    public List<BillingModels.PaymentView> invoicePayments(
            UUID societyId, BillingModels.Actor actor, UUID invoiceId) {
        return queries.invoicePayments(societyId, actor, invoiceId);
    }

    @Override
    public BillingModels.Options options(UUID societyId, BillingModels.Actor actor) {
        return queries.options(societyId, actor);
    }

    @Override
    public BillingModels.InvoiceCreated create(
            UUID societyId, BillingModels.InvoiceCommand command) {
        var flatId = command.spaceId();
        var billingMonth = command.billingMonth();
        var dueDate = command.dueDate();
        access.requireFlat(flatId, societyId);
        if (dueDate.isBefore(billingMonth.withDayOfMonth(1))) {
            throw new IllegalArgumentException("Due date cannot be before the billing month");
        }
        BigDecimal membership =
                command.membershipFee() == null ? BigDecimal.ZERO : command.membershipFee();
        BigDecimal other =
                command.otherCharges() == null ? BigDecimal.ZERO : command.otherCharges();
        if (membership.signum() == 0 && other.signum() == 0 && command.legacySubtotal() != null) {
            membership = command.legacySubtotal();
        }
        BigDecimal subtotal = membership.add(other);
        if (subtotal.signum() <= 0) {
            throw new IllegalArgumentException("Add a membership fee or another society charge");
        }
        if (other.signum() > 0
                && (command.otherChargeNote() == null || command.otherChargeNote().isBlank())) {
            throw new IllegalArgumentException("Describe the other society charge");
        }
        var invoice = new MaintenanceInvoice();
        invoice.setSocietyId(societyId);
        invoice.setFlatId(flatId);
        invoice.setInvoiceNumber(
                command.invoiceNumber() == null || command.invoiceNumber().isBlank()
                        ? automaticInvoiceNumber(flatId, billingMonth)
                        : command.invoiceNumber().trim().toUpperCase());
        invoice.setBillingMonth(billingMonth.withDayOfMonth(1));
        invoice.setDueDate(dueDate);
        invoice.setSubtotal(subtotal);
        invoice.setMembershipFee(membership);
        invoice.setOtherCharges(other);
        invoice.setOtherChargeNote(
                command.otherChargeNote() == null || command.otherChargeNote().isBlank()
                        ? null
                        : command.otherChargeNote().trim());
        invoice.setLateFee(BigDecimal.ZERO);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setStatus(PaymentStatus.DUE.code());
        invoice.setCreatedAt(OffsetDateTime.now());
        invoice = invoices.save(invoice);
        return new BillingModels.InvoiceCreated(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                PaymentStatus.from(invoice.getStatus()));
    }

    @Override
    public BillingModels.PaymentRecorded pay(
            UUID societyId,
            BillingModels.Actor actor,
            UUID invoiceId,
            BillingModels.PaymentCommand command) {
        queries.requireRoleAccess(actor);
        if (actor.role() == UserRole.RESIDENT) {
            throw new AuthorizationException("Members must use Razorpay online checkout");
        }
        var requestedMethod = command.paymentMethod();
        if (!OFFICE_METHODS.contains(requestedMethod)) {
            throw new IllegalArgumentException("Invalid office payment method");
        }
        return recordPayment(
                societyId,
                actor,
                invoiceId,
                command.amount(),
                requestedMethod.code(),
                officeChannel(requestedMethod),
                normalizeReference(requestedMethod, command.providerReference()),
                command.note());
    }

    @Override
    public BillingModels.PaymentRecorded checkout(
            UUID societyId,
            BillingModels.Actor actor,
            UUID invoiceId,
            BillingModels.CheckoutCommand command) {
        queries.requireRoleAccess(actor);
        var requestedMethod = command.paymentMethod();
        if (!ONLINE_METHODS.contains(requestedMethod)) {
            throw new IllegalArgumentException("Invalid Razorpay payment method");
        }
        var setting =
                paymentSettings
                        .findBySocietyId(societyId)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Society online payment account is not configured"));
        if (setting.getRazorpayKeyId() == null || setting.getRazorpayKeyId().isBlank()) {
            throw new IllegalStateException("Society Razorpay account is not configured");
        }
        String reference =
                (setting.isTestMode() ? "pay_soc_test_" : "pay_soc_")
                        + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        return recordPayment(
                societyId,
                actor,
                invoiceId,
                command.amount(),
                requestedMethod.razorpayCode(),
                PaymentChannel.ONLINE.code(),
                reference,
                setting.isTestMode() ? "Razorpay test-mode society collection" : null);
    }

    @Override
    public BillingModels.PaymentSettingView updatePaymentSettings(
            UUID societyId, UUID actorId, BillingModels.PaymentSettingsCommand command) {
        if (!command.testMode()) {
            throw new IllegalArgumentException(
                    "Live Razorpay is not enabled yet. Keep the society account in test mode.");
        }
        var setting =
                paymentSettings
                        .findBySocietyId(societyId)
                        .orElseGet(
                                () -> {
                                    var created = new SocietyPaymentSetting();
                                    created.setSocietyId(societyId);
                                    return created;
                                });
        setting.setBeneficiaryName(command.beneficiaryName().trim());
        setting.setBankName(blankToNull(command.bankName()));
        setting.setAccountNumber(blankToNull(command.accountNumber()));
        setting.setIfscCode(
                command.ifscCode() == null || command.ifscCode().isBlank()
                        ? null
                        : command.ifscCode().trim().toUpperCase());
        setting.setUpiId(blankToNull(command.upiId()));
        setting.setRazorpayKeyId(command.razorpayKeyId().trim());
        setting.setTestMode(true);
        setting.setUpdatedBy(actorId);
        setting.setUpdatedAt(OffsetDateTime.now());
        return queries.paymentSettingView(paymentSettings.save(setting), true);
    }

    private BillingModels.PaymentRecorded recordPayment(
            UUID societyId,
            BillingModels.Actor actor,
            UUID invoiceId,
            BigDecimal amount,
            String method,
            String channel,
            String reference,
            String note) {
        var invoice =
                invoices.findForUpdateByIdAndSocietyId(invoiceId, societyId)
                        .filter(item -> queries.canViewInvoice(item, actor))
                        .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        var total = invoice.getSubtotal().add(invoice.getLateFee());
        var outstanding = total.subtract(invoice.getPaidAmount());
        if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Invoice is already paid");
        }
        if (amount.compareTo(outstanding) > 0) {
            throw new IllegalArgumentException("Payment exceeds outstanding amount");
        }
        var payment = new Payment();
        payment.setSocietyId(societyId);
        payment.setInvoiceId(invoiceId);
        payment.setPaidBy(
                actor.role() == UserRole.RESIDENT
                        ? actor.userId()
                        : queries.residentForFlat(invoice.getFlatId()).orElse(actor.userId()));
        payment.setRecordedBy(actor.userId());
        payment.setAmount(amount);
        payment.setPaymentMethod(method);
        payment.setPaymentChannel(channel);
        payment.setProviderReference(reference);
        payment.setReceiptNumber(receiptNumber());
        payment.setNote(note == null || note.isBlank() ? null : note.trim());
        payment.setStatus(PaymentStatus.SUCCESS.code());
        payment.setPaidAt(OffsetDateTime.now());
        payment = payments.save(payment);

        var paid = invoice.getPaidAmount().add(amount);
        String status =
                paid.compareTo(total) == 0
                        ? PaymentStatus.PAID.code()
                        : PaymentStatus.PART_PAID.code();
        invoice.setPaidAmount(paid);
        invoice.setStatus(status);
        invoices.save(invoice);
        return new BillingModels.PaymentRecorded(
                payment.getId(), payment.getReceiptNumber(), PaymentStatus.from(status), paid);
    }

    @Override
    public BillingModels.Receipt receipt(
            UUID societyId, BillingModels.Actor actor, UUID paymentId) {
        return queries.receipt(societyId, actor, paymentId);
    }

    @Override
    public byte[] receiptPdf(UUID societyId, BillingModels.Actor actor, UUID paymentId) {
        return receiptRenderer.render(receipt(societyId, actor, paymentId));
    }

    private String automaticInvoiceNumber(UUID flatId, LocalDate billingMonth) {
        String space =
                flats.findById(flatId)
                        .map(Flat::getFlatNumber)
                        .orElse("SPACE")
                        .replaceAll("[^A-Za-z0-9]", "")
                        .toUpperCase();
        return "INV-"
                + billingMonth.format(DateTimeFormatter.ofPattern("yyyyMM"))
                + "-"
                + space
                + "-"
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private String receiptNumber() {
        return "RCT-"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"))
                + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private String officeChannel(PaymentMethod method) {
        return Set.of(PaymentMethod.UPI, PaymentMethod.CARD).contains(method)
                ? PaymentChannel.ONLINE.code()
                : PaymentChannel.OFFLINE.code();
    }

    private String normalizeReference(PaymentMethod method, String reference) {
        if (reference != null && !reference.isBlank()) return reference.trim().toUpperCase();
        if (Set.of(PaymentMethod.CHEQUE, PaymentMethod.BANK_TRANSFER).contains(method)) {
            throw new IllegalArgumentException("Transaction or cheque reference is required");
        }
        return method
                + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
