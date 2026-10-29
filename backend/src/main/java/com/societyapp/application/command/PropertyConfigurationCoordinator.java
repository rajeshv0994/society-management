package com.societyapp.application.command;

import com.societyapp.application.exception.*;
import com.societyapp.application.policy.StaffPermissionCatalog;
import com.societyapp.application.port.in.StructureAccessUseCase;
import com.societyapp.application.port.out.*;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.time.OffsetDateTime;
import java.util.*;

public final class PropertyConfigurationCoordinator {
    private final ParkingSlotRepository parking;
    private final SocietyRepository societies;
    private final AuditEventRepository audits;
    private final StructureAccessUseCase structure;
    private final JsonCodec json;

    public PropertyConfigurationCoordinator(
            ParkingSlotRepository parking,
            SocietyRepository societies,
            AuditEventRepository audits,
            StructureAccessUseCase structure,
            JsonCodec json) {
        this.parking = parking;
        this.societies = societies;
        this.audits = audits;
        this.structure = structure;
        this.json = json;
    }

    public ParkingSlot add(UUID societyId, UUID actorId, String number, ParkingSlotType requested) {
        requireModule(societyId);
        number = required(number, "Parking slot number").toUpperCase(Locale.ROOT);
        if (parking.existsBySocietyIdAndSlotNumberIgnoreCase(societyId, number))
            throw new ConflictException("Parking slot number already exists");
        var now = OffsetDateTime.now();
        var slot = new ParkingSlot();
        slot.setSocietyId(societyId);
        slot.setSlotNumber(number);
        slot.setSlotType(requested.code());
        slot.setStatus(ParkingSlotStatus.AVAILABLE.code());
        slot.setCreatedAt(now);
        slot.setUpdatedAt(now);
        slot = parking.save(slot);
        audit(societyId, actorId, "CONFIGURATION_CREATED", "PARKING_SLOT", slot.getId(), Map.of());
        return slot;
    }

    public ParkingSlot update(
            UUID societyId, UUID actorId, UUID id, String number, ParkingSlotType requested) {
        requireModule(societyId);
        var slot = requireParking(societyId, id);
        number = required(number, "Parking slot number").toUpperCase(Locale.ROOT);
        if (parking.existsBySocietyIdAndSlotNumberIgnoreCaseAndIdNot(societyId, number, id))
            throw new ConflictException("Parking slot number already exists");
        slot.setSlotNumber(number);
        slot.setSlotType(requested.code());
        slot.setUpdatedAt(OffsetDateTime.now());
        slot = parking.save(slot);
        audit(societyId, actorId, "CONFIGURATION_UPDATED", "PARKING_SLOT", id, Map.of());
        return slot;
    }

    public ParkingSlot action(
            UUID societyId,
            UUID actorId,
            UUID id,
            ParkingAction requested,
            UUID flatId,
            String vehicleNumber) {
        requireModule(societyId);
        var slot = requireParking(societyId, id);
        switch (requested) {
            case ALLOCATE -> {
                if (flatId == null) throw new IllegalArgumentException("Space is required");
                structure.requireFlat(flatId, societyId);
                vehicleNumber = required(vehicleNumber, "Vehicle number").toUpperCase(Locale.ROOT);
                slot.setFlatId(flatId);
                slot.setVehicleNumber(vehicleNumber);
                slot.setStatus(ParkingSlotStatus.ALLOCATED.code());
            }
            case RELEASE, ACTIVATE -> clear(slot, ParkingSlotStatus.AVAILABLE);
            case RESERVE -> clear(slot, ParkingSlotStatus.RESERVED);
            case MAINTENANCE -> clear(slot, ParkingSlotStatus.MAINTENANCE);
            case DEACTIVATE -> clear(slot, ParkingSlotStatus.INACTIVE);
        }
        slot.setUpdatedAt(OffsetDateTime.now());
        slot = parking.save(slot);
        audit(
                societyId,
                actorId,
                "PARKING_" + requested.code(),
                "PARKING_SLOT",
                id,
                Map.of("status", slot.getStatus()));
        return slot;
    }

    private void clear(ParkingSlot slot, ParkingSlotStatus status) {
        slot.setFlatId(null);
        slot.setVehicleNumber(null);
        slot.setStatus(status.code());
    }

    private ParkingSlot requireParking(UUID society, UUID id) {
        return parking.findByIdAndSocietyId(id, society)
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found"));
    }

    private void requireModule(UUID id) {
        var society =
                societies
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
        if (!StaffPermissionCatalog.moduleEnabled(
                society.getEnabledModules(), BuiltInServiceCode.PARKING.code()))
            throw new IllegalStateException("PARKING module is not enabled");
    }

    private String required(String value, String label) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(label + " is required");
        return value.trim();
    }

    private void audit(
            UUID society, UUID actor, String action, String type, UUID id, Map<String, ?> details) {
        var event = new AuditEvent();
        event.setSocietyId(society);
        event.setActorUserId(actor);
        event.setAction(action);
        event.setEntityType(type);
        event.setEntityId(id == null ? null : id.toString());
        try {
            event.setDetails(json.write(details));
        } catch (JsonCodecException e) {
            event.setDetails("{}");
        }
        event.setOccurredAt(OffsetDateTime.now());
        audits.save(event);
    }
}
