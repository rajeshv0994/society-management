package com.societyapp.controller;

import com.societyapp.application.model.platformbilling.PlatformBillingModels;
import com.societyapp.application.port.in.PlatformMembershipBillingUseCase;
import com.societyapp.controller.mapper.PlatformBillingHttpMapper;
import com.societyapp.controller.model.PlatformBillingResponse;
import com.societyapp.domain.type.PaymentStatus;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/platform")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class PlatformMembershipBillingController {
    private final PlatformMembershipBillingUseCase service;
    private final PlatformBillingHttpMapper mapper = new PlatformBillingHttpMapper();

    public PlatformMembershipBillingController(PlatformMembershipBillingUseCase service) {
        this.service = service;
    }

    public record SubscriptionInvoice(
            @NotNull UUID societyId, @NotNull LocalDate billingMonth, @NotNull LocalDate dueDate) {}

    public record PaymentReview(
            @NotBlank @Pattern(regexp = "PAID|REJECTED") String status, String rejectionReason) {}

    public record Cancellation(@NotBlank @Size(max = 300) String reason) {}

    public record PlatformPaymentSettings(
            @NotBlank String beneficiaryName,
            String bankName,
            String accountNumber,
            String ifscCode,
            String upiId,
            String instructions) {}

    @GetMapping("/orders")
    List<PlatformBillingResponse.Order> orders() {
        return service.allOrders().stream().map(mapper::toResponse).toList();
    }

    @PostMapping("/orders")
    PlatformBillingResponse.Order issueInvoice(@Valid @RequestBody SubscriptionInvoice request) {
        return mapper.toResponse(
                service.issue(request.societyId(), request.billingMonth(), request.dueDate()));
    }

    @GetMapping("/payment-gateway")
    PlatformBillingResponse.GatewayStatus paymentGateway() {
        return mapper.toResponse(service.gatewayStatus());
    }

    @GetMapping("/payment-settings")
    PlatformBillingResponse.PaymentSetting paymentSettings() {
        return mapper.toResponse(service.paymentSettings());
    }

    @PutMapping("/payment-settings")
    PlatformBillingResponse.PaymentSetting paymentSettings(
            @Valid @RequestBody PlatformPaymentSettings request) {
        return mapper.toResponse(
                service.updatePaymentSettings(
                        CurrentUser.id(),
                        new PlatformBillingModels.PaymentSettingsCommand(
                                request.beneficiaryName(),
                                request.bankName(),
                                request.accountNumber(),
                                request.ifscCode(),
                                request.upiId(),
                                request.instructions())));
    }

    @PostMapping("/orders/{id}/review")
    PlatformBillingResponse.Order reviewPayment(
            @PathVariable UUID id, @Valid @RequestBody PaymentReview request) {
        return mapper.toResponse(
                service.review(
                        CurrentUser.id(),
                        id,
                        new PlatformBillingModels.ReviewCommand(
                                PaymentStatus.from(request.status()), request.rejectionReason())));
    }

    @PostMapping("/orders/{id}/reconcile")
    PlatformBillingResponse.Order reconcilePayment(@PathVariable UUID id) {
        return mapper.toResponse(service.reconcile(id));
    }

    @PostMapping("/orders/{id}/cancel")
    PlatformBillingResponse.Order cancelInvoice(
            @PathVariable UUID id, @Valid @RequestBody Cancellation request) {
        return mapper.toResponse(service.cancel(CurrentUser.id(), id, request.reason()));
    }

    @GetMapping(value = "/orders/{id}/invoice", produces = MediaType.APPLICATION_PDF_VALUE)
    ResponseEntity<byte[]> invoice(@PathVariable UUID id) {
        var bytes = service.adminInvoice(id);
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=platform-membership-invoice.pdf")
                .contentLength(bytes.length)
                .body(bytes);
    }
}
