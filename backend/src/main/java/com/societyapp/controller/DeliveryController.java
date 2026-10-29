package com.societyapp.controller;

import com.societyapp.application.model.delivery.DeliveryModels;
import com.societyapp.application.port.in.DeliveryUseCase;
import com.societyapp.controller.mapper.DeliveryHttpMapper;
import com.societyapp.controller.model.DeliveryResponse;
import com.societyapp.domain.type.*;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deliveries")
@PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
public class DeliveryController {
    private final DeliveryUseCase service;
    private final DeliveryHttpMapper mapper = new DeliveryHttpMapper();

    public DeliveryController(DeliveryUseCase service) {
        this.service = service;
    }

    public record DeliveryRequest(
            UUID residentId,
            @NotNull UUID flatId,
            @Size(max = 100) String trackingNumber,
            @NotBlank @Size(max = 80) String provider,
            @Size(max = 150) String senderName,
            @Size(max = 150) String deliveryPersonName,
            @Pattern(regexp = "^$|[0-9+() -]{7,20}$") String deliveryPersonMobile,
            @NotBlank @Pattern(regexp = "DOCUMENT|PACKAGE|FOOD|GROCERY|MEDICINE|OTHER")
                    String parcelType,
            @Min(1) @Max(100) int packageCount,
            @NotBlank @Pattern(regexp = "SMALL|MEDIUM|LARGE|OVERSIZED") String parcelSize,
            boolean fragile,
            @Size(max = 80) String storageLocation,
            OffsetDateTime expectedAt,
            @Size(max = 500) String notes,
            @NotNull @DecimalMin("0") BigDecimal codAmount,
            boolean receivedNow) {}

    public record ActionRequest(
            @NotBlank
                    @Pattern(
                            regexp =
                                    "ACCEPT|REJECT|AUTHORIZE|CANCEL|RECEIVE|MARK_READY|OUT_FOR_DELIVERY|HANDOVER|DELIVER|RETURN|DAMAGED|LOST")
                    String action,
            @Size(max = 500) String note,
            @Size(max = 150) String collectorName,
            @Pattern(regexp = "[0-9]{6}") String pickupCode) {}

    public record CheckoutRequest(
            @NotBlank @Pattern(regexp = "UPI|CARD|NET_BANKING") String paymentMethod) {}

    public record CodRequest(@NotBlank @Pattern(regexp = "CASH|UPI|CARD") String paymentMethod) {}

    @GetMapping
    DeliveryResponse.Workspace workspace() {
        return mapper.toResponse(service.workspace(actor()));
    }

    @GetMapping("/{id}/history")
    List<DeliveryResponse.HistoryEntry> history(@PathVariable UUID id) {
        return service.history(actor(), id).stream().map(mapper::toResponse).toList();
    }

    @PostMapping
    DeliveryResponse.View create(@Valid @RequestBody DeliveryRequest request) {
        return mapper.toResponse(
                service.create(
                        actor(),
                        new DeliveryModels.CreateCommand(
                                request.residentId(),
                                request.flatId(),
                                request.trackingNumber(),
                                request.provider(),
                                request.senderName(),
                                request.deliveryPersonName(),
                                request.deliveryPersonMobile(),
                                ParcelType.from(request.parcelType()),
                                request.packageCount(),
                                ParcelSize.from(request.parcelSize()),
                                request.fragile(),
                                request.storageLocation(),
                                request.expectedAt(),
                                request.notes(),
                                request.codAmount(),
                                request.receivedNow())));
    }

    @PatchMapping("/{id}")
    DeliveryResponse.View action(@PathVariable UUID id, @Valid @RequestBody ActionRequest request) {
        return mapper.toResponse(
                service.action(
                        actor(),
                        id,
                        new DeliveryModels.ActionCommand(
                                DeliveryAction.from(request.action()), request.note(),
                                request.collectorName(), request.pickupCode())));
    }

    @PostMapping("/{id}/checkout")
    DeliveryResponse.View checkout(
            @PathVariable UUID id, @Valid @RequestBody CheckoutRequest request) {
        return mapper.toResponse(
                service.checkout(
                        actor(),
                        id,
                        new DeliveryModels.PaymentCommand(
                                PaymentMethod.from(request.paymentMethod()))));
    }

    @PostMapping("/{id}/cod")
    DeliveryResponse.View recordCod(@PathVariable UUID id, @Valid @RequestBody CodRequest request) {
        return mapper.toResponse(
                service.recordCod(
                        actor(),
                        id,
                        new DeliveryModels.PaymentCommand(
                                PaymentMethod.from(request.paymentMethod()))));
    }

    private DeliveryModels.Actor actor() {
        return new DeliveryModels.Actor(
                CurrentUser.societyId(), CurrentUser.id(), UserRole.from(CurrentUser.role()));
    }
}
