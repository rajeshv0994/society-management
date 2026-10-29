package com.societyapp.application.query;

import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.mapper.ConfigurationViewMapper;
import com.societyapp.application.model.configuration.ConfigurationModels;
import com.societyapp.application.policy.StaffPermissionCatalog;
import com.societyapp.application.port.out.*;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.time.LocalDate;
import java.util.*;

public final class ConfigurationQueryCoordinator {
    private final ParkingSlotRepository parking;
    private final VendorRepository vendors;
    private final ComplaintCategoryRepository categories;
    private final MaintenanceChargeRuleRepository charges;
    private final BlockRepository blocks;
    private final FloorRepository floors;
    private final FlatRepository flats;
    private final SocietyRepository societies;
    private final JsonCodec json;
    private final ConfigurationViewMapper mapper = new ConfigurationViewMapper();

    public ConfigurationQueryCoordinator(
            ParkingSlotRepository parking,
            VendorRepository vendors,
            ComplaintCategoryRepository categories,
            MaintenanceChargeRuleRepository charges,
            BlockRepository blocks,
            FloorRepository floors,
            FlatRepository flats,
            SocietyRepository societies,
            JsonCodec json) {
        this.parking = parking;
        this.vendors = vendors;
        this.categories = categories;
        this.charges = charges;
        this.blocks = blocks;
        this.floors = floors;
        this.flats = flats;
        this.societies = societies;
        this.json = json;
    }

    public ConfigurationModels.ConfigurationView get(UUID societyId) {
        var society = requireSociety(societyId);
        var parkingRows = parking.findBySocietyIdOrderBySlotNumber(societyId);
        var vendorRows = vendors.findBySocietyIdOrderByName(societyId);
        var categoryRows = categories.findBySocietyIdOrderByName(societyId);
        var chargeRows = charges.findBySocietyIdOrderByName(societyId);
        var options =
                StaffPermissionCatalog.definitionsFor(society.getEnabledModules())
                        .entrySet()
                        .stream()
                        .map(
                                entry ->
                                        new ConfigurationModels.PermissionOption(
                                                StaffPermissionCode.from(entry.getKey()),
                                                entry.getValue().label(),
                                                BuiltInServiceCode.from(
                                                        entry.getValue().requiredModule())))
                        .toList();
        return new ConfigurationModels.ConfigurationView(
                parkingRows.stream().map(mapper::parking).toList(),
                vendorRows.stream().map(mapper::vendor).toList(),
                categoryRows.stream().map(mapper::category).toList(),
                chargeRows.stream().map(mapper::charge).toList(),
                spaceOptions(societyId),
                summary(parkingRows, vendorRows, categoryRows, chargeRows),
                setupReadiness(societyId, society),
                enabledModules(society),
                options);
    }

    public ConfigurationModels.ReferenceView reference(UUID societyId, UUID userId, UserRole role) {
        var society = requireSociety(societyId);
        var now = LocalDate.now();
        var parkingRows =
                enabled(society, BuiltInServiceCode.PARKING)
                        ? parking.findBySocietyIdOrderBySlotNumber(societyId).stream()
                                .filter(
                                        item ->
                                                !ParkingSlotStatus.INACTIVE.matches(
                                                        item.getStatus()))
                                .map(mapper::parking)
                                .toList()
                        : List.<ConfigurationModels.ParkingView>of();
        var vendorRows =
                enabled(society, BuiltInServiceCode.VENDORS)
                        ? vendors.findBySocietyIdOrderByName(societyId).stream()
                                .filter(item -> VendorStatus.ACTIVE.matches(item.getStatus()))
                                .map(mapper::vendor)
                                .toList()
                        : List.<ConfigurationModels.VendorView>of();
        var categoryRows =
                enabled(society, BuiltInServiceCode.COMPLAINTS)
                        ? categories.findBySocietyIdOrderByName(societyId).stream()
                                .filter(ComplaintCategory::isActive)
                                .map(mapper::category)
                                .toList()
                        : List.<ConfigurationModels.CategoryView>of();
        var chargeRows =
                enabled(society, BuiltInServiceCode.BILLING)
                        ? charges.findBySocietyIdOrderByName(societyId).stream()
                                .filter(MaintenanceChargeRule::isActive)
                                .filter(item -> !item.getEffectiveFrom().isAfter(now))
                                .filter(
                                        item ->
                                                item.getEffectiveTo() == null
                                                        || !item.getEffectiveTo().isBefore(now))
                                .map(mapper::charge)
                                .toList()
                        : List.<ConfigurationModels.ChargeView>of();
        return new ConfigurationModels.ReferenceView(
                parkingRows,
                vendorRows,
                categoryRows,
                chargeRows,
                enabledModules(society),
                role,
                userId);
    }

