package com.societyapp.application.mapper;

import com.societyapp.application.model.resident.ResidentModels;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ResidentViewMapper {
    private final FlatRepository flats;
    private final FloorRepository floors;
    private final BlockRepository blocks;
    private final AmenityRepository amenities;

    public ResidentViewMapper(
            FlatRepository flats,
            FloorRepository floors,
            BlockRepository blocks,
            AmenityRepository amenities) {
        this.flats = flats;
        this.floors = floors;
        this.blocks = blocks;
        this.amenities = amenities;
    }

    public List<ResidentModels.SpaceOption> availableSpaces(UUID societyId) {
        var result = new ArrayList<ResidentModels.SpaceOption>();
        for (var block : blocks.findBySocietyIdOrderByName(societyId)) {
            for (var floor : floors.findByBlockIdOrderByFloorNumber(block.getId())) {
                for (var flat : flats.findByFloorIdOrderByFlatNumber(floor.getId())) {
                    result.add(
                            new ResidentModels.SpaceOption(
                                    flat.getId(),
                                    block.getName(),
                                    floor.getName(),
                                    flat.getFlatNumber(),
                                    FlatOccupancyStatus.from(flat.getOccupancyStatus())));
                }
            }
        }
        return result;
    }

    public ResidentModels.RegistrationView registration(ResidentRegistration value) {
        var flat = flats.findById(value.getFlatId()).orElse(null);
        return new ResidentModels.RegistrationView(
                value.getId(),
                value.getSocietyId(),
                value.getFlatId(),
                ResidentType.from(value.getResidentType()),
                value.getFullName(),
                value.getMobile(),
                value.getEmail(),
                value.getDob(),
                value.getGender(),
                value.getEmergencyContact(),
                value.getMoveInDate(),
                value.getMoveOutDate(),
                RegistrationStatus.from(value.getStatus()),
                value.getRejectionReason(),
                value.isInvited(),
                value.getInvitationExpiresAt(),
                value.getSubmittedAt(),
                value.getReviewedBy(),
                value.getReviewedAt(),
                flat == null ? null : flat.getFlatNumber(),
                flat == null ? null : block(flat));
    }

    public ResidentModels.Summary resident(UserAccount user) {
        var flat =
                flats.findAll().stream()
                        .filter(
                                item ->
                                        user.getId().equals(item.getActiveOwnerId())
                                                || user.getId().equals(item.getActiveTenantId()))
                        .findFirst()
                        .orElse(null);
        return new ResidentModels.Summary(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getMobile(),
                AccountStatus.from(user.getStatus()),
                flat == null ? null : flat.getFlatNumber(),
                flat == null
                        ? null
                        : user.getId().equals(flat.getActiveOwnerId())
                                ? ResidentType.OWNER
                                : ResidentType.TENANT,
                flat == null ? null : block(flat));
    }

    public ResidentModels.Space space(Flat flat) {
        var floor = floors.findById(flat.getFloorId()).orElse(null);
        return new ResidentModels.Space(
                flat.getId(),
                flat.getFlatNumber(),
                FlatOccupancyStatus.from(flat.getOccupancyStatus()),
                flat.getAreaSqft(),
                floor == null ? null : floor.getName(),
                floor == null
                        ? null
                        : blocks.findById(floor.getBlockId()).map(Block::getName).orElse(null));
    }

    public ResidentModels.Household household(HouseholdMember value) {
        return new ResidentModels.Household(
                value.getId(),
                value.getFullName(),
                value.getRelationship(),
                value.getMobile(),
                value.getDateOfBirth());
    }

    public ResidentModels.Complaint complaint(Complaint value) {
        return new ResidentModels.Complaint(
                value.getId(),
                value.getTitle(),
                value.getDescription(),
                WorkPriority.from(value.getPriority()),
                ComplaintStatus.from(value.getStatus()),
                value.getResolution(),
                value.getCreatedAt());
    }

    public ResidentModels.Invoice invoice(MaintenanceInvoice value) {
        return new ResidentModels.Invoice(
                value.getId(),
                value.getInvoiceNumber(),
                value.getBillingMonth(),
                value.getDueDate(),
                value.getSubtotal(),
                value.getLateFee(),
                value.getPaidAmount(),
                PaymentStatus.from(value.getStatus()));
    }

    public ResidentModels.Payment payment(Payment value) {
        return new ResidentModels.Payment(
                value.getId(),
                value.getInvoiceId(),
                value.getAmount(),
                PaymentMethod.from(value.getPaymentMethod()),
                value.getProviderReference(),
                PaymentStatus.from(value.getStatus()),
                value.getPaidAt());
    }

    public ResidentModels.Visitor visitor(Visitor value) {
        return new ResidentModels.Visitor(
                value.getId(),
                value.getVisitorName(),
                value.getMobile(),
                value.getPurpose(),
                value.getVehicleNumber(),
                value.getExpectedAt(),
                VisitorStatus.from(value.getStatus()));
    }

    public ResidentModels.Booking booking(AmenityBooking value) {
        return new ResidentModels.Booking(
                value.getId(),
                value.getAmenityId(),
                amenities.findById(value.getAmenityId()).map(Amenity::getName).orElse("Amenity"),
                value.getStartsAt(),
                value.getEndsAt(),
                value.getAmount(),
                AmenityBookingStatus.from(value.getStatus()));
    }

    private String block(Flat flat) {
        return floors.findById(flat.getFloorId())
                .flatMap(floor -> blocks.findById(floor.getBlockId()))
                .map(Block::getName)
                .orElse(null);
    }
}
