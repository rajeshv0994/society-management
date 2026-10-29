package com.societyapp.controller;

import com.societyapp.application.port.in.PlatformMembershipBillingUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/payments/razorpay")
public class RazorpayWebhookController {
    public record WebhookResponse(boolean processed) {}

    private final PlatformMembershipBillingUseCase billing;

    public RazorpayWebhookController(PlatformMembershipBillingUseCase billing) {
        this.billing = billing;
    }

    @PostMapping("/webhook")
    ResponseEntity<WebhookResponse> webhook(
            @RequestHeader("X-Razorpay-Signature") String signature,
            @RequestHeader("x-razorpay-event-id") String eventId,
            @RequestBody String rawBody) {
        boolean processed = billing.processWebhook(eventId, signature, rawBody);
        return ResponseEntity.ok(new WebhookResponse(processed));
    }
}
