package com.societyapp.controller;

import com.societyapp.application.model.platformbilling.PlatformBillingModels;
import com.societyapp.application.model.subscription.SubscriptionQuantityModels;
import com.societyapp.application.port.in.PlatformMembershipBillingUseCase;
import com.societyapp.application.port.in.SubscriptionQuantityUseCase;
import com.societyapp.controller.mapper.PlatformBillingHttpMapper;
import com.societyapp.controller.mapper.SubscriptionQuantityHttpMapper;
import com.societyapp.controller.model.PlatformBillingResponse;
import com.societyapp.controller.model.SubscriptionQuantityResponse;
import com.societyapp.domain.type.BillingCycle;
import com.societyapp.domain.type.PaymentMethod;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/society/subscription")
@PreAuthorize("hasRole('SOCIETY_ADMIN')")
public class SubscriptionController {
    private final SubscriptionQuantityUseCase service;
    private final PlatformMembershipBillingUseCase billing;
    private final SubscriptionQuantityHttpMapper quantityMapper =
            new SubscriptionQuantityHttpMapper();
    private final PlatformBillingHttpMapper billingMapper = new PlatformBillingHttpMapper();

    public SubscriptionController(
            SubscriptionQuantityUseCase service, PlatformMembershipBillingUseCase billing) {
        this.service = service;
        this.billing = billing;
    }

    public record QuantityRequest(
            @Min(1) int totalSpaces,
            @Min(0) int totalStaff,
            @NotBlank @Pattern(regexp = "MONTHLY|QUARTERLY|YEARLY") String billingCycle,
            List<String> enabledModules) {}

    public record RazorpayCheckout(
            @NotBlank @Pattern(regexp = "UPI|CARD|NET_BANKING") String paymentMethod) {}

    public record RazorpayVerification(
            @NotBlank String razorpayOrderId,
            @NotBlank String razorpayPaymentId,
            @NotBlank String razorpaySignature) {}

    @GetMapping
    SubscriptionQuantityResponse get() {
        return quantityMapper.toResponse(service.get(CurrentUser.societyId()));
    }

    @PutMapping("/quantities")
    SubscriptionQuantityResponse update(@Valid @RequestBody QuantityRequest request) {
        var command =
                new SubscriptionQuantityModels.UpdateCommand(
                        request.totalSpaces(),
                        request.totalStaff(),
                        BillingCycle.from(request.billingCycle()),
                        request.enabledModules());
        return quantityMapper.toResponse(service.update(CurrentUser.societyId(), command));
    }

    @GetMapping("/orders")
    List<PlatformBillingResponse.Order> orders() {
        return billing.societyOrders(CurrentUser.societyId()).stream()
                .map(billingMapper::toResponse)
                .toList();
    }

    @PostMapping("/orders/{id}/checkout")
    PlatformBillingResponse.Checkout checkout(
            @PathVariable UUID id, @Valid @RequestBody RazorpayCheckout request) {
        return billingMapper.toResponse(
                billing.checkout(
                        CurrentUser.societyId(), id, PaymentMethod.from(request.paymentMethod())));
    }

    @PostMapping("/orders/{id}/verify")
    PlatformBillingResponse.Order verify(
            @PathVariable UUID id, @Valid @RequestBody RazorpayVerification request) {
        return billingMapper.toResponse(
                billing.verifyCheckout(
                        CurrentUser.societyId(),
                        id,
                        new PlatformBillingModels.VerifyCommand(
                                request.razorpayOrderId(),
                                request.razorpayPaymentId(),
                                request.razorpaySignature())));
    }

    @GetMapping(value = "/orders/{id}/invoice", produces = MediaType.APPLICATION_PDF_VALUE)
    ResponseEntity<byte[]> invoice(@PathVariable UUID id) {
        var bytes = billing.societyInvoice(CurrentUser.societyId(), id);
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=platform-membership-invoice.pdf")
                .contentLength(bytes.length)
                .body(bytes);
    }
}
