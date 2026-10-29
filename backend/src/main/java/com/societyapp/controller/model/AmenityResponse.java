package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class AmenityResponse {
    private AmenityResponse() {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Amenity(
            UUID id,
            @JsonProperty("society_id") UUID societyId,
            String name,
            @JsonProperty("amenity_type") String amenityType,
            @JsonProperty("amenity_code") String amenityCode,
            String description,
            String location,
            Integer capacity,
            @JsonProperty("hourly_charge") BigDecimal hourlyCharge,
            @JsonProperty("pricing_type") String pricingType,
            @JsonProperty("booking_charge") BigDecimal bookingCharge,
            @JsonProperty("refundable_deposit") BigDecimal refundableDeposit,
            @JsonProperty("opens_at") LocalTime opensAt,
            @JsonProperty("closes_at") LocalTime closesAt,
            @JsonProperty("booking_approval_required") boolean bookingApprovalRequired,
            @JsonProperty("slot_minutes") int slotMinutes,
            @JsonProperty("advance_booking_days") int advanceBookingDays,
            @JsonProperty("cancellation_hours") int cancellationHours,
            String instructions,
            boolean active,
            @JsonProperty("updated_at") OffsetDateTime updatedAt) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Booking(
            UUID id,
            @JsonProperty("booking_number") String bookingNumber,
            @JsonProperty("amenity_id") UUID amenityId,
            @JsonProperty("amenity_name") String amenityName,
            @JsonProperty("amenity_type") String amenityType,
            @JsonProperty("amenity_code") String amenityCode,
            @JsonProperty("amenity_location") String amenityLocation,
            @JsonProperty("approval_required") Boolean approvalRequired,
            @JsonProperty("cancellation_hours") Integer cancellationHours,
            @JsonProperty("booked_by") UUID bookedBy,
            @JsonProperty("member_name") String memberName,
            @JsonProperty("member_mobile") String memberMobile,
            @JsonProperty("starts_at") OffsetDateTime startsAt,
            @JsonProperty("ends_at") OffsetDateTime endsAt,
            int attendees,
            String purpose,
            BigDecimal amount,
            @JsonProperty("base_charge") BigDecimal baseCharge,
            @JsonProperty("deposit_amount") BigDecimal depositAmount,
            String status,
            @JsonProperty("payment_status") String paymentStatus,
            @JsonProperty("payment_method") String paymentMethod,
            @JsonProperty("payment_reference") String paymentReference,
            @JsonProperty("rejection_reason") String rejectionReason,
            @JsonProperty("checked_in_at") OffsetDateTime checkedInAt,
            @JsonProperty("checked_out_at") OffsetDateTime checkedOutAt,
            @JsonProperty("created_at") OffsetDateTime createdAt) {}
}