    public ConfigurationModels.Setup setup(UUID societyId) {
        return setupReadiness(societyId, requireSociety(societyId));
    }

    private ConfigurationModels.Summary summary(
            List<ParkingSlot> p,
            List<Vendor> v,
            List<ComplaintCategory> c,
            List<MaintenanceChargeRule> r) {
        return new ConfigurationModels.Summary(
                p.size(),
                p.stream().filter(x -> ParkingSlotStatus.AVAILABLE.matches(x.getStatus())).count(),
                p.stream().filter(x -> ParkingSlotStatus.ALLOCATED.matches(x.getStatus())).count(),
                v.stream().filter(x -> VendorStatus.ACTIVE.matches(x.getStatus())).count(),
                c.stream().filter(ComplaintCategory::isActive).count(),
                r.stream().filter(MaintenanceChargeRule::isActive).count());
    }

    private ConfigurationModels.Setup setupReadiness(UUID societyId, Society society) {
        var societyBlocks = blocks.findBySocietyIdOrderByName(societyId);
        long spaces =
                societyBlocks.stream()
                        .flatMap(
                                block ->
                                        floors
                                                .findByBlockIdOrderByFloorNumber(block.getId())
                                                .stream())
                        .mapToLong(
                                floor -> flats.findByFloorIdOrderByFlatNumber(floor.getId()).size())
                        .sum();
        var list = new ArrayList<ConfigurationModels.SetupCheck>();
        list.add(
                check(
                        "PROPERTY_GROUP",
                        "At least one property group",
                        !societyBlocks.isEmpty(),
                        true));
        list.add(check("SPACE", "At least one configured space", spaces > 0, true));
        optional(
                list,
                society,
                BuiltInServiceCode.PARKING,
                "Parking slots",
                !parking.findBySocietyIdOrderBySlotNumber(societyId).isEmpty());
        optional(
                list,
                society,
                BuiltInServiceCode.VENDORS,
                "Active vendors",
                vendors.findBySocietyIdOrderByName(societyId).stream()
                        .anyMatch(x -> VendorStatus.ACTIVE.matches(x.getStatus())));
        optional(
                list,
                society,
                BuiltInServiceCode.COMPLAINTS,
                "Active complaint categories",
                categories.findBySocietyIdOrderByName(societyId).stream()
                        .anyMatch(ComplaintCategory::isActive));
        optional(
                list,
                society,
                BuiltInServiceCode.BILLING,
                "Active charge rules",
                charges.findBySocietyIdOrderByName(societyId).stream()
                        .anyMatch(MaintenanceChargeRule::isActive));
        boolean ready =
                list.stream()
                        .filter(ConfigurationModels.SetupCheck::required)
                        .allMatch(ConfigurationModels.SetupCheck::complete);
        return new ConfigurationModels.Setup(
                ready, society.isSetupCompleted(), societyBlocks.size(), spaces, List.copyOf(list));
    }

    private void optional(
            List<ConfigurationModels.SetupCheck> list,
            Society society,
            BuiltInServiceCode module,
            String label,
            boolean complete) {
        if (enabled(society, module)) list.add(check(module.code(), label, complete, false));
    }

    private ConfigurationModels.SetupCheck check(
            String code, String label, boolean complete, boolean required) {
        return new ConfigurationModels.SetupCheck(code, label, complete, required);
    }

    private boolean enabled(Society society, BuiltInServiceCode module) {
        return StaffPermissionCatalog.moduleEnabled(society.getEnabledModules(), module.code());
    }

    private List<ConfigurationModels.SpaceOption> spaceOptions(UUID societyId) {
        var result = new ArrayList<ConfigurationModels.SpaceOption>();
        for (var block : blocks.findBySocietyIdOrderByName(societyId))
            for (var floor : floors.findByBlockIdOrderByFloorNumber(block.getId()))
                for (var flat : flats.findByFloorIdOrderByFlatNumber(floor.getId()))
                    result.add(
                            new ConfigurationModels.SpaceOption(
                                    flat.getId(),
                                    block.getName()
                                            + " · "
                                            + floor.getName()
                                            + " · "
                                            + flat.getFlatNumber(),
                                    flat.getFlatNumber(),
                                    flat.getOccupancyStatus()));
        return result;
    }

    private Society requireSociety(UUID id) {
        return societies
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
    }

    private List<String> enabledModules(Society society) {
        try {
            return json.readStrings(society.getEnabledModules());
        } catch (JsonCodecException e) {
            throw new IllegalStateException("Society enabled modules are invalid", e);
        }
    }
}
