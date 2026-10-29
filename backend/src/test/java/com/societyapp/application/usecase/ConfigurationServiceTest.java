package com.societyapp.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.societyapp.application.exception.ConflictException;
import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.model.configuration.ConfigurationModels;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.service.ConfigurationService;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import com.societyapp.infrastructure.serialization.JacksonJsonCodec;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ConfigurationServiceTest {
    private final ParkingSlotRepository parking = mock(ParkingSlotRepository.class);
    private final VendorRepository vendors = mock(VendorRepository.class);
    private final ComplaintCategoryRepository categories = mock(ComplaintCategoryRepository.class);
    private final MaintenanceChargeRuleRepository charges =
            mock(MaintenanceChargeRuleRepository.class);
    private final StaffPermissionRepository permissions = mock(StaffPermissionRepository.class);
    private final UserAccountJpaRepository users = mock(UserAccountJpaRepository.class);
    private final BlockRepository blocks = mock(BlockRepository.class);
    private final FloorRepository floors = mock(FloorRepository.class);
    private final FlatRepository flats = mock(FlatRepository.class);
    private final SocietyRepository societies = mock(SocietyRepository.class);
    private final AuditEventRepository audits = mock(AuditEventRepository.class);
    private final StructureAccessService structure = mock(StructureAccessService.class);
    private final com.societyapp.application.port.out.JsonCodec json =
            new JacksonJsonCodec(new ObjectMapper());
    private final LegacyConfigurationFacade service =
            new LegacyConfigurationFacade(
                    new ConfigurationService(
                            parking,
                            vendors,
                            categories,
                            charges,
                            permissions,
                            users,
                            blocks,
                            floors,
                            flats,
                            societies,
                            audits,
                            structure,
                            json));

    private final UUID societyId = UUID.randomUUID();
    private final UUID otherSocietyId = UUID.randomUUID();
    private final UUID adminId = UUID.randomUUID();
    private final UUID staffId = UUID.randomUUID();
    private final UUID itemId = UUID.randomUUID();
    private final UUID flatId = UUID.randomUUID();
    private Society society;

    @BeforeEach
    void defaults() {
        society = new Society();
        society.setEnabledModules(
                "[\"RESIDENTS\",\"STAFF\",\"PARKING\",\"VENDORS\",\"COMPLAINTS\",\"BILLING\",\"PAYMENTS\",\"VISITORS\",\"NOTICES\",\"AMENITIES\",\"DELIVERIES\",\"EMERGENCY_INCIDENTS\",\"ACTIVITIES_GALLERIES\"]");
        when(societies.findById(societyId)).thenReturn(Optional.of(society));
        when(societies.findById(otherSocietyId)).thenReturn(Optional.empty());
        when(parking.save(any(ParkingSlot.class))).thenAnswer(call -> call.getArgument(0));
        when(vendors.save(any(Vendor.class))).thenAnswer(call -> call.getArgument(0));
        when(categories.save(any(ComplaintCategory.class))).thenAnswer(call -> call.getArgument(0));
        when(charges.save(any(MaintenanceChargeRule.class)))
                .thenAnswer(call -> call.getArgument(0));
        when(audits.save(any(AuditEvent.class))).thenAnswer(call -> call.getArgument(0));
        when(parking.findBySocietyIdOrderBySlotNumber(societyId)).thenReturn(List.of());
        when(vendors.findBySocietyIdOrderByName(societyId)).thenReturn(List.of());
        when(categories.findBySocietyIdOrderByName(societyId)).thenReturn(List.of());
        when(charges.findBySocietyIdOrderByName(societyId)).thenReturn(List.of());
        when(blocks.findBySocietyIdOrderByName(societyId)).thenReturn(List.of());
    }

    @Test
    void workspaceReturnsAllConfigurationAreasSummaryReadinessAndPermissionCatalog() {
        var slot = slot("AVAILABLE");
        var vendor = vendor("ACTIVE");
        var category = category(true);
        var charge = charge(true, LocalDate.now().minusDays(1), null);
        when(parking.findBySocietyIdOrderBySlotNumber(societyId)).thenReturn(List.of(slot));
        when(vendors.findBySocietyIdOrderByName(societyId)).thenReturn(List.of(vendor));
        when(categories.findBySocietyIdOrderByName(societyId)).thenReturn(List.of(category));
        when(charges.findBySocietyIdOrderByName(societyId)).thenReturn(List.of(charge));

        var result = service.get(societyId);

        assertThat(result.keySet())
                .contains(
                        "parking",
                        "vendors",
                        "complaintCategories",
                        "chargeRules",
                        "summary",
                        "setup",
                        "staff_permission_options");
        @SuppressWarnings("unchecked")
        var summary = (Map<String, Object>) result.get("summary");
        assertThat(summary)
                .containsEntry("parking_available", 1L)
                .containsEntry("active_vendors", 1L)
                .containsEntry("active_categories", 1L)
                .containsEntry("active_charge_rules", 1L);
        assertThat((List<?>) result.get("staff_permission_options")).hasSize(10);
    }

    @Test
    void createsNormalizedParkingAndRejectsCaseInsensitiveDuplicate() {
        var created = service.addParking(societyId, adminId, " p-01 ", "CAR");
        assertThat(created)
                .containsEntry("slot_number", "P-01")
                .containsEntry("status", "AVAILABLE");
        verify(audits).save(any(AuditEvent.class));

        when(parking.existsBySocietyIdAndSlotNumberIgnoreCase(societyId, "P-01")).thenReturn(true);
        assertThatThrownBy(() -> service.addParking(societyId, adminId, "p-01", "CAR"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void parkingSupportsAllocationReleaseMaintenanceAndTenantIsolation() {
        var slot = slot("AVAILABLE");
        when(parking.findByIdAndSocietyId(itemId, societyId)).thenReturn(Optional.of(slot));

        var allocated =
                service.parkingAction(
                        societyId, adminId, itemId, "ALLOCATE", flatId, "mh 12 ab 1234");
        assertThat(allocated)
                .containsEntry("status", "ALLOCATED")
                .containsEntry("flat_id", flatId.toString())
                .containsEntry("vehicle_number", "MH 12 AB 1234");

        var released = service.parkingAction(societyId, adminId, itemId, "RELEASE", null, null);
        assertThat(released).containsEntry("status", "AVAILABLE").containsEntry("flat_id", null);

        var maintenance =
                service.parkingAction(societyId, adminId, itemId, "MAINTENANCE", null, null);
        assertThat(maintenance).containsEntry("status", "MAINTENANCE");

        when(parking.findByIdAndSocietyId(itemId, otherSocietyId)).thenReturn(Optional.empty());
        assertThatThrownBy(
                        () ->
                                service.parkingAction(
                                        otherSocietyId, adminId, itemId, "RELEASE", null, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void parkingAllocationRequiresAValidSpaceAndVehicle() {
        var slot = slot("AVAILABLE");
        when(parking.findByIdAndSocietyId(itemId, societyId)).thenReturn(Optional.of(slot));

        assertThatThrownBy(
                        () ->
                                service.parkingAction(
                                        societyId, adminId, itemId, "ALLOCATE", null, "MH12"))
                .hasMessageContaining("Space");
        assertThatThrownBy(
                        () ->
                                service.parkingAction(
                                        societyId, adminId, itemId, "ALLOCATE", flatId, " "))
                .hasMessageContaining("Vehicle");
    }

    @Test
    void vendorSupportsCompleteCreateUpdateAndStatusLifecycle() {
        var created =
                service.addVendor(
                        societyId,
                        adminId,
                        " Lift Care ",
                        " Elevator ",
                        " Priya ",
                        "+91 90000 00000",
                        "HELP@EXAMPLE.COM",
                        "27abcde1234f1z5");
        assertThat(created)
                .containsEntry("name", "Lift Care")
                .containsEntry("mobile", "+91 90000 00000")
                .containsEntry("email", "help@example.com")
                .containsEntry("gst_number", "27ABCDE1234F1Z5");

        var row = vendor("ACTIVE");
        when(vendors.findByIdAndSocietyId(itemId, societyId)).thenReturn(Optional.of(row));
        var updated =
                service.updateVendor(
                        societyId,
                        adminId,
                        itemId,
                        "Lift Care 2",
                        "Elevator",
                        "Asha",
                        "9000000001",
                        "asha@example.com",
                        null);
        assertThat(updated).containsEntry("name", "Lift Care 2");
        assertThat(service.setVendorStatus(societyId, adminId, itemId, "INACTIVE"))
                .containsEntry("status", "INACTIVE");
    }

    @Test
    void duplicateVendorCombinationIsRejected() {
        when(vendors.existsBySocietyIdAndNameIgnoreCaseAndServiceTypeIgnoreCase(
                        societyId, "Lift Care", "Elevator"))
                .thenReturn(true);
        assertThatThrownBy(
                        () ->
                                service.addVendor(
                                        societyId,
                                        adminId,
                                        "Lift Care",
                                        "Elevator",
                                        null,
                                        "9000000000",
                                        null,
                                        null))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void complaintCategorySupportsSlaEditAndDeactivate() {
        var created = service.addCategory(societyId, adminId, " Plumbing ", 24);
        assertThat(created).containsEntry("name", "Plumbing").containsEntry("active", true);

        var row = category(true);
        when(categories.findByIdAndSocietyId(itemId, societyId)).thenReturn(Optional.of(row));
        assertThat(service.updateCategory(societyId, adminId, itemId, "Urgent plumbing", 6))
                .containsEntry("sla_hours", 6);
        assertThat(service.setCategoryStatus(societyId, adminId, itemId, false))
                .containsEntry("active", false);
    }

    @Test
    void categoryRejectsDuplicateAndInvalidSla() {
        when(categories.existsBySocietyIdAndNameIgnoreCase(societyId, "Plumbing")).thenReturn(true);
        assertThatThrownBy(() -> service.addCategory(societyId, adminId, "Plumbing", 24))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> service.addCategory(societyId, adminId, "Other", 0))
                .hasMessageContaining("SLA");
    }

    @Test
    void chargeRuleSupportsAllFieldsEditAndLifecycle() {
        var from = LocalDate.now();
        var to = from.plusYears(1);
        var created =
                service.addCharge(
                        societyId,
                        adminId,
                        "Area charge",
                        "PER_SQFT",
                        new BigDecimal("2.50"),
                        "MONTHLY",
                        from,
                        to);
        assertThat(created)
                .containsEntry("calculation_type", "PER_SQFT")
                .containsEntry("active", true);
        assertThat(created.get("effective_to").toString())
                .contains(
                        String.valueOf(to.getYear()),
                        String.valueOf(to.getMonthValue()),
                        String.valueOf(to.getDayOfMonth()));

        var row = charge(true, from, to);
        when(charges.findByIdAndSocietyId(itemId, societyId)).thenReturn(Optional.of(row));
        assertThat(
                        service.updateCharge(
                                societyId,
                                adminId,
                                itemId,
                                "Area charge",
                                "FIXED",
                                new BigDecimal("500"),
                                "QUARTERLY",
                                from,
                                null))
                .containsEntry("frequency", "QUARTERLY");
        assertThat(service.setChargeStatus(societyId, adminId, itemId, false))
                .containsEntry("active", false);
    }

    @Test
    void chargeRejectsInvalidPercentageDateRangeAndDuplicateEffectiveVersion() {
        var from = LocalDate.now();
        assertThatThrownBy(
                        () ->
                                service.addCharge(
                                        societyId,
                                        adminId,
                                        "Penalty",
                                        "PERCENTAGE",
                                        new BigDecimal("101"),
                                        "MONTHLY",
                                        from,
                                        null))
                .hasMessageContaining("100");
        assertThatThrownBy(
                        () ->
                                service.addCharge(
                                        societyId,
                                        adminId,
                                        "Charge",
                                        "FIXED",
                                        BigDecimal.TEN,
                                        "MONTHLY",
                                        from,
                                        from.minusDays(1)))
                .hasMessageContaining("end date");
        when(charges.existsBySocietyIdAndNameIgnoreCaseAndEffectiveFrom(societyId, "Charge", from))
                .thenReturn(true);
        assertThatThrownBy(
                        () ->
                                service.addCharge(
                                        societyId,
                                        adminId,
                                        "Charge",
                                        "FIXED",
                                        BigDecimal.TEN,
                                        "MONTHLY",
                                        from,
                                        null))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void referenceReturnsOnlyActiveAndCurrentlyEffectiveConfiguration() {
        var available = slot("AVAILABLE");
        var inactiveSlot = slot("INACTIVE");
        var activeVendor = vendor("ACTIVE");
        var inactiveVendor = vendor("INACTIVE");
        var activeCategory = category(true);
        var inactiveCategory = category(false);
        var current = charge(true, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        var future = charge(true, LocalDate.now().plusDays(1), null);
        when(parking.findBySocietyIdOrderBySlotNumber(societyId))
                .thenReturn(List.of(available, inactiveSlot));
        when(vendors.findBySocietyIdOrderByName(societyId))
                .thenReturn(List.of(activeVendor, inactiveVendor));
        when(categories.findBySocietyIdOrderByName(societyId))
                .thenReturn(List.of(activeCategory, inactiveCategory));
        when(charges.findBySocietyIdOrderByName(societyId)).thenReturn(List.of(current, future));

        var result = service.reference(societyId, staffId, "STAFF");

        assertThat((List<?>) result.get("parking")).hasSize(1);
        assertThat((List<?>) result.get("vendors")).hasSize(1);
        assertThat((List<?>) result.get("complaint_categories")).hasSize(1);
        assertThat((List<?>) result.get("charge_rules")).hasSize(1);
    }

    @Test
    void permissionsRejectUnknownOrDisabledCodesAndReplaceValidSet() {
        var staff = new UserAccount();
        staff.setSocietyId(societyId);
        staff.setRole("STAFF");
        when(users.findById(staffId)).thenReturn(Optional.of(staff));

        assertThatThrownBy(
                        () ->
                                service.setPermissions(
                                        societyId, adminId, staffId, Set.of("UNKNOWN")))
                .hasMessageContaining("Unknown or disabled");

        society.setEnabledModules("[\"STAFF\"]");
        assertThatThrownBy(
                        () ->
                                service.setPermissions(
                                        societyId, adminId, staffId, Set.of("VISITORS")))
                .hasMessageContaining("disabled");

        var result = service.setPermissions(societyId, adminId, staffId, Set.of("TASKS"));
        assertThat(result).containsEntry("permissions", Set.of("TASKS"));
        verify(permissions, atLeastOnce()).deleteByStaffUserId(staffId);
        verify(permissions).save(any(StaffPermission.class));
    }

    @Test
    void disabledConfigurationModuleRejectsMutation() {
        society.setEnabledModules("[\"STAFF\"]");
        assertThatThrownBy(() -> service.addParking(societyId, adminId, "P1", "CAR"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not enabled");
    }

    @Test
    void setupCompletionRequiresStructureAndThenPersistsCompletion() {
        assertThatThrownBy(() -> service.complete(societyId, adminId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("property group");

        var block = new Block();
        block.setSocietyId(societyId);
        var floor = new Floor();
        var flat = new Flat();
        when(blocks.findBySocietyIdOrderByName(societyId)).thenReturn(List.of(block));
        when(floors.findByBlockIdOrderByFloorNumber(nullable(UUID.class)))
                .thenReturn(List.of(floor));
        when(flats.findByFloorIdOrderByFlatNumber(nullable(UUID.class))).thenReturn(List.of(flat));

        var result = service.complete(societyId, adminId);

        assertThat(result).containsEntry("setup_completed", true);
        assertThat(society.isSetupCompleted()).isTrue();
        verify(societies).save(society);
    }

    private ParkingSlot slot(String status) {
        var row = new ParkingSlot();
        row.setSocietyId(societyId);
        row.setSlotNumber("P-01");
        row.setSlotType("CAR");
        row.setStatus(status);
        row.setCreatedAt(OffsetDateTime.now());
        row.setUpdatedAt(OffsetDateTime.now());
        return row;
    }

    private Vendor vendor(String status) {
        var row = new Vendor();
        row.setSocietyId(societyId);
        row.setName("Lift Care");
        row.setServiceType("Elevator");
        row.setMobile("9000000000");
        row.setStatus(status);
        row.setCreatedAt(OffsetDateTime.now());
        row.setUpdatedAt(OffsetDateTime.now());
        return row;
    }

    private ComplaintCategory category(boolean active) {
        var row = new ComplaintCategory();
        row.setSocietyId(societyId);
        row.setName("Plumbing");
        row.setSlaHours(24);
        row.setActive(active);
        row.setCreatedAt(OffsetDateTime.now());
        row.setUpdatedAt(OffsetDateTime.now());
        return row;
    }

    private MaintenanceChargeRule charge(
            boolean active, LocalDate effectiveFrom, LocalDate effectiveTo) {
        var row = new MaintenanceChargeRule();
        row.setSocietyId(societyId);
        row.setName("Maintenance");
        row.setCalculationType("FIXED");
        row.setAmount(BigDecimal.TEN);
        row.setFrequency("MONTHLY");
        row.setEffectiveFrom(effectiveFrom);
        row.setEffectiveTo(effectiveTo);
        row.setActive(active);
        row.setCreatedAt(OffsetDateTime.now());
        row.setUpdatedAt(OffsetDateTime.now());
        return row;
    }

    private static final class LegacyConfigurationFacade {
        private final ConfigurationService delegate;

        private LegacyConfigurationFacade(ConfigurationService delegate) {
            this.delegate = delegate;
        }

        Map<String, Object> get(UUID societyId) {
            var value = delegate.get(societyId);
            var result = new LinkedHashMap<String, Object>();
            result.put("parking", value.parking());
            result.put("vendors", value.vendors());
            result.put("complaintCategories", value.complaintCategories());
            result.put("chargeRules", value.chargeRules());
            result.put("summary", summary(value.summary()));
            result.put("setup", value.setup());
            result.put("staff_permission_options", value.staffPermissionOptions());
            return result;
        }

        Map<String, Object> reference(UUID societyId, UUID userId, String role) {
            var value = delegate.reference(societyId, userId, UserRole.from(role));
            var result = new LinkedHashMap<String, Object>();
            result.put("parking", value.parking());
            result.put("vendors", value.vendors());
            result.put("complaint_categories", value.complaintCategories());
            result.put("charge_rules", value.chargeRules());
            return result;
        }

        Map<String, Object> addParking(UUID society, UUID actor, String number, String type) {
            return parking(
                    delegate.addParking(
                            society,
                            actor,
                            new ConfigurationModels.ParkingCommand(
                                    number, ParkingSlotType.from(type))));
        }

        Map<String, Object> updateParking(
                UUID society, UUID actor, UUID id, String number, String type) {
            return parking(
                    delegate.updateParking(
                            society,
                            actor,
                            id,
                            new ConfigurationModels.ParkingCommand(
                                    number, ParkingSlotType.from(type))));
        }

        Map<String, Object> parkingAction(
                UUID society, UUID actor, UUID id, String action, UUID flatId, String vehicle) {
            return parking(
                    delegate.parkingAction(
                            society,
                            actor,
                            id,
                            new ConfigurationModels.ParkingActionCommand(
                                    ParkingAction.from(action), flatId, vehicle)));
        }

        Map<String, Object> addVendor(
                UUID society,
                UUID actor,
                String name,
                String type,
                String contact,
                String mobile,
                String email,
                String gst) {
            return vendor(
                    delegate.addVendor(
                            society,
                            actor,
                            new ConfigurationModels.VendorCommand(
                                    name, type, contact, mobile, email, gst)));
        }

        Map<String, Object> updateVendor(
                UUID society,
                UUID actor,
                UUID id,
                String name,
                String type,
                String contact,
                String mobile,
                String email,
                String gst) {
            return vendor(
                    delegate.updateVendor(
                            society,
                            actor,
                            id,
                            new ConfigurationModels.VendorCommand(
                                    name, type, contact, mobile, email, gst)));
        }

        Map<String, Object> setVendorStatus(UUID society, UUID actor, UUID id, String status) {
            return vendor(delegate.setVendorStatus(society, actor, id, VendorStatus.from(status)));
        }

        Map<String, Object> addCategory(UUID society, UUID actor, String name, int sla) {
            return category(
                    delegate.addCategory(
                            society, actor, new ConfigurationModels.CategoryCommand(name, sla)));
        }

        Map<String, Object> updateCategory(
                UUID society, UUID actor, UUID id, String name, int sla) {
            return category(
                    delegate.updateCategory(
                            society,
                            actor,
                            id,
                            new ConfigurationModels.CategoryCommand(name, sla)));
        }

        Map<String, Object> setCategoryStatus(UUID society, UUID actor, UUID id, boolean active) {
            return category(delegate.setCategoryStatus(society, actor, id, active));
        }

        Map<String, Object> addCharge(
                UUID society,
                UUID actor,
                String name,
                String type,
                BigDecimal amount,
                String frequency,
                LocalDate from,
                LocalDate to) {
            return charge(
                    delegate.addCharge(
                            society,
                            actor,
                            chargeCommand(name, type, amount, frequency, from, to)));
        }

        Map<String, Object> updateCharge(
                UUID society,
                UUID actor,
                UUID id,
                String name,
                String type,
                BigDecimal amount,
                String frequency,
                LocalDate from,
                LocalDate to) {
            return charge(
                    delegate.updateCharge(
                            society,
                            actor,
                            id,
                            chargeCommand(name, type, amount, frequency, from, to)));
        }

        Map<String, Object> setChargeStatus(UUID society, UUID actor, UUID id, boolean active) {
            return charge(delegate.setChargeStatus(society, actor, id, active));
        }

        Map<String, Object> setPermissions(
                UUID society, UUID actor, UUID staff, Set<String> codes) {
            if (codes.stream()
                    .anyMatch(
                            code ->
                                    Arrays.stream(StaffPermissionCode.values())
                                            .noneMatch(value -> value.code().equals(code))))
                throw new IllegalArgumentException("Unknown or disabled staff permissions");
            var typed =
                    codes.stream()
                            .map(StaffPermissionCode::from)
                            .collect(java.util.stream.Collectors.toUnmodifiableSet());
            var value = delegate.setPermissions(society, actor, staff, typed);
            return Map.of("staffId", value.staffId(), "permissions", codes);
        }

        Map<String, Object> complete(UUID society, UUID actor) {
            var value = delegate.complete(society, actor);
            return Map.of("setup_completed", value.setupCompleted(), "setup", value.setup());
        }

        private ConfigurationModels.ChargeCommand chargeCommand(
                String name,
                String type,
                BigDecimal amount,
                String frequency,
                LocalDate from,
                LocalDate to) {
            return new ConfigurationModels.ChargeCommand(
                    name,
                    ChargeCalculationType.from(type),
                    amount,
                    ChargeFrequency.from(frequency),
                    from,
                    to);
        }

        private Map<String, Object> summary(ConfigurationModels.Summary value) {
            return Map.of(
                    "parking_available",
                    value.parkingAvailable(),
                    "active_vendors",
                    value.activeVendors(),
                    "active_categories",
                    value.activeCategories(),
                    "active_charge_rules",
                    value.activeChargeRules());
        }

        private Map<String, Object> parking(ConfigurationModels.ParkingView value) {
            var result = new LinkedHashMap<String, Object>();
            result.put("slot_number", value.slotNumber());
            result.put("status", value.status().code());
            result.put("flat_id", value.spaceId() == null ? null : value.spaceId().toString());
            result.put("vehicle_number", value.vehicleNumber());
            return result;
        }

        private Map<String, Object> vendor(ConfigurationModels.VendorView value) {
            var result = new LinkedHashMap<String, Object>();
            result.put("name", value.name());
            result.put("mobile", value.mobile());
            result.put("email", value.email());
            result.put("gst_number", value.gstNumber());
            result.put("status", value.status().code());
            return result;
        }

        private Map<String, Object> category(ConfigurationModels.CategoryView value) {
            return Map.of(
                    "name", value.name(), "sla_hours", value.slaHours(), "active", value.active());
        }

        private Map<String, Object> charge(ConfigurationModels.ChargeView value) {
            var result = new LinkedHashMap<String, Object>();
            result.put("calculation_type", value.calculationType().code());
            result.put("frequency", value.frequency().code());
            result.put("active", value.active());
            result.put("effective_to", value.effectiveTo());
            return result;
        }
    }
}
