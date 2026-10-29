package com.societyapp.application.service;

import com.societyapp.application.command.AmenityBookingCreator;
import com.societyapp.application.command.AmenityManagementCoordinator;
import com.societyapp.application.exception.AuthorizationException;
import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.model.amenity.AmenityModels;
import com.societyapp.application.port.in.AmenityUseCase;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.query.AmenityQueryCoordinator;
import com.societyapp.application.usecase.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.AmenityBookingAction;
import com.societyapp.domain.type.AmenityBookingStatus;
import com.societyapp.domain.type.PaymentMethod;
import com.societyapp.domain.type.PaymentStatus;
import com.societyapp.domain.type.StaffPermissionCode;
import com.societyapp.domain.type.UserRole;
import java.math.*;
import java.time.*;
import java.util.*;

public class AmenityService implements AmenityUseCase {
    private static final Set<PaymentMethod> ONLINE_METHODS =
            Set.of(PaymentMethod.UPI, PaymentMethod.CARD, PaymentMethod.NET_BANKING);

    private final AmenityRepository amenities;
    private final AmenityBookingRepository bookings;
    private final StaffPermissionRepository permissions;
    private final UserAccountJpaRepository users;
    private final SocietyPaymentSettingRepository paymentSettings;
    private final AmenityQueryCoordinator queries;
    private final AmenityManagementCoordinator management;
    private final AmenityBookingCreator bookingCreator;

    public AmenityService(
            AmenityRepository amenities,
            AmenityBookingRepository bookings,
            StaffPermissionRepository permissions,
            UserAccountJpaRepository users,
            SocietyPaymentSettingRepository paymentSettings) {
        this.amenities = amenities;
        this.bookings = bookings;
        this.permissions = permissions;
        this.users = users;
        this.paymentSettings = paymentSettings;
        this.queries = new AmenityQueryCoordinator(amenities, bookings, users);
        this.management = new AmenityManagementCoordinator(amenities, queries);
        this.bookingCreator = new AmenityBookingCreator(amenities, bookings, queries);
    }

    @Override
    public List<AmenityModels.AmenityView> list(UUID societyId, UserRole role) {
        return queries.amenities(societyId, role);
    }

    @Override
    public List<AmenityModels.BookingView> bookings(UUID societyId, AmenityModels.Actor actor) {
        if (actor.role() == UserRole.STAFF) requireStaffPermission(actor.userId());
        return queries.bookings(societyId, actor.userId(), actor.role());
    }

    @Override
    public AmenityModels.AmenityView create(UUID societyId, AmenityModels.AmenityCommand command) {
        return management.create(societyId, command);
    }

    @Override
    public AmenityModels.AmenityView update(
            UUID societyId, UUID amenityId, AmenityModels.AmenityCommand command) {
        return management.update(societyId, amenityId, command);
    }

    @Override
    public AmenityModels.BookingView book(
            UUID societyId, UUID userId, UUID amenityId, AmenityModels.BookingCommand command) {
        return bookingCreator.book(societyId, userId, amenityId, command);
    }

    @Override
    public AmenityModels.BookingView checkout(
            UUID societyId,
            AmenityModels.Actor actor,
            UUID bookingId,
            PaymentMethod requestedMethod) {
        if (actor.role() == UserRole.STAFF) requireStaffPermission(actor.userId());
        if (!ONLINE_METHODS.contains(requestedMethod)) {
            throw new IllegalArgumentException("Invalid Razorpay payment method");
        }
        var setting =
                paymentSettings
                        .findBySocietyId(societyId)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Society online payment account is not configured"));
        if (!setting.isTestMode()) {
            throw new IllegalStateException("Live Razorpay amenity payments are not enabled yet");
        }
        var booking = requireBooking(societyId, bookingId);
        requireBookingOwnerOrManager(booking, actor);
        if (!PaymentStatus.PENDING.matches(booking.getPaymentStatus())) {
            throw new IllegalStateException("This booking does not have a pending payment");
        }
        booking.setPaymentStatus(PaymentStatus.PAID.code());
        booking.setPaymentMethod(requestedMethod.razorpayCode());
        booking.setPaymentReference(
                "pay_amenity_test_"
                        + UUID.randomUUID().toString().replace("-", "").substring(0, 18));
        moveAfterPayment(booking);
        booking.setUpdatedAt(OffsetDateTime.now());
        return queries.booking(bookings.save(booking));
    }

