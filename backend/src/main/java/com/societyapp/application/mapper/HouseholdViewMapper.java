package com.societyapp.application.mapper;

import com.societyapp.application.model.household.HouseholdModels;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.util.UUID;

public final class HouseholdViewMapper {
    private final HouseholdMemberRepository members;
    private final HouseholdVehicleRepository vehicles;
    private final HouseholdEmergencyContactRepository contacts;
    private final FlatRepository flats;
    private final FloorRepository floors;
    private final BlockRepository blocks;

    public HouseholdViewMapper(
            HouseholdMemberRepository members,
            HouseholdVehicleRepository vehicles,
            HouseholdEmergencyContactRepository contacts,
            FlatRepository flats,
            FloorRepository floors,
            BlockRepository blocks) {
        this.members = members;
        this.vehicles = vehicles;
        this.contacts = contacts;
        this.flats = flats;
        this.floors = floors;
        this.blocks = blocks;
    }

    public HouseholdModels.Household household(UserAccount user) {
        UUID societyId = user.getSocietyId();
        UUID residentId = user.getId();
        var spaces =
                flats.findAll().stream()
                        .filter(
                                flat ->
                                        residentId.equals(flat.getActiveOwnerId())
                                                || residentId.equals(flat.getActiveTenantId()))
                        .map(flat -> flat(flat, residentId))
                        .toList();
        return new HouseholdModels.Household(
                user(user),
                spaces,
                members
                        .findBySocietyIdAndPrimaryResidentIdAndActiveTrue(societyId, residentId)
                        .stream()
                        .map(this::member)
                        .toList(),
                vehicles
                        .findBySocietyIdAndPrimaryResidentIdAndActiveTrueOrderByRegistrationNumber(
                                societyId, residentId)
                        .stream()
                        .map(this::vehicle)
                        .toList(),
                contacts
                        .findBySocietyIdAndPrimaryResidentIdAndActiveTrueOrderByPriority(
                                societyId, residentId)
                        .stream()
                        .map(this::contact)
                        .toList());
    }

    public HouseholdModels.Member member(HouseholdMember item) {
        return new HouseholdModels.Member(
                item.getId(),
                item.getMemberCode(),
                item.getPrimaryResidentId(),
                item.getFullName(),
                item.getRelationship(),
                item.getMobile(),
                item.getEmail(),
                item.getDateOfBirth(),
                value(item.getGender(), Gender::from),
                item.getBloodGroup(),
                item.getOccupation(),
                value(item.getIdentityType(), IdentityType::from),
                item.getIdentityLast4(),
                item.getEmergencyContactName(),
                item.getEmergencyContactMobile(),
                item.isAccessEnabled(),
                VerificationStatus.from(item.getVerificationStatus()),
                item.getVerificationNote(),
                item.isActive());
    }

    public HouseholdModels.Vehicle vehicle(HouseholdVehicle item) {
        return new HouseholdModels.Vehicle(
                item.getId(),
                item.getPrimaryResidentId(),
                VehicleType.from(item.getVehicleType()),
                item.getRegistrationNumber(),
                item.getMake(),
                item.getModel(),
                item.getColor(),
                item.getParkingSlot(),
                item.getStickerNumber(),
                VerificationStatus.from(item.getVerificationStatus()),
                item.getVerificationNote(),
                item.isActive());
    }

    public HouseholdModels.Contact contact(HouseholdEmergencyContact item) {
        return new HouseholdModels.Contact(
                item.getId(),
                item.getPrimaryResidentId(),
                item.getFullName(),
                item.getRelationship(),
                item.getMobile(),
                item.getAlternateMobile(),
                item.getPriority(),
                item.isActive());
    }

    private HouseholdModels.User user(UserAccount user) {
        return new HouseholdModels.User(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getMobile(),
                user.isEmailVerified(),
                user.isMobileVerified(),
                AccountStatus.from(user.getStatus()));
    }

    private HouseholdModels.Space flat(Flat flat, UUID residentId) {
        var floor = floors.findById(flat.getFloorId()).orElse(null);
        var block = floor == null ? null : blocks.findById(floor.getBlockId()).orElse(null);
        return new HouseholdModels.Space(
                flat.getId(),
                flat.getFlatNumber(),
                flat.getOccupancyStatus(),
                flat.getAreaSqft(),
                residentId.equals(flat.getActiveOwnerId())
                        ? ResidentType.OWNER
                        : ResidentType.TENANT,
                floor == null ? null : floor.getName(),
                block == null ? null : block.getName());
    }

    private <T> T value(String raw, java.util.function.Function<String, T> parser) {
        return raw == null || raw.isBlank() ? null : parser.apply(raw);
    }
}
