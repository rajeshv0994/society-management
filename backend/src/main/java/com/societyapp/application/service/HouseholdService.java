package com.societyapp.application.service;

import com.societyapp.application.command.HouseholdAssetCoordinator;
import com.societyapp.application.command.HouseholdMemberCoordinator;
import com.societyapp.application.mapper.HouseholdViewMapper;
import com.societyapp.application.model.household.HouseholdModels;
import com.societyapp.application.port.in.HouseholdUseCase;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.query.HouseholdQueryCoordinator;
import com.societyapp.domain.type.UserRole;
import java.util.UUID;

public class HouseholdService implements HouseholdUseCase {
    private final HouseholdMemberCoordinator memberCommands;
    private final HouseholdAssetCoordinator assetCommands;
    private final HouseholdQueryCoordinator queries;

    public HouseholdService(
            UserAccountJpaRepository users,
            HouseholdMemberRepository members,
            HouseholdVehicleRepository vehicles,
            HouseholdEmergencyContactRepository contacts,
            FlatRepository flats,
            FloorRepository floors,
            BlockRepository blocks,
            StaffPermissionRepository permissions) {
        var views = new HouseholdViewMapper(members, vehicles, contacts, flats, floors, blocks);
        this.memberCommands = new HouseholdMemberCoordinator(users, members, permissions, views);
        this.assetCommands =
                new HouseholdAssetCoordinator(users, vehicles, contacts, permissions, views);
        this.queries = new HouseholdQueryCoordinator(users, permissions, views);
    }

    @Override
    public HouseholdModels.Household profile(UUID societyId, UUID userId) {
        return queries.profile(societyId, userId);
    }

    @Override
    public HouseholdModels.Workspace workspace(UUID societyId, UUID actorId, UserRole role) {
        return queries.workspace(societyId, actorId, role);
    }

    @Override
    public HouseholdModels.Member add(
            UUID societyId, UUID residentId, HouseholdModels.MemberCommand command) {
        return addMember(actor(societyId, residentId, UserRole.RESIDENT, null), command);
    }

    @Override
    public HouseholdModels.Member addMember(
            HouseholdModels.Actor actor, HouseholdModels.MemberCommand command) {
        return memberCommands.add(actor, command);
    }

    @Override
    public HouseholdModels.Member updateMember(
            HouseholdModels.Actor actor, UUID memberId, HouseholdModels.MemberCommand command) {
        return memberCommands.update(actor, memberId, command);
    }

    @Override
    public HouseholdModels.Member reviewMember(
            HouseholdModels.Actor actor, UUID memberId, HouseholdModels.ReviewCommand command) {
        return memberCommands.review(actor, memberId, command);
    }

    @Override
    public HouseholdModels.EntityResult remove(UUID societyId, UUID residentId, UUID memberId) {
        return removeMember(actor(societyId, residentId, UserRole.RESIDENT, null), memberId);
    }

    @Override
    public HouseholdModels.EntityResult removeMember(HouseholdModels.Actor actor, UUID memberId) {
        return memberCommands.remove(actor, memberId);
    }

    @Override
    public HouseholdModels.Vehicle addVehicle(
            HouseholdModels.Actor actor, HouseholdModels.VehicleCommand command) {
        return assetCommands.addVehicle(actor, command);
    }

    @Override
    public HouseholdModels.Vehicle updateVehicle(
            HouseholdModels.Actor actor, UUID vehicleId, HouseholdModels.VehicleCommand command) {
        return assetCommands.updateVehicle(actor, vehicleId, command);
    }

    @Override
    public HouseholdModels.Vehicle reviewVehicle(
            HouseholdModels.Actor actor, UUID vehicleId, HouseholdModels.ReviewCommand command) {
        return assetCommands.reviewVehicle(actor, vehicleId, command);
    }

    @Override
    public HouseholdModels.EntityResult removeVehicle(HouseholdModels.Actor actor, UUID vehicleId) {
        return assetCommands.removeVehicle(actor, vehicleId);
    }

    @Override
    public HouseholdModels.Contact addContact(
            HouseholdModels.Actor actor, HouseholdModels.ContactCommand command) {
        return assetCommands.addContact(actor, command);
    }

    @Override
    public HouseholdModels.Contact updateContact(
            HouseholdModels.Actor actor, UUID contactId, HouseholdModels.ContactCommand command) {
        return assetCommands.updateContact(actor, contactId, command);
    }

    @Override
    public HouseholdModels.EntityResult removeContact(HouseholdModels.Actor actor, UUID contactId) {
        return assetCommands.removeContact(actor, contactId);
    }

    private HouseholdModels.Actor actor(
            UUID societyId, UUID actorId, UserRole role, UUID residentId) {
        return new HouseholdModels.Actor(societyId, actorId, role, residentId);
    }
}
