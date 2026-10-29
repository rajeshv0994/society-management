package com.societyapp.application.model.amenity;

import com.societyapp.domain.type.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class AmenityModels {
    private AmenityModels() {}

    public record AmenityCommand(
            String name,
            AmenityType amenityType,
            String amenityCode,
            String description,
            String location,
            Integer capacity,
            BigDecimal hourlyCharge,
            AmenityPricingType pricingType,
            BigDecimal bookingCharge,
            BigDecimal refundableDeposit,
            LocalTime opensAt,
            LocalTime closesAt,
            boolean bookingApprovalRequired,
            int slotMinutes,
            int advanceBookingDays,
            int cancellationHours,
            String instructions,
            boolean active) {}

    public record BookingCommand(
            OffsetDateTime startsAt, OffsetDateTime endsAt, int attendees, String purpose) {}

    public record Actor(UUID userId, UserRole role) {}

    public record ActionCommand(AmenityBookingAction action, String reason) {}

    public record AmenityView(
            UUID id,
            UUID societyId,
            String name,
            AmenityType amenityType,
            String amenityCode,
            String description,
            String location,
            Integer capacity,
            BigDecimal hourlyCharge,
            AmenityPricingType pricingType,
            BigDecimal bookingCharge,
            BigDecimal refundableDeposit,
            LocalTime opensAt,
            LocalTime closesAt,
            boolean bookingApprovalRequired,
            int slotMinutes,
            int advanceBookingDays,
            int cancellationHours,
            String instructions,
            boolean active,
            OffsetDateTime updatedAt) {}

    public record BookingView(
            UUID id,
            String bookingNumber,
            UUID amenityId,
            String amenityName,
            AmenityType amenityType,
            String amenityCode,
            String amenityLocation,
            Boolean approvalRequired,
            Integer cancellationHours,
            UUID bookedBy,
            String memberName,
            String memberMobile,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            int attendees,
            String purpose,
            BigDecimal amount,
            BigDecimal baseCharge,
            BigDecimal depositAmount,
            AmenityBookingStatus status,
            PaymentStatus paymentStatus,
            PaymentMethod paymentMethod,
            String paymentReference,
            String rejectionReason,
            OffsetDateTime checkedInAt,
            OffsetDateTime checkedOutAt,
            OffsetDateTime createdAt) {}
}
