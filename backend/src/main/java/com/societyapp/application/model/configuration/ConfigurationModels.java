package com.societyapp.application.model.configuration;

import com.societyapp.domain.type.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public final class ConfigurationModels {
    private ConfigurationModels() {}

    public record ParkingCommand(String slotNumber, ParkingSlotType slotType) {}

    public record ParkingActionCommand(ParkingAction action, UUID spaceId, String vehicleNumber) {}

    public record VendorCommand(
            String name,
            String serviceType,
            String contactPerson,
            String mobile,
            String email,
            String gstNumber) {}

    public record CategoryCommand(String name, int slaHours) {}

    public record ChargeCommand(
            String name,
            ChargeCalculationType calculationType,
            BigDecimal amount,
            ChargeFrequency frequency,
            LocalDate effectiveFrom,
            LocalDate effectiveTo) {}

    public record ParkingView(
            UUID id,
            UUID societyId,
            String slotNumber,
            ParkingSlotType slotType,
            ParkingSlotStatus status,
            UUID spaceId,
            String vehicleNumber,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {}

    public record VendorView(
            UUID id,
            UUID societyId,
            String name,
            String serviceType,
            String contactPerson,
            String mobile,
            String email,
            String gstNumber,
            VendorStatus status,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {}

    public record CategoryView(
            UUID id,
            UUID societyId,
            String name,
            int slaHours,
            boolean active,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {}

    public record ChargeView(
            UUID id,
            UUID societyId,
            String name,
            ChargeCalculationType calculationType,
            BigDecimal amount,
            ChargeFrequency frequency,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            boolean active,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {}

    public record SpaceOption(UUID id, String label, String spaceNumber, String occupancyStatus) {}

    public record Summary(
            int parkingTotal,
            long parkingAvailable,
            long parkingAllocated,
            long activeVendors,
            long activeCategories,
            long activeChargeRules) {}

    public record SetupCheck(String code, String label, boolean complete, boolean required) {}

    public record Setup(
            boolean ready,
            boolean setupCompleted,
            int propertyGroups,
            long spaces,
            List<SetupCheck> checklist) {}

    public record PermissionOption(
            StaffPermissionCode code, String label, BuiltInServiceCode requiredModule) {}

    public record ConfigurationView(
            List<ParkingView> parking,
            List<VendorView> vendors,
            List<CategoryView> complaintCategories,
            List<ChargeView> chargeRules,
            List<SpaceOption> spaces,
            Summary summary,
            Setup setup,
            List<String> enabledModules,
            List<PermissionOption> staffPermissionOptions) {}

    public record ReferenceView(
            List<ParkingView> parking,
            List<VendorView> vendors,
            List<CategoryView> complaintCategories,
            List<ChargeView> chargeRules,
            List<String> enabledModules,
            UserRole viewerRole,
            UUID viewerId) {}

    public record PermissionsView(UUID staffId, Set<StaffPermissionCode> permissions) {}

    public record Completion(boolean setupCompleted, Setup setup) {}
}
