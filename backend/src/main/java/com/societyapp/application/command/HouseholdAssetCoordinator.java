package com.societyapp.application.command;

import com.societyapp.application.exception.AuthorizationException;
import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.mapper.HouseholdViewMapper;
import com.societyapp.application.model.household.HouseholdModels;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.time.OffsetDateTime;
import java.util.*;

public final class HouseholdAssetCoordinator {
    private final UserAccountJpaRepository users;
    private final HouseholdVehicleRepository vehicles;
    private final HouseholdEmergencyContactRepository contacts;
    private final StaffPermissionRepository permissions;
    private final HouseholdViewMapper views;

    public HouseholdAssetCoordinator(
            UserAccountJpaRepository users,
            HouseholdVehicleRepository vehicles,
            HouseholdEmergencyContactRepository contacts,
            StaffPermissionRepository permissions,
            HouseholdViewMapper views) {
        this.users = users;
        this.vehicles = vehicles;
        this.contacts = contacts;
        this.permissions = permissions;
        this.views = views;
    }

    public HouseholdModels.Vehicle addVehicle(
            HouseholdModels.Actor actor, HouseholdModels.VehicleCommand command) {
        UUID residentId = target(actor);
        var item = new HouseholdVehicle();
        item.setSocietyId(actor.societyId());
        item.setPrimaryResidentId(residentId);
        item.setCreatedAt(OffsetDateTime.now());
        applyVehicle(item, command);
        item.setVerificationStatus(VerificationStatus.PENDING.code());
        item.setActive(true);
        return views.vehicle(vehicles.save(item));
    }

    public HouseholdModels.Vehicle updateVehicle(
            HouseholdModels.Actor actor, UUID vehicleId, HouseholdModels.VehicleCommand command) {
        UUID residentId = target(actor);
        var item = requireVehicle(actor.societyId(), residentId, vehicleId);
        applyVehicle(item, command);
        if (actor.role() == UserRole.RESIDENT) {
            item.setVerificationStatus(VerificationStatus.PENDING.code());
            item.setVerificationNote(null);
            item.setReviewedBy(null);
            item.setReviewedAt(null);
        }
        return views.vehicle(vehicles.save(item));
    }

    public HouseholdModels.Vehicle reviewVehicle(
            HouseholdModels.Actor actor, UUID vehicleId, HouseholdModels.ReviewCommand command) {
        requireManager(actor.actorId(), actor.role());
        requireResident(actor.societyId(), actor.residentId());
        var item = requireVehicle(actor.societyId(), actor.residentId(), vehicleId);
        var requested = command.status();
        if (requested == VerificationStatus.PENDING)
            throw new IllegalArgumentException("Review status must be VERIFIED or REJECTED");
        if (requested == VerificationStatus.REJECTED
                && (command.note() == null || command.note().isBlank()))
            throw new IllegalArgumentException("Rejection note is required");
        item.setVerificationStatus(requested.code());
        item.setVerificationNote(blank(command.note()));
        item.setReviewedBy(actor.actorId());
        item.setReviewedAt(OffsetDateTime.now());
        item.setUpdatedAt(OffsetDateTime.now());
        return views.vehicle(vehicles.save(item));
    }

    public HouseholdModels.EntityResult removeVehicle(HouseholdModels.Actor actor, UUID vehicleId) {
        UUID residentId = target(actor);
        var item = requireVehicle(actor.societyId(), residentId, vehicleId);
        item.setActive(false);
        item.setUpdatedAt(OffsetDateTime.now());
        vehicles.save(item);
        return new HouseholdModels.EntityResult(vehicleId, false);
    }