    @Override
    public AmenityModels.BookingView recordCash(
            UUID societyId, AmenityModels.Actor actor, UUID bookingId) {
        requireManager(actor);
        var booking = requireBooking(societyId, bookingId);
        if (!PaymentStatus.PENDING.matches(booking.getPaymentStatus())) {
            throw new IllegalStateException("This booking does not have a pending payment");
        }
        booking.setPaymentStatus(PaymentStatus.PAID.code());
        booking.setPaymentMethod(PaymentMethod.CASH.code());
        booking.setPaymentReference(
                "AM-CASH-"
                        + UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 12)
                                .toUpperCase());
        moveAfterPayment(booking);
        booking.setUpdatedAt(OffsetDateTime.now());
        return queries.booking(bookings.save(booking));
    }

    @Override
    public AmenityModels.BookingView action(
            UUID societyId,
            AmenityModels.Actor actor,
            UUID bookingId,
            AmenityModels.ActionCommand command) {
        var booking = requireBooking(societyId, bookingId);
        var requestedAction = command.action();
        if (requestedAction == AmenityBookingAction.CANCEL) {
            requireBookingOwnerOrManager(booking, actor);
            var amenity = requireAmenity(societyId, booking.getAmenityId(), false);
            if (actor.role() == UserRole.RESIDENT
                    && OffsetDateTime.now()
                            .plusHours(amenity.getCancellationHours())
                            .isAfter(booking.getStartsAt())) {
                throw new IllegalStateException(
                        "Cancellation requires "
                                + amenity.getCancellationHours()
                                + " hours notice");
            }
            if (!Set.of(
                            PaymentStatus.PENDING_PAYMENT.code(),
                            AmenityBookingStatus.PENDING_APPROVAL.code(),
                            AmenityBookingStatus.CONFIRMED.code())
                    .contains(booking.getStatus())) {
                throw new IllegalStateException("This booking cannot be cancelled");
            }
            booking.setStatus(AmenityBookingStatus.CANCELLED.code());
            booking.setCancelledAt(OffsetDateTime.now());
            if (PaymentStatus.PAID.matches(booking.getPaymentStatus())) {
                booking.setPaymentStatus(PaymentStatus.REFUND_PENDING.code());
            }
        } else {
            requireManager(actor);
            switch (requestedAction) {
                case APPROVE -> {
                    requireStatus(booking, AmenityBookingStatus.PENDING_APPROVAL);
                    booking.setStatus(AmenityBookingStatus.CONFIRMED.code());
                    booking.setReviewedBy(actor.userId());
                    booking.setReviewedAt(OffsetDateTime.now());
                }
                case REJECT -> {
                    requireStatus(booking, AmenityBookingStatus.PENDING_APPROVAL);
                    if (command.reason() == null || command.reason().isBlank()) {
                        throw new IllegalArgumentException("Rejection reason is required");
                    }
                    booking.setStatus(AmenityBookingStatus.REJECTED.code());
                    booking.setRejectionReason(command.reason().trim());
                    booking.setReviewedBy(actor.userId());
                    booking.setReviewedAt(OffsetDateTime.now());
                    if (PaymentStatus.PAID.matches(booking.getPaymentStatus())) {
                        booking.setPaymentStatus(PaymentStatus.REFUND_PENDING.code());
                    }
                }
                case CHECK_IN -> {
                    requireStatus(booking, AmenityBookingStatus.CONFIRMED);
                    booking.setStatus(AmenityBookingStatus.CHECKED_IN.code());
                    booking.setCheckedInAt(OffsetDateTime.now());
                }
                case CHECK_OUT -> {
                    requireStatus(booking, AmenityBookingStatus.CHECKED_IN);
                    booking.setStatus(AmenityBookingStatus.COMPLETED.code());
                    booking.setCheckedOutAt(OffsetDateTime.now());
                }
                case REFUND -> {
                    if (!PaymentStatus.REFUND_PENDING.matches(booking.getPaymentStatus())) {
                        throw new IllegalStateException("Booking does not have a pending refund");
                    }
                    booking.setPaymentStatus(PaymentStatus.REFUNDED.code());
                }
                case CANCEL -> throw new IllegalStateException("Cancellation was already handled");
            }
        }
        booking.setUpdatedAt(OffsetDateTime.now());
        return queries.booking(bookings.save(booking));
    }

    private void moveAfterPayment(AmenityBooking booking) {
        var amenity = requireAmenity(booking.getSocietyId(), booking.getAmenityId(), false);
        booking.setStatus(
                amenity.isBookingApprovalRequired()
                        ? AmenityBookingStatus.PENDING_APPROVAL.code()
                        : AmenityBookingStatus.CONFIRMED.code());
    }

    private Amenity requireAmenity(UUID societyId, UUID amenityId, boolean active) {
        return amenities
                .findByIdAndSocietyId(amenityId, societyId)
                .filter(item -> !active || item.isActive())
                .orElseThrow(() -> new ResourceNotFoundException("Amenity not found"));
    }

    private AmenityBooking requireBooking(UUID societyId, UUID bookingId) {
        return bookings.findByIdAndSocietyId(bookingId, societyId)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity booking not found"));
    }

    private void requireBookingOwnerOrManager(AmenityBooking booking, AmenityModels.Actor actor) {
        if (actor.role() == UserRole.RESIDENT && !booking.getBookedBy().equals(actor.userId())) {
            throw new AuthorizationException("You can only manage your own amenity booking");
        }
        if (actor.role() == UserRole.STAFF) requireStaffPermission(actor.userId());
    }

    private void requireManager(AmenityModels.Actor actor) {
        if (actor.role() == UserRole.RESIDENT) {
            throw new AuthorizationException("Amenity management permission is required");
        }
        if (actor.role() == UserRole.STAFF) requireStaffPermission(actor.userId());
    }

    private void requireStaffPermission(UUID userId) {
        if (!permissions.existsByStaffUserIdAndPermissionCode(
                userId, StaffPermissionCode.AMENITIES.code())) {
            throw new AuthorizationException("Amenities permission is required");
        }
    }

    private void requireStatus(AmenityBooking booking, AmenityBookingStatus status) {
        if (!status.matches(booking.getStatus())) {
            throw new IllegalStateException(
                    "Booking must be " + status.code().toLowerCase(Locale.ROOT));
        }
    }
}
