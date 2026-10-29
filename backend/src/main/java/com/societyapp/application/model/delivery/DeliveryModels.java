package com.societyapp.application.model.delivery;

import com.societyapp.domain.type.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class DeliveryModels {
    private DeliveryModels() {}

    public record Actor(UUID societyId, UUID userId, UserRole role) {}

    public record CreateCommand(
            UUID residentId,
            UUID spaceId,
            String trackingNumber,
            String provider,
            String senderName,
            String deliveryPersonName,
            String deliveryPersonMobile,
            ParcelType parcelType,
            int packageCount,
            ParcelSize parcelSize,
            boolean fragile,
            String storageLocation,
            OffsetDateTime expectedAt,
            String notes,
            BigDecimal codAmount,
            boolean receivedNow) {}

    public record ActionCommand(
            DeliveryAction action, String note, String collectorName, String pickupCode) {}

    public record PaymentCommand(PaymentMethod paymentMethod) {}

    public record ResidentSpace(
            UUID residentId,
            String residentName,
            String residentMobile,
            UUID spaceId,
            String spaceNumber,
            String block) {}

    public record View(
            UUID id,
            String deliveryNumber,
            String trackingNumber,
            String provider,
            String senderName,
            String deliveryPersonName,
            String deliveryPersonMobile,
            ParcelType parcelType,
            int packageCount,
            ParcelSize parcelSize,
            boolean fragile,
            String storageLocation,
            OffsetDateTime expectedAt,
            String notes,
            DeliveryStatus status,
            DeliveryDecision residentDecision,
            String authorizedCollector,
            String pickupCode,
            boolean pickupCodeIssued,
            BigDecimal codAmount,
            PaymentStatus codPaymentStatus,
            DeliveryPaymentMethod codPaymentMethod,
            String codPaymentReference,
            OffsetDateTime receivedAt,
            String collectedByName,
            OffsetDateTime collectedAt,
            OffsetDateTime deliveredAt,
            OffsetDateTime returnedAt,
            String closedReason,
            OffsetDateTime createdAt,
            UUID residentId,
            String residentName,
            String residentMobile,
            UUID spaceId,
            String spaceNumber,
            String block) {}

    public record Workspace(
            List<View> deliveries,
            List<ResidentSpace> options,
            int total,
            long awaitingAction,
            long readyForPickup,
            long completed,
            long pendingCod) {}

    public record HistoryEntry(
            UUID id,
            DeliveryEventType eventType,
            DeliveryStatus fromStatus,
            DeliveryStatus toStatus,
            String note,
            UserRole actorRole,
            OffsetDateTime occurredAt,
            String actorName) {}
}
