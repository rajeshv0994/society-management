package com.societyapp.application.command;

import com.societyapp.application.exception.ConflictException;
import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.model.amenity.AmenityModels;
import com.societyapp.application.port.out.persistence.AmenityBookingRepository;
import com.societyapp.application.port.out.persistence.AmenityRepository;
import com.societyapp.application.query.AmenityQueryCoordinator;
import com.societyapp.domain.model.Amenity;
import com.societyapp.domain.model.AmenityBooking;
import com.societyapp.domain.type.*;
import java.math.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AmenityBookingCreator {
    private static final ZoneId SOCIETY_TIME_ZONE = ZoneId.of("Asia/Kolkata");
    private static final Set<String> BLOCKING_STATUSES =
            Set.of(
                    PaymentStatus.PENDING_PAYMENT.code(),
                            AmenityBookingStatus.PENDING_APPROVAL.code(),
                    AmenityBookingStatus.CONFIRMED.code(), AmenityBookingStatus.CHECKED_IN.code());

    private final AmenityRepository amenities;
    private final AmenityBookingRepository bookings;
    private final AmenityQueryCoordinator views;

    public AmenityBookingCreator(
            AmenityRepository amenities,
            AmenityBookingRepository bookings,
            AmenityQueryCoordinator views) {
        this.amenities = amenities;
        this.bookings = bookings;
        this.views = views;
    }

    public AmenityModels.BookingView book(
            UUID societyId, UUID userId, UUID amenityId, AmenityModels.BookingCommand command) {
        var start = command.startsAt();
        var end = command.endsAt();
        if (!end.isAfter(start))
            throw new IllegalArgumentException("Booking end must be after start");
        if (!start.isAfter(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Booking must start in the future");
        }
        var amenity =
                amenities
                        .findByIdAndSocietyId(amenityId, societyId)
                        .filter(Amenity::isActive)
                        .orElseThrow(() -> new ResourceNotFoundException("Amenity not found"));
        if (start.isAfter(OffsetDateTime.now().plusDays(amenity.getAdvanceBookingDays()))) {
            throw new IllegalArgumentException(
                    "This amenity can be booked up to "
                            + amenity.getAdvanceBookingDays()
                            + " days ahead");
        }
        if (amenity.getCapacity() != null && command.attendees() > amenity.getCapacity()) {
            throw new IllegalArgumentException(
                    "Attendees exceed the amenity capacity of " + amenity.getCapacity());
        }
        validateOperatingHours(amenity, start, end);
        long minutes = Duration.between(start, end).toMinutes();
        if (minutes < amenity.getSlotMinutes() || minutes % amenity.getSlotMinutes() != 0) {
            throw new IllegalArgumentException(
                    "Booking duration must use " + amenity.getSlotMinutes() + " minute slots");
        }
        if (bookings.existsByAmenityIdAndStatusInAndStartsAtLessThanAndEndsAtGreaterThan(
                amenityId, BLOCKING_STATUSES, end, start)) {
            throw new ConflictException("Amenity is already reserved for this time");
        }
        var baseCharge = calculateBaseCharge(amenity, minutes);
        var deposit = amenity.getRefundableDeposit();
        var amount = baseCharge.add(deposit);
        var booking = new AmenityBooking();
        booking.setSocietyId(societyId);
        booking.setAmenityId(amenityId);
        booking.setBookedBy(userId);
        booking.setBookingNumber(bookingNumber());
        booking.setStartsAt(start);
        booking.setEndsAt(end);
        booking.setAttendees(command.attendees());
        booking.setPurpose(blank(command.purpose()));
        booking.setAmount(amount);
        booking.setBaseCharge(baseCharge);
        booking.setDepositAmount(deposit);
        booking.setPaymentStatus(
                amount.signum() == 0 ? PaymentStatus.FREE.code() : PaymentStatus.PENDING.code());
        booking.setStatus(
                amount.signum() > 0
                        ? PaymentStatus.PENDING_PAYMENT.code()
                        : amenity.isBookingApprovalRequired()
                                ? AmenityBookingStatus.PENDING_APPROVAL.code()
                                : AmenityBookingStatus.CONFIRMED.code());
        booking.setCreatedAt(OffsetDateTime.now());
        booking.setUpdatedAt(OffsetDateTime.now());
        return views.booking(bookings.save(booking));
    }

    private BigDecimal calculateBaseCharge(Amenity amenity, long minutes) {
        return switch (AmenityPricingType.from(amenity.getPricingType())) {
            case FREE -> BigDecimal.ZERO;
            case FIXED -> amenity.getBookingCharge();
            case PER_SLOT ->
                    amenity.getBookingCharge()
                            .multiply(BigDecimal.valueOf(minutes / amenity.getSlotMinutes()));
            case HOURLY ->
                    amenity.getHourlyCharge()
                            .multiply(BigDecimal.valueOf(minutes))
                            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        };
    }

    private void validateOperatingHours(Amenity amenity, OffsetDateTime start, OffsetDateTime end) {
        var localStart = start.atZoneSameInstant(SOCIETY_TIME_ZONE);
        var localEnd = end.atZoneSameInstant(SOCIETY_TIME_ZONE);
        if (!localStart.toLocalDate().equals(localEnd.toLocalDate())) {
            throw new IllegalArgumentException("Booking must start and end on the same day");
        }
        if (amenity.getOpensAt() != null
                && localStart.toLocalTime().isBefore(amenity.getOpensAt())) {
            throw new IllegalArgumentException("Booking starts before the amenity opens");
        }
        if (amenity.getClosesAt() != null
                && localEnd.toLocalTime().isAfter(amenity.getClosesAt())) {
            throw new IllegalArgumentException("Booking ends after the amenity closes");
        }
    }

    private String bookingNumber() {
        return "AMB-"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"))
                + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
