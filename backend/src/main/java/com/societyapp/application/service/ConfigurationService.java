package com.societyapp.application.service;

import com.societyapp.application.command.ConfigurationCatalogCoordinator;
import com.societyapp.application.command.PropertyConfigurationCoordinator;
import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.mapper.ConfigurationViewMapper;
import com.societyapp.application.model.configuration.ConfigurationModels;
import com.societyapp.application.policy.StaffPermissionCatalog;
import com.societyapp.application.port.in.ConfigurationUseCase;
import com.societyapp.application.port.in.StructureAccessUseCase;
import com.societyapp.application.port.out.JsonCodec;
import com.societyapp.application.port.out.JsonCodecException;
import com.societyapp.application.port.out.persistence.AuditEventRepository;
import com.societyapp.application.port.out.persistence.BlockRepository;
import com.societyapp.application.port.out.persistence.ComplaintCategoryRepository;
import com.societyapp.application.port.out.persistence.FlatRepository;
import com.societyapp.application.port.out.persistence.FloorRepository;
import com.societyapp.application.port.out.persistence.MaintenanceChargeRuleRepository;
import com.societyapp.application.port.out.persistence.ParkingSlotRepository;
import com.societyapp.application.port.out.persistence.SocietyRepository;
import com.societyapp.application.port.out.persistence.StaffPermissionRepository;
import com.societyapp.application.port.out.persistence.UserAccountJpaRepository;
import com.societyapp.application.port.out.persistence.VendorRepository;
import com.societyapp.application.query.ConfigurationQueryCoordinator;
import com.societyapp.application.usecase.*;
import com.societyapp.domain.model.AuditEvent;
import com.societyapp.domain.model.Society;
import com.societyapp.domain.model.StaffPermission;
import com.societyapp.domain.type.UserRole;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ConfigurationService implements ConfigurationUseCase {
    private final ParkingSlotRepository parking;
    private final VendorRepository vendors;
    private final ComplaintCategoryRepository categories;
    private final MaintenanceChargeRuleRepository charges;
    private final StaffPermissionRepository permissions;
    private final UserAccountJpaRepository users;
    private final BlockRepository blocks;
    private final FloorRepository floors;
    private final FlatRepository flats;
    private final SocietyRepository societies;
    private final AuditEventRepository audits;
    private final StructureAccessUseCase structure;
    private final JsonCodec json;
    private final ConfigurationQueryCoordinator queries;
    private final PropertyConfigurationCoordinator propertyCommands;
    private final ConfigurationCatalogCoordinator catalogCommands;
    private final ConfigurationViewMapper mapper = new ConfigurationViewMapper();

    public ConfigurationService(
            ParkingSlotRepository parking,
            VendorRepository vendors,
            ComplaintCategoryRepository categories,
            MaintenanceChargeRuleRepository charges,
            StaffPermissionRepository permissions,
            UserAccountJpaRepository users,
            BlockRepository blocks,
            FloorRepository floors,
            FlatRepository flats,
            SocietyRepository societies,
            AuditEventRepository audits,
            StructureAccessUseCase structure,
            JsonCodec json) {
        this.parking = parking;
        this.vendors = vendors;
        this.categories = categories;
        this.charges = charges;
        this.permissions = permissions;
        this.users = users;
        this.blocks = blocks;
        this.floors = floors;
        this.flats = flats;
        this.societies = societies;
        this.audits = audits;
        this.structure = structure;
        this.json = json;
        this.queries =
                new ConfigurationQueryCoordinator(
                        parking,
                        vendors,
                        categories,
                        charges,
                        blocks,
                        floors,
                        flats,
                        societies,
                        json);
        this.propertyCommands =
                new PropertyConfigurationCoordinator(parking, societies, audits, structure, json);
        this.catalogCommands =
                new ConfigurationCatalogCoordinator(
                        vendors, categories, charges, societies, audits, json);
    }

    @Override
    public ConfigurationModels.ConfigurationView get(UUID societyId) {
        return queries.get(societyId);
    }

    @Override
    public ConfigurationModels.ReferenceView reference(UUID societyId, UUID userId, UserRole role) {
        return queries.reference(societyId, userId, role);
    }

    @Override
    public ConfigurationModels.ParkingView addParking(
            UUID societyId, UUID actorId, ConfigurationModels.ParkingCommand command) {
        return mapper.parking(
                propertyCommands.add(societyId, actorId, command.slotNumber(), command.slotType()));
    }

    @Override
    public ConfigurationModels.ParkingView updateParking(
            UUID societyId, UUID actorId, UUID id, ConfigurationModels.ParkingCommand command) {
        return mapper.parking(
                propertyCommands.update(
                        societyId, actorId, id, command.slotNumber(), command.slotType()));
    }

    @Override
    public ConfigurationModels.ParkingView parkingAction(
            UUID societyId,
            UUID actorId,
            UUID id,
            ConfigurationModels.ParkingActionCommand command) {
        return mapper.parking(
                propertyCommands.action(
                        societyId,
                        actorId,
                        id,
                        command.action(),
                        command.spaceId(),
                        command.vehicleNumber()));
    }

    @Override
    public ConfigurationModels.VendorView addVendor(
            UUID societyId, UUID actorId, ConfigurationModels.VendorCommand command) {
        return mapper.vendor(
                catalogCommands.addVendor(
                        societyId,
                        actorId,
                        command.name(),
                        command.serviceType(),
                        command.contactPerson(),
                        command.mobile(),
                        command.email(),
                        command.gstNumber()));
    }

    @Override
    public ConfigurationModels.VendorView updateVendor(
            UUID societyId, UUID actorId, UUID id, ConfigurationModels.VendorCommand command) {
        return mapper.vendor(
                catalogCommands.updateVendor(
                        societyId,
                        actorId,
                        id,
                        command.name(),
                        command.serviceType(),
                        command.contactPerson(),
                        command.mobile(),
                        command.email(),
                        command.gstNumber()));
    }

    @Override
    public ConfigurationModels.VendorView setVendorStatus(
            UUID societyId, UUID actorId, UUID id, com.societyapp.domain.type.VendorStatus status) {
        return mapper.vendor(catalogCommands.vendorStatus(societyId, actorId, id, status));
    }

    @Override
    public ConfigurationModels.CategoryView addCategory(
            UUID societyId, UUID actorId, ConfigurationModels.CategoryCommand command) {
        return mapper.category(
                catalogCommands.addCategory(
                        societyId, actorId, command.name(), command.slaHours()));
    }

    @Override
    public ConfigurationModels.CategoryView updateCategory(
            UUID societyId, UUID actorId, UUID id, ConfigurationModels.CategoryCommand command) {
        return mapper.category(
                catalogCommands.updateCategory(
                        societyId, actorId, id, command.name(), command.slaHours()));
    }

    @Override
    public ConfigurationModels.CategoryView setCategoryStatus(
            UUID societyId, UUID actorId, UUID id, boolean active) {
        return mapper.category(catalogCommands.categoryStatus(societyId, actorId, id, active));
    }

    @Override
    public ConfigurationModels.ChargeView addCharge(
            UUID societyId, UUID actorId, ConfigurationModels.ChargeCommand command) {
        return mapper.charge(
                catalogCommands.addCharge(
                        societyId,
                        actorId,
                        command.name(),
                        command.calculationType(),
                        command.amount(),
                        command.frequency(),
                        command.effectiveFrom(),
                        command.effectiveTo()));
    }

    @Override
    public ConfigurationModels.ChargeView updateCharge(
            UUID societyId, UUID actorId, UUID id, ConfigurationModels.ChargeCommand command) {
        return mapper.charge(
                catalogCommands.updateCharge(
                        societyId,
                        actorId,
                        id,
                        command.name(),
                        command.calculationType(),
                        command.amount(),
                        command.frequency(),
                        command.effectiveFrom(),
                        command.effectiveTo()));
    }

    @Override
    public ConfigurationModels.ChargeView setChargeStatus(
            UUID societyId, UUID actorId, UUID id, boolean active) {
        return mapper.charge(catalogCommands.chargeStatus(societyId, actorId, id, active));
    }

    @Override
    public ConfigurationModels.PermissionsView setPermissions(
            UUID societyId,
            UUID granterId,
            UUID staffId,
            Set<com.societyapp.domain.type.StaffPermissionCode> requestedCodes) {
        Society society = this.requireSociety(societyId);
        this.users
                .findById(staffId)
                .filter(user -> societyId.equals(user.getSocietyId()))
                .filter(user -> UserRole.STAFF.matches(user.getRole()))
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found"));
        Set<String> codes =
                requestedCodes.stream()
                        .map(code -> code.code())
                        .collect(java.util.stream.Collectors.toUnmodifiableSet());
        codes = StaffPermissionCatalog.validate(codes, society.getEnabledModules());
        this.permissions.deleteByStaffUserId(staffId);
        this.permissions.flush();
        for (String code : codes) {
            StaffPermission permission = new StaffPermission();
            permission.setStaffUserId(staffId);
            permission.setPermissionCode(code);
            permission.setGrantedBy(granterId);
            permission.setGrantedAt(OffsetDateTime.now());
            this.permissions.save(permission);
        }
        this.audit(
                societyId,
                granterId,
                "STAFF_PERMISSIONS_UPDATED",
                UserRole.STAFF.code(),
                staffId,
                Map.of("permissions", codes));
        return new ConfigurationModels.PermissionsView(staffId, requestedCodes);
    }

    @Override
    public ConfigurationModels.Completion complete(UUID societyId, UUID actorId) {
        Society society = this.requireSociety(societyId);
        ConfigurationModels.Setup readiness = queries.setup(societyId);
        if (!readiness.ready()) {
            throw new IllegalStateException("At least one property group and space are required");
        }
        society.setSetupCompleted(true);
        society.setUpdatedAt(OffsetDateTime.now());
        this.societies.save(society);
        this.audit(societyId, actorId, "SOCIETY_SETUP_COMPLETED", "SOCIETY", societyId, Map.of());
        return new ConfigurationModels.Completion(true, queries.setup(societyId));
    }

    private Society requireSociety(UUID societyId) {
        return (Society)
                this.societies
                        .findById(societyId)
                        .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
    }

    private void audit(
            UUID societyId,
            UUID actorId,
            String action,
            String type,
            UUID entityId,
            Map<String, ?> details) {
        AuditEvent event = new AuditEvent();
        event.setSocietyId(societyId);
        event.setActorUserId(actorId);
        event.setAction(action);
        event.setEntityType(type);
        event.setEntityId(entityId == null ? null : entityId.toString());
        try {
            event.setDetails(this.json.write(details));
        } catch (JsonCodecException exception) {
            event.setDetails("{}");
        }
        event.setOccurredAt(OffsetDateTime.now());
        this.audits.save(event);
    }
}