    public HouseholdModels.Contact addContact(
            HouseholdModels.Actor actor, HouseholdModels.ContactCommand command) {
        UUID residentId = target(actor);
        if (contacts.findBySocietyIdAndPrimaryResidentIdAndActiveTrueOrderByPriority(
                                actor.societyId(), residentId)
                        .size()
                >= 3)
            throw new IllegalStateException("A household can have up to 3 emergency contacts");
        var item = new HouseholdEmergencyContact();
        item.setSocietyId(actor.societyId());
        item.setPrimaryResidentId(residentId);
        item.setCreatedAt(OffsetDateTime.now());
        item.setActive(true);
        applyContact(item, command);
        return views.contact(contacts.save(item));
    }

    public HouseholdModels.Contact updateContact(
            HouseholdModels.Actor actor, UUID contactId, HouseholdModels.ContactCommand command) {
        UUID residentId = target(actor);
        var item = requireContact(actor.societyId(), residentId, contactId);
        applyContact(item, command);
        return views.contact(contacts.save(item));
    }

    public HouseholdModels.EntityResult removeContact(HouseholdModels.Actor actor, UUID contactId) {
        UUID residentId = target(actor);
        var item = requireContact(actor.societyId(), residentId, contactId);
        item.setActive(false);
        item.setUpdatedAt(OffsetDateTime.now());
        contacts.save(item);
        return new HouseholdModels.EntityResult(contactId, false);
    }

    private void applyVehicle(HouseholdVehicle item, HouseholdModels.VehicleCommand command) {
        item.setVehicleType(command.vehicleType().code());
        item.setRegistrationNumber(
                command.registrationNumber().replaceAll("\\s+", "").toUpperCase(Locale.ROOT));
        item.setMake(blank(command.make()));
        item.setModel(blank(command.model()));
        item.setColor(blank(command.color()));
        item.setParkingSlot(blank(command.parkingSlot()));
        item.setStickerNumber(blank(command.stickerNumber()));
        item.setUpdatedAt(OffsetDateTime.now());
    }

    private void applyContact(
            HouseholdEmergencyContact item, HouseholdModels.ContactCommand command) {
        item.setFullName(command.fullName().trim());
        item.setRelationship(command.relationship().trim().toUpperCase(Locale.ROOT));
        item.setMobile(command.mobile().trim());
        item.setAlternateMobile(blank(command.alternateMobile()));
        item.setPriority(command.priority());
        item.setUpdatedAt(OffsetDateTime.now());
    }

    private UUID target(HouseholdModels.Actor actor) {
        if (actor.role() == UserRole.RESIDENT) {
            if (actor.residentId() != null && !actor.actorId().equals(actor.residentId()))
                throw new AuthorizationException("You can only manage your own household");
            requireResident(actor.societyId(), actor.actorId());
            return actor.actorId();
        }
        requireManager(actor.actorId(), actor.role());
        if (actor.residentId() == null) throw new IllegalArgumentException("Resident is required");
        requireResident(actor.societyId(), actor.residentId());
        return actor.residentId();
    }

    private void requireManager(UUID actorId, UserRole role) {
        if (role == UserRole.SOCIETY_ADMIN) return;
        if (role == UserRole.STAFF
                && permissions.existsByStaffUserIdAndPermissionCode(
                        actorId, StaffPermissionCode.HOUSEHOLDS.code())) return;
        throw new AuthorizationException("Household management permission is required");
    }

    private UserAccount requireResident(UUID societyId, UUID residentId) {
        return users.findById(residentId)
                .filter(user -> societyId.equals(user.getSocietyId()))
                .filter(user -> UserRole.RESIDENT.matches(user.getRole()))
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found"));
    }

    private HouseholdVehicle requireVehicle(UUID societyId, UUID residentId, UUID id) {
        return vehicles.findByIdAndSocietyIdAndPrimaryResidentId(id, societyId, residentId)
                .filter(HouseholdVehicle::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Household vehicle not found"));
    }

    private HouseholdEmergencyContact requireContact(UUID societyId, UUID residentId, UUID id) {
        return contacts.findByIdAndSocietyIdAndPrimaryResidentId(id, societyId, residentId)
                .filter(HouseholdEmergencyContact::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency contact not found"));
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
