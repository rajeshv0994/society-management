package com.societyapp.application.query;

import com.societyapp.application.model.amenity.AmenityModels;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.util.List;
import java.util.UUID;

public class AmenityQueryCoordinator {
    private final AmenityRepository amenities;
    private final AmenityBookingRepository bookings;
    private final UserAccountJpaRepository users;

    public AmenityQueryCoordinator(
            AmenityRepository amenities,
            AmenityBookingRepository bookings,
            UserAccountJpaRepository users) {
        this.amenities = amenities;
        this.bookings = bookings;
        this.users = users;
    }

    public List<AmenityModels.AmenityView> amenities(UUID societyId, UserRole role) {
        return amenities.findBySocietyIdOrderByName(societyId).stream()
                .filter(item -> role != UserRole.RESIDENT || item.isActive())
                .map(this::amenity)
                .toList();
    }

    public List<AmenityModels.BookingView> bookings(UUID societyId, UUID userId, UserRole role) {
        var rows =
                role == UserRole.RESIDENT
                        ? bookings.findBySocietyIdAndBookedByOrderByStartsAtDesc(societyId, userId)
                        : bookings.findBySocietyIdOrderByStartsAtDesc(societyId);
        return rows.stream().map(this::booking).toList();
    }

    public AmenityModels.AmenityView amenity(Amenity item) {
        return new AmenityModels.AmenityView(
                item.getId(),
                item.getSocietyId(),
                item.getName(),
                AmenityType.from(item.getAmenityType()),
                item.getAmenityCode(),
                item.getDescription(),
                item.getLocation(),
                item.getCapacity(),
                item.getHourlyCharge(),
                AmenityPricingType.from(item.getPricingType()),
                item.getBookingCharge(),
                item.getRefundableDeposit(),
                item.getOpensAt(),
                item.getClosesAt(),
                item.isBookingApprovalRequired(),
                item.getSlotMinutes(),
                item.getAdvanceBookingDays(),
                item.getCancellationHours(),
                item.getInstructions(),
                item.isActive(),
                item.getUpdatedAt());
    }

    public AmenityModels.BookingView booking(AmenityBooking item) {
        var amenity = amenities.findById(item.getAmenityId()).orElse(null);
        var user = users.findById(item.getBookedBy()).orElse(null);
        return new AmenityModels.BookingView(
                item.getId(),
                item.getBookingNumber(),
                item.getAmenityId(),
                amenity == null ? null : amenity.getName(),
                amenity == null ? null : AmenityType.from(amenity.getAmenityType()),
                amenity == null ? null : amenity.getAmenityCode(),
                amenity == null ? null : amenity.getLocation(),
                amenity == null ? null : amenity.isBookingApprovalRequired(),
                amenity == null ? null : amenity.getCancellationHours(),
                item.getBookedBy(),
                user == null ? null : user.getFullName(),
                user == null ? null : user.getMobile(),
                item.getStartsAt(),
                item.getEndsAt(),
                item.getAttendees(),
                item.getPurpose(),
                item.getAmount(),
                item.getBaseCharge(),
                item.getDepositAmount(),
                AmenityBookingStatus.from(item.getStatus()),
                PaymentStatus.from(item.getPaymentStatus()),
                paymentMethod(item.getPaymentMethod()),
                item.getPaymentReference(),
                item.getRejectionReason(),
                item.getCheckedInAt(),
                item.getCheckedOutAt(),
                item.getCreatedAt());
    }

    private PaymentMethod paymentMethod(String value) {
        if (value == null || value.isBlank()) return null;
        return PaymentMethod.from(value.replaceFirst("^RAZORPAY_", ""));
    }
}
