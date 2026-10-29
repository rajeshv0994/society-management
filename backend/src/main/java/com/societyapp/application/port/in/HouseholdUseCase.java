package com.societyapp.application.port.in;

import com.societyapp.application.model.household.HouseholdModels;
import com.societyapp.domain.type.UserRole;
import java.util.UUID;

public interface HouseholdUseCase {
    HouseholdModels.Household profile(UUID societyId, UUID userId);

    HouseholdModels.Workspace workspace(UUID societyId, UUID actorId, UserRole role);

    HouseholdModels.Member add(
            UUID societyId, UUID residentId, HouseholdModels.MemberCommand command);

    HouseholdModels.Member addMember(
            HouseholdModels.Actor actor, HouseholdModels.MemberCommand command);

    HouseholdModels.Member updateMember(
            HouseholdModels.Actor actor, UUID memberId, HouseholdModels.MemberCommand command);

    HouseholdModels.Member reviewMember(
            HouseholdModels.Actor actor, UUID memberId, HouseholdModels.ReviewCommand command);

    HouseholdModels.EntityResult remove(UUID societyId, UUID residentId, UUID memberId);

    HouseholdModels.EntityResult removeMember(HouseholdModels.Actor actor, UUID memberId);

    HouseholdModels.Vehicle addVehicle(
            HouseholdModels.Actor actor, HouseholdModels.VehicleCommand command);

    HouseholdModels.Vehicle updateVehicle(
            HouseholdModels.Actor actor, UUID vehicleId, HouseholdModels.VehicleCommand command);

    HouseholdModels.Vehicle reviewVehicle(
            HouseholdModels.Actor actor, UUID vehicleId, HouseholdModels.ReviewCommand command);

    HouseholdModels.EntityResult removeVehicle(HouseholdModels.Actor actor, UUID vehicleId);

    HouseholdModels.Contact addContact(
            HouseholdModels.Actor actor, HouseholdModels.ContactCommand command);

    HouseholdModels.Contact updateContact(
            HouseholdModels.Actor actor, UUID contactId, HouseholdModels.ContactCommand command);

    HouseholdModels.EntityResult removeContact(HouseholdModels.Actor actor, UUID contactId);
}
