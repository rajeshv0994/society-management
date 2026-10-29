package com.societyapp.controller;

import com.societyapp.application.model.amenity.AmenityModels;
import com.societyapp.application.port.in.AmenityUseCase;
import com.societyapp.controller.mapper.AmenityHttpMapper;
import com.societyapp.controller.model.AmenityResponse;
import com.societyapp.domain.type.*;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/amenities")
public class AmenityController {
    private final AmenityUseCase service;
    private final AmenityHttpMapper mapper = new AmenityHttpMapper();

    public AmenityController(AmenityUseCase service) {
        this.service = service;
    }

    public record AmenityRequest(
            @NotBlank String name,
            @NotBlank
                    @Pattern(
                            regexp =
                                    "COMMUNITY_CENTER|GYM|SWIMMING_POOL|SPORTS_COURT|GUEST_ROOM|PLAY_AREA|OTHER")
                    String amenityType,
            @NotBlank @Pattern(regexp = "[A-Z0-9][A-Z0-9_-]{1,39}") String amenityCode,
            String description,
            String location,
            @Min(1) Integer capacity,
            @NotNull @DecimalMin("0") BigDecimal hourlyCharge,
            @NotBlank @Pattern(regexp = "FREE|HOURLY|PER_SLOT|FIXED") String pricingType,
            @NotNull @DecimalMin("0") BigDecimal bookingCharge,
            @NotNull @DecimalMin("0") BigDecimal refundableDeposit,
            LocalTime opensAt,
            LocalTime closesAt,
            boolean bookingApprovalRequired,
            @Min(15) @Max(1440) int slotMinutes,
            @Min(1) @Max(365) int advanceBookingDays,
            @Min(0) @Max(168) int cancellationHours,
            @Size(max = 500) String instructions,
            Boolean active) {}

    public record Book(
            @NotNull OffsetDateTime startsAt,
            @NotNull OffsetDateTime endsAt,
            @Min(1) int attendees,
            @Size(max = 240) String purpose) {}

    public record Checkout(
            @NotBlank @Pattern(regexp = "UPI|CARD|NET_BANKING") String paymentMethod) {}

    public record BookingAction(
            @NotBlank @Pattern(regexp = "APPROVE|REJECT|CANCEL|CHECK_IN|CHECK_OUT|REFUND")
                    String action,
            @Size(max = 300) String reason) {}

    @GetMapping
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    List<AmenityResponse.Amenity> list() {
        return service.list(CurrentUser.societyId(), UserRole.from(CurrentUser.role())).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/bookings")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    List<AmenityResponse.Booking> bookings() {
        return service.bookings(CurrentUser.societyId(), actor()).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    AmenityResponse.Amenity create(@Valid @RequestBody AmenityRequest request) {
        return mapper.toResponse(service.create(CurrentUser.societyId(), amenity(request, true)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    AmenityResponse.Amenity update(
            @PathVariable UUID id, @Valid @RequestBody AmenityRequest request) {
        return mapper.toResponse(
                service.update(
                        CurrentUser.societyId(),
                        id,
                        amenity(request, request.active() == null || request.active())));
    }

    @PostMapping("/{id}/book")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','RESIDENT')")
    AmenityResponse.Booking book(@PathVariable UUID id, @Valid @RequestBody Book request) {
        return mapper.toResponse(
                service.book(
                        CurrentUser.societyId(),
                        CurrentUser.id(),
                        id,
                        new AmenityModels.BookingCommand(
                                request.startsAt(),
                                request.endsAt(),
                                request.attendees(),
                                request.purpose())));
    }

    @PostMapping("/bookings/{id}/checkout")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    AmenityResponse.Booking checkout(@PathVariable UUID id, @Valid @RequestBody Checkout request) {
        return mapper.toResponse(
                service.checkout(
                        CurrentUser.societyId(),
                        actor(),
                        id,
                        PaymentMethod.from(request.paymentMethod())));
    }

    @PostMapping("/bookings/{id}/cash")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF')")
    AmenityResponse.Booking cash(@PathVariable UUID id) {
        return mapper.toResponse(service.recordCash(CurrentUser.societyId(), actor(), id));
    }

    @PatchMapping("/bookings/{id}")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    AmenityResponse.Booking action(
            @PathVariable UUID id, @Valid @RequestBody BookingAction request) {
        return mapper.toResponse(
                service.action(
                        CurrentUser.societyId(),
                        actor(),
                        id,
                        new AmenityModels.ActionCommand(
                                AmenityBookingAction.from(request.action()), request.reason())));
    }

    private AmenityModels.Actor actor() {
        return new AmenityModels.Actor(CurrentUser.id(), UserRole.from(CurrentUser.role()));
    }

    private AmenityModels.AmenityCommand amenity(AmenityRequest request, boolean active) {
        return new AmenityModels.AmenityCommand(
                request.name(),
                AmenityType.from(request.amenityType()),
                request.amenityCode(),
                request.description(),
                request.location(),
                request.capacity(),
                request.hourlyCharge(),
                AmenityPricingType.from(request.pricingType()),
                request.bookingCharge(),
                request.refundableDeposit(),
                request.opensAt(),
                request.closesAt(),
                request.bookingApprovalRequired(),
                request.slotMinutes(),
                request.advanceBookingDays(),
                request.cancellationHours(),
                request.instructions(),
                active);
    }
}
