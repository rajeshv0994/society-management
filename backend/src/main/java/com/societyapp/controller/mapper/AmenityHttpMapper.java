package com.societyapp.controller.mapper;

import com.societyapp.application.model.amenity.AmenityModels;
import com.societyapp.controller.model.AmenityResponse;

public final class AmenityHttpMapper {
    public AmenityResponse.Amenity toResponse(AmenityModels.AmenityView value) {
        return new AmenityResponse.Amenity(
                value.id(),
                value.societyId(),
                value.name(),
                value.amenityType().code(),
                value.amenityCode(),
                value.description(),
                value.location(),
                value.capacity(),
                value.hourlyCharge(),
                value.pricingType().code(),
                value.bookingCharge(),
                value.refundableDeposit(),
                value.opensAt(),
                value.closesAt(),
                value.bookingApprovalRequired(),
                value.slotMinutes(),
                value.advanceBookingDays(),
                value.cancellationHours(),
                value.instructions(),
                value.active(),
                value.updatedAt());
    }

    public AmenityResponse.Booking toResponse(AmenityModels.BookingView value) {
        return new AmenityResponse.Booking(
                value.id(),
                value.bookingNumber(),
                value.amenityId(),
                value.amenityName(),
                code(value.amenityType()),
                value.amenityCode(),
                value.amenityLocation(),
                value.approvalRequired(),
                value.cancellationHours(),
                value.bookedBy(),
                value.memberName(),
                value.memberMobile(),
                value.startsAt(),
                value.endsAt(),
                value.attendees(),
                value.purpose(),
                value.amount(),
                value.baseCharge(),
                value.depositAmount(),
                value.status().code(),
                value.paymentStatus().code(),
                paymentMethod(value),
                value.paymentReference(),
                value.rejectionReason(),
                value.checkedInAt(),
                value.checkedOutAt(),
                value.createdAt());
    }

    private String paymentMethod(AmenityModels.BookingView value) {
        if (value.paymentMethod() == null) return null;
        return value.paymentReference() != null
                        && value.paymentReference().startsWith("pay_amenity_test_")
                ? value.paymentMethod().razorpayCode()
                : value.paymentMethod().code();
    }

    private String code(Enum<?> value) {
        return value == null ? null : value.name();
    }
}
