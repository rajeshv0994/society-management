package com.societyapp.application.query;

import com.societyapp.application.exception.AuthorizationException;
import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.model.billing.BillingModels;
import com.societyapp.application.port.in.StructureAccessUseCase;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public final class BillingQueryCoordinator {
    private static final Set<PaymentMethod> OFFICE_METHODS = Set.of(PaymentMethod.CASH);
    private static final Set<PaymentMethod> ONLINE_METHODS =
            Set.of(PaymentMethod.UPI, PaymentMethod.CARD, PaymentMethod.NET_BANKING);

    private final MaintenanceInvoiceRepository invoices;
    private final PaymentRepository payments;
    private final FlatRepository flats;
    private final FloorRepository floors;
    private final BlockRepository blocks;
    private final StructureAccessUseCase access;
    private final StaffPermissionRepository permissions;
    private final UserAccountJpaRepository users;
    private final SocietyRepository societies;
    private final SocietyPaymentSettingRepository paymentSettings;

    public BillingQueryCoordinator(
            MaintenanceInvoiceRepository invoices,
            PaymentRepository payments,
            FlatRepository flats,
            FloorRepository floors,
            BlockRepository blocks,
            StructureAccessUseCase access,
            StaffPermissionRepository permissions,
            UserAccountJpaRepository users,
            SocietyRepository societies,
            SocietyPaymentSettingRepository paymentSettings) {
        this.invoices = invoices;
        this.payments = payments;
        this.flats = flats;
        this.floors = floors;
        this.blocks = blocks;
        this.access = access;
        this.permissions = permissions;
        this.users = users;
        this.societies = societies;
        this.paymentSettings = paymentSettings;
    }

    public List<BillingModels.InvoiceView> list(UUID societyId, BillingModels.Actor actor) {
        requireRoleAccess(actor);
        return invoices.findBySocietyIdOrderByBillingMonthDesc(societyId).stream()
                .filter(invoice -> canViewInvoice(invoice, actor))
                .map(this::invoiceView)
                .toList();
    }

    public List<BillingModels.PaymentView> payments(UUID societyId, BillingModels.Actor actor) {
        requireRoleAccess(actor);
        return payments.findBySocietyIdOrderByPaidAtDesc(societyId).stream()
                .filter(
                        payment ->
                                invoices.findById(payment.getInvoiceId())
                                        .filter(invoice -> canViewInvoice(invoice, actor))
                                        .isPresent())
                .map(this::paymentView)
                .toList();
    }

    public List<BillingModels.PaymentView> invoicePayments(
            UUID societyId, BillingModels.Actor actor, UUID invoiceId) {
        requireInvoice(societyId, actor, invoiceId);
        return payments.findByInvoiceIdOrderByPaidAtDesc(invoiceId).stream()
                .map(this::paymentView)
                .toList();
    }

    public BillingModels.Options options(UUID societyId, BillingModels.Actor actor) {
        requireRoleAccess(actor);
        var methods = actor.role() == UserRole.RESIDENT ? ONLINE_METHODS : OFFICE_METHODS;
        var setting =
                paymentSettings
                        .findBySocietyId(societyId)
                        .map(
                                value ->
                                        paymentSettingView(
                                                value, actor.role() == UserRole.SOCIETY_ADMIN))
                        .orElse(null);
        var spaces =
                flats.findAll().stream()
                        .filter(flat -> access.belongsToSociety(flat.getId(), societyId))
                        .filter(
                                flat ->
                                        actor.role() != UserRole.RESIDENT
                                                || access.occupiedBy(flat.getId(), actor.userId()))
                        .map(this::spaceView)
                        .toList();
        return new BillingModels.Options(actor.userId(), actor.role(), methods, setting, spaces);
    }

    public BillingModels.Receipt receipt(
            UUID societyId, BillingModels.Actor actor, UUID paymentId) {
        requireRoleAccess(actor);
        var payment =
                payments.findByIdAndSocietyId(paymentId, societyId)
                        .orElseThrow(() -> new ResourceNotFoundException("Receipt not found"));
        var invoice = requireInvoice(societyId, actor, payment.getInvoiceId());
        var society = societies.findById(societyId).orElse(null);
        return new BillingModels.Receipt(
                paymentView(payment),
                society == null ? "Society" : society.getName(),
                society == null ? null : society.getRegistrationNumber(),
                invoice.getBillingMonth(),
                invoice.getDueDate(),
                invoice.getMembershipFee(),
                invoice.getOtherCharges(),
                invoice.getOtherChargeNote(),
                invoice.getSubtotal().add(invoice.getLateFee()),
                invoice.getPaidAmount(),
                PaymentStatus.from(invoice.getStatus()));
    }

    public MaintenanceInvoice requireInvoice(
            UUID societyId, BillingModels.Actor actor, UUID invoiceId) {
        requireRoleAccess(actor);
        return invoices.findByIdAndSocietyId(invoiceId, societyId)
                .filter(invoice -> canViewInvoice(invoice, actor))
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
    }

    public boolean canViewInvoice(MaintenanceInvoice invoice, BillingModels.Actor actor) {
        return actor.role() != UserRole.RESIDENT
                || access.occupiedBy(invoice.getFlatId(), actor.userId());
    }

    public void requireRoleAccess(BillingModels.Actor actor) {
        if (actor.role() == UserRole.STAFF
                && !permissions.existsByStaffUserIdAndPermissionCode(
                        actor.userId(), StaffPermissionCode.PAYMENTS.code())) {
            throw new AuthorizationException("Payments permission is required");
        }
    }

    public Optional<UUID> residentForFlat(UUID flatId) {
        return flats.findById(flatId)
                .map(
                        flat ->
                                flat.getActiveTenantId() != null
                                        ? flat.getActiveTenantId()
                                        : flat.getActiveOwnerId());
    }

    public BillingModels.PaymentSettingView paymentSettingView(
            SocietyPaymentSetting setting, boolean includePrivate) {
        return new BillingModels.PaymentSettingView(
                setting.getBeneficiaryName(),
                setting.getBankName(),
                maskAccount(setting.getAccountNumber()),
                setting.getUpiId(),
                setting.getRazorpayKeyId(),
                setting.isTestMode(),
                setting.getRazorpayKeyId() != null && !setting.getRazorpayKeyId().isBlank(),
                setting.getUpdatedAt(),
                includePrivate ? setting.getAccountNumber() : null,
                includePrivate ? setting.getIfscCode() : null);
    }

    private BillingModels.InvoiceView invoiceView(MaintenanceInvoice invoice) {
        BigDecimal total = invoice.getSubtotal().add(invoice.getLateFee());
        return new BillingModels.InvoiceView(
                invoice.getId(),
                invoice.getSocietyId(),
                spaceView(invoice.getFlatId()),
                invoice.getInvoiceNumber(),
                invoice.getBillingMonth(),
                invoice.getDueDate(),
                invoice.getSubtotal(),
                invoice.getMembershipFee(),
                invoice.getOtherCharges(),
                invoice.getOtherChargeNote(),
                invoice.getLateFee(),
                total,
                invoice.getPaidAmount(),
                total.subtract(invoice.getPaidAmount()),
                PaymentStatus.from(invoice.getStatus()),
                !PaymentStatus.PAID.matches(invoice.getStatus())
                        && invoice.getDueDate().isBefore(LocalDate.now()),
                invoice.getCreatedAt(),
                payments.findByInvoiceIdOrderByPaidAtDesc(invoice.getId()).size());
    }

    private BillingModels.PaymentView paymentView(Payment payment) {
        var invoice = invoices.findById(payment.getInvoiceId()).orElse(null);
        return new BillingModels.PaymentView(
                payment.getId(),
                payment.getInvoiceId(),
                invoice == null ? null : invoice.getInvoiceNumber(),
                invoice == null ? null : spaceView(invoice.getFlatId()),
                payment.getAmount(),
                paymentMethod(payment.getPaymentMethod()),
                PaymentChannel.from(payment.getPaymentChannel()),
                payment.getProviderReference(),
                payment.getReceiptNumber(),
                payment.getNote(),
                PaymentStatus.from(payment.getStatus()),
                payment.getPaidAt(),
                payment.getPaidBy(),
                payment.getRecordedBy(),
                userName(payment.getPaidBy(), "Resident"),
                userName(payment.getRecordedBy(), "System"));
    }

    private BillingModels.Space spaceView(UUID flatId) {
        return flats.findById(flatId).map(this::spaceView).orElse(null);
    }

    private BillingModels.Space spaceView(Flat flat) {
        UUID residentId =
                flat.getActiveTenantId() != null
                        ? flat.getActiveTenantId()
                        : flat.getActiveOwnerId();
        var resident = residentId == null ? null : users.findById(residentId).orElse(null);
        var floor = floors.findById(flat.getFloorId()).orElse(null);
        var block = floor == null ? null : blocks.findById(floor.getBlockId()).orElse(null);
        return new BillingModels.Space(
                flat.getId(),
                flat.getFlatNumber(),
                residentId,
                resident == null ? null : resident.getFullName(),
                resident == null ? null : resident.getMobile(),
                block == null ? null : block.getName(),
                floor == null ? null : floor.getName());
    }

    private String userName(UUID id, String fallback) {
        return users.findById(id).map(UserAccount::getFullName).orElse(fallback);
    }

    private PaymentMethod paymentMethod(String value) {
        return PaymentMethod.from(value.replaceFirst("^RAZORPAY_", ""));
    }

    private String maskAccount(String account) {
        if (account == null || account.isBlank()) return null;
        String clean = account.trim();
        return clean.length() <= 4 ? clean : "XXXX" + clean.substring(clean.length() - 4);
    }
}
