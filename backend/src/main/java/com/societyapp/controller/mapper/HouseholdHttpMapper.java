package com.societyapp.controller.mapper;

import com.societyapp.application.model.household.HouseholdModels;
import com.societyapp.controller.model.HouseholdResponse;

public final class HouseholdHttpMapper {
    public HouseholdResponse.Household toResponse(HouseholdModels.Household value) {
        var spaces = value.spaces().stream().map(this::space).toList();
        var user = user(value.resident());
        return new HouseholdResponse.Household(
                user,
                user,
                spaces,
                spaces,
                spaces,
                value.household().stream().map(this::toResponse).toList(),
                value.vehicles().stream().map(this::toResponse).toList(),
                value.emergencyContacts().stream().map(this::toResponse).toList());
    }

    public HouseholdResponse.Workspace toResponse(HouseholdModels.Workspace value) {
        return new HouseholdResponse.Workspace(
                value.households().stream().map(this::toResponse).toList(),
                value.residentCount(),
                value.memberCount(),
                value.vehicleCount(),
                value.pendingVerification());
    }

    public HouseholdResponse.Member toResponse(HouseholdModels.Member value) {
        return new HouseholdResponse.Member(
                value.id(),
                value.memberCode(),
                value.residentId(),
                value.fullName(),
                value.relationship(),
                value.mobile(),
                value.email(),
                value.dateOfBirth(),
                code(value.gender()),
                value.bloodGroup(),
                value.occupation(),
                code(value.identityType()),
                value.identityLast4(),
                value.emergencyContactName(),
                value.emergencyContactMobile(),
                value.accessEnabled(),
                value.verificationStatus().code(),
                value.verificationNote(),
                value.active());
    }

    public HouseholdResponse.Vehicle toResponse(HouseholdModels.Vehicle value) {
        return new HouseholdResponse.Vehicle(
                value.id(),
                value.residentId(),
                value.vehicleType().code(),
                value.registrationNumber(),
                value.make(),
                value.model(),
                value.color(),
                value.parkingSlot(),
                value.stickerNumber(),
                value.verificationStatus().code(),
                value.verificationNote(),
                value.active());
    }

    public HouseholdResponse.Contact toResponse(HouseholdModels.Contact value) {
        return new HouseholdResponse.Contact(
                value.id(),
                value.residentId(),
                value.fullName(),
                value.relationship(),
                value.mobile(),
                value.alternateMobile(),
                value.priority(),
                value.active());
    }

    public HouseholdResponse.EntityResult toResponse(HouseholdModels.EntityResult value) {
        return new HouseholdResponse.EntityResult(value.id(), value.active());
    }

    private HouseholdResponse.User user(HouseholdModels.User value) {
        return new HouseholdResponse.User(
                value.id(),
                value.fullName(),
                value.email(),
                value.mobile(),
                value.emailVerified(),
                value.mobileVerified(),
                value.status().code());
    }

    private HouseholdResponse.Space space(HouseholdModels.Space value) {
        return new HouseholdResponse.Space(
                value.id(),
                value.spaceNumber(),
                value.spaceNumber(),
                value.spaceNumber(),
                value.occupancyStatus(),
                value.areaSqft(),
                value.residentType().code(),
                value.section(),
                value.section(),
                value.block());
    }

    private String code(Enum<?> value) {
        return value == null ? null : value.name();
    }
}
