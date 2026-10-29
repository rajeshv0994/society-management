package com.societyapp.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.societyapp.application.exception.AuthorizationException;
import com.societyapp.application.exception.ConflictException;
import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.model.amenity.AmenityModels;
import com.societyapp.application.port.out.persistence.AmenityBookingRepository;
import com.societyapp.application.port.out.persistence.AmenityRepository;
import com.societyapp.application.port.out.persistence.SocietyPaymentSettingRepository;
import com.societyapp.application.port.out.persistence.StaffPermissionRepository;
import com.societyapp.application.port.out.persistence.UserAccountJpaRepository;
import com.societyapp.application.service.AmenityService;
import com.societyapp.domain.model.Amenity;
import com.societyapp.domain.model.AmenityBooking;
import com.societyapp.domain.model.SocietyPaymentSetting;
import com.societyapp.domain.type.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AmenityServiceTest {
    private final AmenityRepository amenities = mock(AmenityRepository.class);
    private final AmenityBookingRepository bookings = mock(AmenityBookingRepository.class);
    private final StaffPermissionRepository permissions = mock(StaffPermissionRepository.class);
    private final UserAccountJpaRepository users = mock(UserAccountJpaRepository.class);
    private final SocietyPaymentSettingRepository paymentSettings =
            mock(SocietyPaymentSettingRepository.class);
    private final LegacyAmenityFacade service =
            new LegacyAmenityFacade(
                    new AmenityService(amenities, bookings, permissions, users, paymentSettings));

    private final UUID societyId = UUID.randomUUID();
    private final UUID amenityId = UUID.randomUUID();
    private final UUID residentId = UUID.randomUUID();
    private final UUID staffId = UUID.randomUUID();

    @BeforeEach
    void saveReturnsEntityAndViewsCanResolveAmenity() {
        when(amenities.save(any(Amenity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(bookings.save(any(AmenityBooking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(users.findById(any())).thenReturn(Optional.empty());
    }

    @Test
    void residentsOnlySeeActiveFacilitiesButManagersSeeAllInstances() {
        Amenity active = amenity("HOURLY", "100", "0", true, false);
        Amenity inactive = amenity("FREE", "0", "0", false, false);
        inactive.setName("Pool maintenance");
        when(amenities.findBySocietyIdOrderByName(societyId)).thenReturn(List.of(active, inactive));

        assertThat(service.list(societyId, "RESIDENT")).hasSize(1);
        assertThat(service.list(societyId, "SOCIETY_ADMIN")).hasSize(2);
    }

    @Test
    void createNormalizesCodeAndStoresCompleteFacilityConfiguration() {
        var result =
                service.create(
                        societyId,
                        " Community Hall B ",
                        "COMMUNITY_CENTER",
                        " hall-b ",
                        " Events ",
                        " First floor ",
                        80,
                        BigDecimal.ZERO,
                        "FIXED",
                        new BigDecimal("750"),
                        new BigDecimal("500"),
                        LocalTime.of(8, 0),
                        LocalTime.of(22, 0),
                        true,
                        60,
                        90,
                        24,
                        " ID required ");

        assertThat(result)
                .containsEntry("name", "Community Hall B")
                .containsEntry("amenity_code", "HALL-B")
                .containsEntry("pricing_type", "FIXED")
                .containsEntry("booking_charge", new BigDecimal("750"))
                .containsEntry("refundable_deposit", new BigDecimal("500"));
    }

    @Test
    void invalidOperatingWindowAndCrossSocietyUpdateAreRejected() {
        assertThatThrownBy(
                        () ->
                                service.create(
                                        societyId,
                                        "Gym",
                                        "GYM",
                                        "GYM-A",
                                        null,
                                        null,
                                        20,
                                        BigDecimal.ZERO,
                                        "FREE",
                                        BigDecimal.ZERO,
                                        BigDecimal.ZERO,
                                        LocalTime.of(20, 0),
                                        LocalTime.of(8, 0),
                                        false,
                                        60,
                                        30,
                                        2,
                                        null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Closing time");

        when(amenities.findByIdAndSocietyId(amenityId, societyId)).thenReturn(Optional.empty());
        assertThatThrownBy(
                        () ->
                                service.update(
                                        societyId,
                                        amenityId,
                                        "Gym",
                                        "GYM",
                                        "GYM-A",
                                        null,
                                        null,
                                        20,
                                        BigDecimal.ZERO,
                                        "FREE",
                                        BigDecimal.ZERO,
                                        BigDecimal.ZERO,
                                        null,
                                        null,
                                        false,
                                        60,
                                        30,
                                        2,
                                        null,
                                        true))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    static Stream<Arguments> pricingCases() {
        return Stream.of(
                Arguments.of("FREE", "0", "0", "0", "FREE", "CONFIRMED"),
                Arguments.of("HOURLY", "120", "0", "240.00", "PENDING", "PENDING_PAYMENT"),
                Arguments.of("PER_SLOT", "0", "75", "150", "PENDING", "PENDING_PAYMENT"),
                Arguments.of("FIXED", "0", "500", "500", "PENDING", "PENDING_PAYMENT"));
    }

    @ParameterizedTest
    @MethodSource("pricingCases")
    void calculatesEveryPricingMode(
            String pricingType,
            String hourlyCharge,
            String bookingCharge,
            String expectedBase,
            String paymentStatus,
            String bookingStatus) {
        Amenity amenity = amenity(pricingType, hourlyCharge, bookingCharge, true, false);
        stubBookableAmenity(amenity);
        OffsetDateTime start = futureAt(10, 0);

        var result =
                service.book(
                        societyId, residentId, amenityId, start, start.plusHours(2), 4, "Test");

        assertThat(new BigDecimal(result.get("base_charge").toString()))
                .isEqualByComparingTo(expectedBase);
        assertThat(result)
                .containsEntry("payment_status", paymentStatus)
                .containsEntry("status", bookingStatus);
    }

    @Test
    void depositIsAddedToBaseChargeAndRequiresPaymentEvenForFreeFacility() {
        Amenity amenity = amenity("FREE", "0", "0", true, false);
        amenity.setRefundableDeposit(new BigDecimal("300"));
        stubBookableAmenity(amenity);
        OffsetDateTime start = futureAt(10, 0);

        var result =
                service.book(societyId, residentId, amenityId, start, start.plusHours(1), 2, null);

        assertThat(result)
                .containsEntry("base_charge", BigDecimal.ZERO)
                .containsEntry("deposit_amount", new BigDecimal("300"))
                .containsEntry("amount", new BigDecimal("300"))
                .containsEntry("status", "PENDING_PAYMENT");
    }

    @Test
    void bookingRulesRejectPastInvalidDurationCapacityHoursAdvanceAndConflicts() {
        Amenity amenity = amenity("FREE", "0", "0", true, false);
        amenity.setCapacity(5);
        amenity.setOpensAt(LocalTime.of(8, 0));
        amenity.setClosesAt(LocalTime.of(20, 0));
        stubBookableAmenity(amenity);
        OffsetDateTime valid = futureAt(10, 0);

        assertThatThrownBy(
                        () -> service.book(societyId, residentId, amenityId, valid, valid, 2, null))
                .hasMessageContaining("end must be after start");
        assertThatThrownBy(
                        () ->
                                service.book(
                                        societyId,
                                        residentId,
                                        amenityId,
                                        OffsetDateTime.now().minusHours(2),
                                        OffsetDateTime.now().minusHours(1),
                                        2,
                                        null))
                .hasMessageContaining("future");
        assertThatThrownBy(
                        () ->
                                service.book(
                                        societyId,
                                        residentId,
                                        amenityId,
                                        valid,
                                        valid.plusHours(1),
                                        6,
                                        null))
                .hasMessageContaining("capacity");
        assertThatThrownBy(
                        () ->
                                service.book(
                                        societyId,
                                        residentId,
                                        amenityId,
                                        valid,
                                        valid.plusMinutes(90),
                                        2,
                                        null))
                .hasMessageContaining("60 minute slots");

        OffsetDateTime beforeOpening = futureAt(7, 0);
        assertThatThrownBy(
                        () ->
                                service.book(
                                        societyId,
                                        residentId,
                                        amenityId,
                                        beforeOpening,
                                        beforeOpening.plusHours(1),
                                        2,
                                        null))
                .hasMessageContaining("before the amenity opens");

        amenity.setAdvanceBookingDays(1);
        OffsetDateTime tooFar = OffsetDateTime.now().plusDays(3);
        assertThatThrownBy(
                        () ->
                                service.book(
                                        societyId,
                                        residentId,
                                        amenityId,
                                        tooFar,
                                        tooFar.plusHours(1),
                                        2,
                                        null))
                .hasMessageContaining("1 days ahead");

        amenity.setAdvanceBookingDays(30);
        when(bookings.existsByAmenityIdAndStatusInAndStartsAtLessThanAndEndsAtGreaterThan(
                        eq(amenityId), anyCollection(), any(), any()))
                .thenReturn(true);
        assertThatThrownBy(
                        () ->
                                service.book(
                                        societyId,
                                        residentId,
                                        amenityId,
                                        valid,
                                        valid.plusHours(1),
                                        2,
                                        null))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void checkoutRequiresValidMethodConfiguredTestAccountAndOwner() {
        AmenityBooking booking = booking("PENDING_PAYMENT", "PENDING", residentId);
        stubBooking(booking, amenity("HOURLY", "100", "0", true, false));

        assertThatThrownBy(
                        () ->
                                service.checkout(
                                        societyId,
                                        residentId,
                                        "RESIDENT",
                                        UUID.randomUUID(),
                                        "cash"))
                .hasMessageContaining("Invalid Razorpay");
        assertThatThrownBy(
                        () ->
                                service.checkout(
                                        societyId,
                                        residentId,
                                        "RESIDENT",
                                        UUID.randomUUID(),
                                        "UPI"))
                .hasMessageContaining("not configured");

        SocietyPaymentSetting setting = new SocietyPaymentSetting();
        setting.setTestMode(false);
        when(paymentSettings.findBySocietyId(societyId)).thenReturn(Optional.of(setting));
        assertThatThrownBy(
                        () ->
                                service.checkout(
                                        societyId,
                                        residentId,
                                        "RESIDENT",
                                        UUID.randomUUID(),
                                        "UPI"))
                .hasMessageContaining("Live Razorpay");

        setting.setTestMode(true);
        assertThatThrownBy(
                        () ->
                                service.checkout(
                                        societyId,
                                        UUID.randomUUID(),
                                        "RESIDENT",
                                        UUID.randomUUID(),
                                        "UPI"))
                .isInstanceOf(AuthorizationException.class);

        var result = service.checkout(societyId, residentId, "RESIDENT", UUID.randomUUID(), "card");
        assertThat(result)
                .containsEntry("payment_status", "PAID")
                .containsEntry("payment_method", "RAZORPAY_CARD")
                .containsEntry("status", "CONFIRMED");
        assertThat(result.get("payment_reference").toString()).startsWith("pay_amenity_test_");
    }

    @Test
    void onlineOrCashPaymentMovesApprovalFacilityToPendingApproval() {
        Amenity amenity = amenity("FIXED", "0", "500", true, true);
        AmenityBooking online = booking("PENDING_PAYMENT", "PENDING", residentId);
        stubBooking(online, amenity);
        SocietyPaymentSetting setting = new SocietyPaymentSetting();
        setting.setTestMode(true);
        when(paymentSettings.findBySocietyId(societyId)).thenReturn(Optional.of(setting));

        assertThat(service.checkout(societyId, residentId, "RESIDENT", UUID.randomUUID(), "UPI"))
                .containsEntry("status", "PENDING_APPROVAL");

        AmenityBooking cash = booking("PENDING_PAYMENT", "PENDING", residentId);
        stubBooking(cash, amenity);
        var result =
                service.recordCash(
                        societyId, UUID.randomUUID(), "SOCIETY_ADMIN", UUID.randomUUID());
        assertThat(result)
                .containsEntry("payment_status", "PAID")
                .containsEntry("payment_method", "CASH")
                .containsEntry("status", "PENDING_APPROVAL");
    }

    @Test
    void staffNeedsAmenitiesPermissionForListingPaymentsAndActions() {
        when(permissions.existsByStaffUserIdAndPermissionCode(staffId, "AMENITIES"))
                .thenReturn(false);

        assertThatThrownBy(() -> service.bookings(societyId, staffId, "STAFF"))
                .isInstanceOf(AuthorizationException.class);
        assertThatThrownBy(() -> service.recordCash(societyId, staffId, "STAFF", UUID.randomUUID()))
                .isInstanceOf(AuthorizationException.class);

        when(permissions.existsByStaffUserIdAndPermissionCode(staffId, "AMENITIES"))
                .thenReturn(true);
        when(bookings.findBySocietyIdOrderByStartsAtDesc(societyId)).thenReturn(List.of());
        assertThat(service.bookings(societyId, staffId, "STAFF")).isEmpty();
    }

    @Test
    void approvalCheckInAndCheckOutCompleteTheBooking() {
        UUID managerId = UUID.randomUUID();
        AmenityBooking booking = booking("PENDING_APPROVAL", "PAID", residentId);
        stubBooking(booking, amenity("FIXED", "0", "500", true, true));

        assertThat(
                        service.action(
                                societyId,
                                managerId,
                                "SOCIETY_ADMIN",
                                UUID.randomUUID(),
                                "APPROVE",
                                null))
                .containsEntry("status", "CONFIRMED");
        assertThat(booking.getReviewedBy()).isEqualTo(managerId);
        assertThat(
                        service.action(
                                societyId,
                                managerId,
                                "SOCIETY_ADMIN",
                                UUID.randomUUID(),
                                "CHECK_IN",
                                null))
                .containsEntry("status", "CHECKED_IN");
        assertThat(
                        service.action(
                                societyId,
                                managerId,
                                "SOCIETY_ADMIN",
                                UUID.randomUUID(),
                                "CHECK_OUT",
                                null))
                .containsEntry("status", "COMPLETED");
        assertThat(booking.getCheckedInAt()).isNotNull();
        assertThat(booking.getCheckedOutAt()).isNotNull();
    }

    @Test
    void rejectionRequiresReasonAndPaidRejectionCanBeRefunded() {
        AmenityBooking booking = booking("PENDING_APPROVAL", "PAID", residentId);
        stubBooking(booking, amenity("FIXED", "0", "500", true, true));

        assertThatThrownBy(
                        () ->
                                service.action(
                                        societyId,
                                        staffId,
                                        "SOCIETY_ADMIN",
                                        UUID.randomUUID(),
                                        "REJECT",
                                        " "))
                .hasMessageContaining("reason");

        var rejected =
                service.action(
                        societyId,
                        staffId,
                        "SOCIETY_ADMIN",
                        UUID.randomUUID(),
                        "REJECT",
                        "Maintenance");
        assertThat(rejected)
                .containsEntry("status", "REJECTED")
                .containsEntry("payment_status", "REFUND_PENDING")
                .containsEntry("rejection_reason", "Maintenance");

        assertThat(
                        service.action(
                                societyId,
                                staffId,
                                "SOCIETY_ADMIN",
                                UUID.randomUUID(),
                                "REFUND",
                                null))
                .containsEntry("payment_status", "REFUNDED");
    }

    @Test
    void residentCancellationEnforcesOwnershipAndNoticeThenCreatesRefund() {
        Amenity amenity = amenity("FIXED", "0", "500", true, false);
        amenity.setCancellationHours(24);
        AmenityBooking booking = booking("CONFIRMED", "PAID", residentId);
        booking.setStartsAt(OffsetDateTime.now().plusHours(12));
        stubBooking(booking, amenity);

        assertThatThrownBy(
                        () ->
                                service.action(
                                        societyId,
                                        UUID.randomUUID(),
                                        "RESIDENT",
                                        UUID.randomUUID(),
                                        "CANCEL",
                                        null))
                .isInstanceOf(AuthorizationException.class);
        assertThatThrownBy(
                        () ->
                                service.action(
                                        societyId,
                                        residentId,
                                        "RESIDENT",
                                        UUID.randomUUID(),
                                        "CANCEL",
                                        null))
                .hasMessageContaining("24 hours notice");

        booking.setStartsAt(OffsetDateTime.now().plusDays(2));
        var result =
                service.action(
                        societyId, residentId, "RESIDENT", UUID.randomUUID(), "CANCEL", null);
        assertThat(result)
                .containsEntry("status", "CANCELLED")
                .containsEntry("payment_status", "REFUND_PENDING");
    }

    @Test
    void invalidLifecycleTransitionsAreRejected() {
        AmenityBooking booking = booking("CONFIRMED", "PAID", residentId);
        stubBooking(booking, amenity("FREE", "0", "0", true, false));

        assertThatThrownBy(
                        () ->
                                service.action(
                                        societyId,
                                        staffId,
                                        "SOCIETY_ADMIN",
                                        UUID.randomUUID(),
                                        "APPROVE",
                                        null))
                .hasMessageContaining("pending_approval");
        assertThatThrownBy(
                        () ->
                                service.action(
                                        societyId,
                                        staffId,
                                        "SOCIETY_ADMIN",
                                        UUID.randomUUID(),
                                        "UNKNOWN",
                                        null))
                .hasMessageContaining("Invalid booking action");
        assertThatThrownBy(
                        () ->
                                service.action(
                                        societyId,
                                        staffId,
                                        "SOCIETY_ADMIN",
                                        UUID.randomUUID(),
                                        "REFUND",
                                        null))
                .hasMessageContaining("pending refund");
    }

    private Amenity amenity(
            String pricingType,
            String hourlyCharge,
            String bookingCharge,
            boolean active,
            boolean approvalRequired) {
        Amenity amenity = new Amenity();
        amenity.setSocietyId(societyId);
        amenity.setName("Community Hall");
        amenity.setAmenityType("COMMUNITY_CENTER");
        amenity.setAmenityCode("HALL-A");
        amenity.setCapacity(20);
        amenity.setHourlyCharge(new BigDecimal(hourlyCharge));
        amenity.setPricingType(pricingType);
        amenity.setBookingCharge(new BigDecimal(bookingCharge));
        amenity.setRefundableDeposit(BigDecimal.ZERO);
        amenity.setSlotMinutes(60);
        amenity.setAdvanceBookingDays(30);
        amenity.setCancellationHours(4);
        amenity.setBookingApprovalRequired(approvalRequired);
        amenity.setActive(active);
        amenity.setCreatedAt(OffsetDateTime.now());
        amenity.setUpdatedAt(OffsetDateTime.now());
        return amenity;
    }

    private AmenityBooking booking(String status, String paymentStatus, UUID owner) {
        AmenityBooking booking = new AmenityBooking();
        booking.setSocietyId(societyId);
        booking.setAmenityId(amenityId);
        booking.setBookedBy(owner);
        booking.setBookingNumber("AMB-TEST-1");
        booking.setStartsAt(OffsetDateTime.now().plusDays(2));
        booking.setEndsAt(OffsetDateTime.now().plusDays(2).plusHours(1));
        booking.setAttendees(2);
        booking.setAmount(new BigDecimal("500"));
        booking.setBaseCharge(new BigDecimal("500"));
        booking.setDepositAmount(BigDecimal.ZERO);
        booking.setStatus(status);
        booking.setPaymentStatus(paymentStatus);
        booking.setCreatedAt(OffsetDateTime.now());
        booking.setUpdatedAt(OffsetDateTime.now());
        return booking;
    }

    private void stubBookableAmenity(Amenity amenity) {
        when(amenities.findByIdAndSocietyId(amenityId, societyId)).thenReturn(Optional.of(amenity));
        when(amenities.findById(amenityId)).thenReturn(Optional.of(amenity));
        when(bookings.existsByAmenityIdAndStatusInAndStartsAtLessThanAndEndsAtGreaterThan(
                        eq(amenityId), anyCollection(), any(), any()))
                .thenReturn(false);
    }

    private void stubBooking(AmenityBooking booking, Amenity amenity) {
        when(bookings.findByIdAndSocietyId(any(), eq(societyId))).thenReturn(Optional.of(booking));
        when(amenities.findByIdAndSocietyId(amenityId, societyId)).thenReturn(Optional.of(amenity));
        when(amenities.findById(amenityId)).thenReturn(Optional.of(amenity));
    }

    private OffsetDateTime futureAt(int hour, int minute) {
        return OffsetDateTime.now(ZoneOffset.ofHoursMinutes(5, 30))
                .plusDays(2)
                .withHour(hour)
                .withMinute(minute)
                .withSecond(0)
                .withNano(0);
    }

    private static final class LegacyAmenityFacade {
        private final AmenityService delegate;

        private LegacyAmenityFacade(AmenityService delegate) {
            this.delegate = delegate;
        }

        List<Map<String, Object>> list(UUID societyId, String role) {
            return delegate.list(societyId, UserRole.from(role)).stream()
                    .map(this::amenity)
                    .toList();
        }

        List<Map<String, Object>> bookings(UUID societyId, UUID userId, String role) {
            return delegate.bookings(societyId, actor(userId, role)).stream()
                    .map(this::booking)
                    .toList();
        }

        Map<String, Object> create(
                UUID societyId,
                String name,
                String type,
                String code,
                String description,
                String location,
                Integer capacity,
                BigDecimal hourly,
                String pricing,
                BigDecimal bookingCharge,
                BigDecimal deposit,
                LocalTime opens,
                LocalTime closes,
                boolean approval,
                int slot,
                int advance,
                int cancellation,
                String instructions) {
            return amenity(
                    delegate.create(
                            societyId,
                            command(
                                    name,
                                    type,
                                    code,
                                    description,
                                    location,
                                    capacity,
                                    hourly,
                                    pricing,
                                    bookingCharge,
                                    deposit,
                                    opens,
                                    closes,
                                    approval,
                                    slot,
                                    advance,
                                    cancellation,
                                    instructions,
                                    true)));
        }

        Map<String, Object> update(
                UUID societyId,
                UUID id,
                String name,
                String type,
                String code,
                String description,
                String location,
                Integer capacity,
                BigDecimal hourly,
                String pricing,
                BigDecimal bookingCharge,
                BigDecimal deposit,
                LocalTime opens,
                LocalTime closes,
                boolean approval,
                int slot,
                int advance,
                int cancellation,
                String instructions,
                boolean active) {
            return amenity(
                    delegate.update(
                            societyId,
                            id,
                            command(
                                    name,
                                    type,
                                    code,
                                    description,
                                    location,
                                    capacity,
                                    hourly,
                                    pricing,
                                    bookingCharge,
                                    deposit,
                                    opens,
                                    closes,
                                    approval,
                                    slot,
                                    advance,
                                    cancellation,
                                    instructions,
                                    active)));
        }

        Map<String, Object> book(
                UUID societyId,
                UUID userId,
                UUID amenityId,
                OffsetDateTime start,
                OffsetDateTime end,
                int attendees,
                String purpose) {
            return booking(
                    delegate.book(
                            societyId,
                            userId,
                            amenityId,
                            new AmenityModels.BookingCommand(start, end, attendees, purpose)));
        }

        Map<String, Object> checkout(
                UUID societyId, UUID actorId, String role, UUID bookingId, String method) {
            return booking(
                    delegate.checkout(
                            societyId,
                            actor(actorId, role),
                            bookingId,
                            PaymentMethod.from(method)));
        }

        Map<String, Object> recordCash(UUID societyId, UUID actorId, String role, UUID bookingId) {
            return booking(delegate.recordCash(societyId, actor(actorId, role), bookingId));
        }

        Map<String, Object> action(
                UUID societyId,
                UUID actorId,
                String role,
                UUID bookingId,
                String action,
                String reason) {
            return booking(
                    delegate.action(
                            societyId,
                            actor(actorId, role),
                            bookingId,
                            new AmenityModels.ActionCommand(
                                    AmenityBookingAction.from(action), reason)));
        }

        private AmenityModels.Actor actor(UUID id, String role) {
            return new AmenityModels.Actor(id, UserRole.from(role));
        }

        private AmenityModels.AmenityCommand command(
                String name,
                String type,
                String code,
                String description,
                String location,
                Integer capacity,
                BigDecimal hourly,
                String pricing,
                BigDecimal bookingCharge,
                BigDecimal deposit,
                LocalTime opens,
                LocalTime closes,
                boolean approval,
                int slot,
                int advance,
                int cancellation,
                String instructions,
                boolean active) {
            return new AmenityModels.AmenityCommand(
                    name,
                    AmenityType.from(type),
                    code,
                    description,
                    location,
                    capacity,
                    hourly,
                    AmenityPricingType.from(pricing),
                    bookingCharge,
                    deposit,
                    opens,
                    closes,
                    approval,
                    slot,
                    advance,
                    cancellation,
                    instructions,
                    active);
        }

        private Map<String, Object> amenity(AmenityModels.AmenityView value) {
            var result = new LinkedHashMap<String, Object>();
            result.put("name", value.name());
            result.put("amenity_code", value.amenityCode());
            result.put("pricing_type", value.pricingType().code());
            result.put("booking_charge", value.bookingCharge());
            result.put("refundable_deposit", value.refundableDeposit());
            return result;
        }

        private Map<String, Object> booking(AmenityModels.BookingView value) {
            var result = new LinkedHashMap<String, Object>();
            result.put("base_charge", value.baseCharge());
            result.put("deposit_amount", value.depositAmount());
            result.put("amount", value.amount());
            result.put("status", value.status().code());
            result.put("payment_status", value.paymentStatus().code());
            result.put(
                    "payment_method",
                    value.paymentMethod() == null
                            ? null
                            : value.paymentReference() != null
                                            && value.paymentReference()
                                                    .startsWith("pay_amenity_test_")
                                    ? value.paymentMethod().razorpayCode()
                                    : value.paymentMethod().code());
            result.put("payment_reference", value.paymentReference());
            result.put("rejection_reason", value.rejectionReason());
            return result;
        }
    }
}
