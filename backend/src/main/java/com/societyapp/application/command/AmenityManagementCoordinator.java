package com.societyapp.application.command;

import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.model.amenity.AmenityModels;
import com.societyapp.application.port.out.persistence.AmenityRepository;
import com.societyapp.application.query.AmenityQueryCoordinator;
import com.societyapp.domain.model.Amenity;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class AmenityManagementCoordinator {
    private final AmenityRepository amenities;
    private final AmenityQueryCoordinator views;

    public AmenityManagementCoordinator(
            AmenityRepository amenities, AmenityQueryCoordinator views) {
        this.amenities = amenities;
        this.views = views;
    }

    public AmenityModels.AmenityView create(UUID societyId, AmenityModels.AmenityCommand command) {
        var amenity = new Amenity();
        amenity.setSocietyId(societyId);
        amenity.setCreatedAt(OffsetDateTime.now());
        apply(amenity, command);
        return views.amenity(amenities.save(amenity));
    }

    public AmenityModels.AmenityView update(
            UUID societyId, UUID amenityId, AmenityModels.AmenityCommand command) {
        var amenity =
                amenities
                        .findByIdAndSocietyId(amenityId, societyId)
                        .orElseThrow(() -> new ResourceNotFoundException("Amenity not found"));
        apply(amenity, command);
        return views.amenity(amenities.save(amenity));
    }

    private void apply(Amenity amenity, AmenityModels.AmenityCommand command) {
        if (command.opensAt() != null
                && command.closesAt() != null
                && !command.closesAt().isAfter(command.opensAt())) {
            throw new IllegalArgumentException("Closing time must be after opening time");
        }
        amenity.setName(command.name().trim());
        amenity.setAmenityType(command.amenityType().code());
        amenity.setAmenityCode(command.amenityCode().trim().toUpperCase(Locale.ROOT));
        amenity.setDescription(blank(command.description()));
        amenity.setLocation(blank(command.location()));
        amenity.setCapacity(command.capacity());
        amenity.setHourlyCharge(
                command.hourlyCharge() == null ? BigDecimal.ZERO : command.hourlyCharge());
        amenity.setPricingType(command.pricingType().code());
        amenity.setBookingCharge(
                command.bookingCharge() == null ? BigDecimal.ZERO : command.bookingCharge());
        amenity.setRefundableDeposit(
                command.refundableDeposit() == null
                        ? BigDecimal.ZERO
                        : command.refundableDeposit());
        amenity.setOpensAt(command.opensAt());
        amenity.setClosesAt(command.closesAt());
        amenity.setBookingApprovalRequired(command.bookingApprovalRequired());
        amenity.setSlotMinutes(command.slotMinutes());
        amenity.setAdvanceBookingDays(command.advanceBookingDays());
        amenity.setCancellationHours(command.cancellationHours());
        amenity.setInstructions(blank(command.instructions()));
        amenity.setActive(command.active());
        amenity.setUpdatedAt(OffsetDateTime.now());
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
