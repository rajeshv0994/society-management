package com.societyapp.controller;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.societyapp.application.model.billing.BillingModels;
import com.societyapp.application.port.in.BillingUseCase;
import com.societyapp.controller.mapper.BillingHttpMapper;
import com.societyapp.controller.model.BillingResponse;
import com.societyapp.domain.type.*;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
public class BillingController {
    private final BillingUseCase service;
    private final BillingHttpMapper mapper = new BillingHttpMapper();

    public BillingController(BillingUseCase service) {
        this.service = service;
    }

    public record Invoice(
            @JsonAlias({"spaceId", "unitId"}) @NotNull UUID flatId,
            String invoiceNumber,
            @NotNull LocalDate billingMonth,
            @NotNull LocalDate dueDate,
            @DecimalMin("0") BigDecimal membershipFee,
            @DecimalMin("0") BigDecimal otherCharges,
            @Size(max = 240) String otherChargeNote,
            @DecimalMin(value = "0", inclusive = false) BigDecimal subtotal) {}

    public record Payment(
            @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal amount,
            @NotBlank String paymentMethod,
            @Size(max = 120) String providerReference,
            @Size(max = 500) String note) {}

    public record RazorpayCheckout(
            @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal amount,
            @NotBlank @Pattern(regexp = "UPI|CARD|NET_BANKING") String paymentMethod) {}

    public record SocietyPaymentSettings(
            @NotBlank @Size(max = 150) String beneficiaryName,
            @Size(max = 150) String bankName,
            @Size(max = 60) String accountNumber,
            @Size(max = 20) String ifscCode,
            @Size(max = 100) String upiId,
            @NotBlank @Size(max = 100) String razorpayKeyId,
            boolean testMode) {}

    @GetMapping("/invoices")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    List<BillingResponse.Invoice> invoices() {
        return service.list(CurrentUser.societyId(), actor()).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/options")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    BillingResponse.Options options() {
        return mapper.toResponse(service.options(CurrentUser.societyId(), actor()));
    }

    @PostMapping("/invoices")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    BillingResponse.InvoiceCreated invoice(@Valid @RequestBody Invoice request) {
        return mapper.toResponse(
                service.create(
                        CurrentUser.societyId(),
                        new BillingModels.InvoiceCommand(
                                request.flatId(),
                                request.invoiceNumber(),
                                request.billingMonth(),
                                request.dueDate(),
                                request.membershipFee(),
                                request.otherCharges(),
                                request.otherChargeNote(),
                                request.subtotal())));
    }

    @GetMapping("/payments")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    List<BillingResponse.Payment> payments() {
        return service.payments(CurrentUser.societyId(), actor()).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/invoices/{id}/payments")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    List<BillingResponse.Payment> invoicePayments(@PathVariable UUID id) {
        return service.invoicePayments(CurrentUser.societyId(), actor(), id).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @PostMapping("/invoices/{id}/payments")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF')")
    BillingResponse.PaymentRecorded pay(
            @PathVariable UUID id, @Valid @RequestBody Payment request) {
        return mapper.toResponse(
                service.pay(
                        CurrentUser.societyId(),
                        actor(),
                        id,
                        new BillingModels.PaymentCommand(
                                request.amount(), PaymentMethod.from(request.paymentMethod()),
                                request.providerReference(), request.note())));
    }

    @PostMapping("/invoices/{id}/checkout")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    BillingResponse.PaymentRecorded checkout(
            @PathVariable UUID id, @Valid @RequestBody RazorpayCheckout request) {
        return mapper.toResponse(
                service.checkout(
                        CurrentUser.societyId(),
                        actor(),
                        id,
                        new BillingModels.CheckoutCommand(
                                request.amount(), PaymentMethod.from(request.paymentMethod()))));
    }

    @PutMapping("/payment-settings")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    BillingResponse.PaymentSetting paymentSettings(
            @Valid @RequestBody SocietyPaymentSettings request) {
        return mapper.toResponse(
                service.updatePaymentSettings(
                        CurrentUser.societyId(),
                        CurrentUser.id(),
                        new BillingModels.PaymentSettingsCommand(
                                request.beneficiaryName(),
                                request.bankName(),
                                request.accountNumber(),
                                request.ifscCode(),
                                request.upiId(),
                                request.razorpayKeyId(),
                                request.testMode())));
    }

    @GetMapping("/payments/{id}/receipt")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    BillingResponse.Receipt receipt(@PathVariable UUID id) {
        return mapper.toResponse(service.receipt(CurrentUser.societyId(), actor(), id));
    }

    @GetMapping(value = "/payments/{id}/receipt.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    ResponseEntity<byte[]> receiptPdf(@PathVariable UUID id) {
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=payment-receipt-" + id + ".pdf")
                .body(service.receiptPdf(CurrentUser.societyId(), actor(), id));
    }

    private BillingModels.Actor actor() {
        return new BillingModels.Actor(CurrentUser.id(), UserRole.from(CurrentUser.role()));
    }
}
