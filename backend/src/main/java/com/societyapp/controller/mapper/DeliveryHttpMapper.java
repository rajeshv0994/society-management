package com.societyapp.controller.mapper;

import com.societyapp.application.model.delivery.DeliveryModels;
import com.societyapp.controller.model.DeliveryResponse;

public final class DeliveryHttpMapper {
    public DeliveryResponse.Workspace toResponse(DeliveryModels.Workspace value) {
        return new DeliveryResponse.Workspace(
                value.deliveries().stream().map(this::toResponse).toList(),
                value.options().stream().map(this::option).toList(),
                value.total(),
                value.awaitingAction(),
                value.readyForPickup(),
                value.completed(),
                value.pendingCod());
    }

    public DeliveryResponse.View toResponse(DeliveryModels.View value) {
        return new DeliveryResponse.View(
                value.id(),
                value.deliveryNumber(),
                value.trackingNumber(),
                value.provider(),
                value.senderName(),
                value.deliveryPersonName(),
                value.deliveryPersonMobile(),
                value.parcelType().code(),
                value.packageCount(),
                value.parcelSize().code(),
                value.fragile(),
                value.storageLocation(),
                value.expectedAt(),
                value.notes(),
                value.status().code(),
                value.residentDecision() == null ? null : value.residentDecision().code(),
                value.authorizedCollector(),
                value.pickupCode(),
                value.pickupCodeIssued(),
                value.codAmount(),
                value.codPaymentStatus().code(),
                value.codPaymentMethod() == null ? null : value.codPaymentMethod().code(),
                value.codPaymentReference(),
                value.receivedAt(),
                value.collectedByName(),
                value.collectedAt(),
                value.deliveredAt(),
                value.returnedAt(),
                value.closedReason(),
                value.createdAt(),
                value.residentId(),
                value.residentName(),
                value.residentMobile(),
                value.spaceId(),
                value.spaceNumber(),
                value.block());
    }

    public DeliveryResponse.HistoryEntry toResponse(DeliveryModels.HistoryEntry value) {
        return new DeliveryResponse.HistoryEntry(
                value.id(),
                value.eventType().code(),
                value.fromStatus() == null ? null : value.fromStatus().code(),
                value.toStatus() == null ? null : value.toStatus().code(),
                value.note(),
                value.actorRole().code(),
                value.occurredAt(),
                value.actorName());
    }

    private DeliveryResponse.ResidentSpace option(DeliveryModels.ResidentSpace value) {
        return new DeliveryResponse.ResidentSpace(
                value.residentId(),
                value.residentName(),
                value.residentMobile(),
                value.spaceId(),
                value.spaceNumber(),
                value.block());
    }
}
