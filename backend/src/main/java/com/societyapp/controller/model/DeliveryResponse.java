package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class DeliveryResponse {
    private DeliveryResponse() {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record View(
            UUID id,
            @JsonProperty("delivery_number") String deliveryNumber,
            @JsonProperty("tracking_number") String trackingNumber,
            String provider,
            @JsonProperty("sender_name") String senderName,
            @JsonProperty("delivery_person_name") String deliveryPersonName,
            @JsonProperty("delivery_person_mobile") String deliveryPersonMobile,
            @JsonProperty("parcel_type") String parcelType,
            @JsonProperty("package_count") int packageCount,
            @JsonProperty("parcel_size") String parcelSize,
            boolean fragile,
            @JsonProperty("storage_location") String storageLocation,
            @JsonProperty("expected_at") OffsetDateTime expectedAt,
            String notes,
            String status,
            @JsonProperty("resident_decision") String residentDecision,
            @JsonProperty("authorized_collector") String authorizedCollector,
            @JsonProperty("pickup_code") String pickupCode,
            @JsonProperty("pickup_code_issued") boolean pickupCodeIssued,
            @JsonProperty("cod_amount") BigDecimal codAmount,
            @JsonProperty("cod_payment_status") String codPaymentStatus,
            @JsonProperty("cod_payment_method") String codPaymentMethod,
            @JsonProperty("cod_payment_reference") String codPaymentReference,
            @JsonProperty("received_at") OffsetDateTime receivedAt,
            @JsonProperty("collected_by_name") String collectedByName,
            @JsonProperty("collected_at") OffsetDateTime collectedAt,
            @JsonProperty("delivered_at") OffsetDateTime deliveredAt,
            @JsonProperty("returned_at") OffsetDateTime returnedAt,
            @JsonProperty("closed_reason") String closedReason,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("resident_id") UUID residentId,
            @JsonProperty("resident_name") String residentName,
            @JsonProperty("resident_mobile") String residentMobile,
            @JsonProperty("flat_id") UUID flatId,
            @JsonProperty("space_number") String spaceNumber,
            String block) {}

    public record ResidentSpace(
            @JsonProperty("resident_id") UUID residentId,
            @JsonProperty("resident_name") String residentName,
            @JsonProperty("resident_mobile") String residentMobile,
            @JsonProperty("flat_id") UUID flatId,
            @JsonProperty("space_number") String spaceNumber,
            String block) {}

    public record Workspace(
            List<View> deliveries,
            List<ResidentSpace> options,
            int total,
            @JsonProperty("awaiting_action") long awaitingAction,
            @JsonProperty("ready_for_pickup") long readyForPickup,
            long completed,
            @JsonProperty("pending_cod") long pendingCod) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record HistoryEntry(
            UUID id,
            @JsonProperty("event_type") String eventType,
            @JsonProperty("from_status") String fromStatus,
            @JsonProperty("to_status") String toStatus,
            String note,
            @JsonProperty("actor_role") String actorRole,
            @JsonProperty("occurred_at") OffsetDateTime occurredAt,
            @JsonProperty("actor_name") String actorName) {}
}
