package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public final class ConfigurationResponse {
    private ConfigurationResponse() {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Parking(
            UUID id,
            @JsonProperty("society_id") UUID societyId,
            @JsonProperty("slot_number") String slotNumber,
            @JsonProperty("slot_type") String slotType,
            String status,
            @JsonProperty("flat_id") UUID flatId,
            @JsonProperty("vehicle_number") String vehicleNumber,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("updated_at") OffsetDateTime updatedAt) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Vendor(
            UUID id,
            @JsonProperty("society_id") UUID societyId,
            String name,
            @JsonProperty("service_type") String serviceType,
            @JsonProperty("contact_person") String contactPerson,
            String mobile,
            String email,
            @JsonProperty("gst_number") String gstNumber,
            String status,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("updated_at") OffsetDateTime updatedAt) {}

    public record Category(
            UUID id,
            @JsonProperty("society_id") UUID societyId,
            String name,
            @JsonProperty("sla_hours") int slaHours,
            boolean active,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("updated_at") OffsetDateTime updatedAt) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Charge(
            UUID id,
            @JsonProperty("society_id") UUID societyId,
            String name,
            @JsonProperty("calculation_type") String calculationType,
            BigDecimal amount,
            String frequency,
            @JsonProperty("effective_from") LocalDate effectiveFrom,
            @JsonProperty("effective_to") LocalDate effectiveTo,
            boolean active,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("updated_at") OffsetDateTime updatedAt) {}

    public record Space(
            UUID id,
            String label,
            @JsonProperty("space_number") String spaceNumber,
            @JsonProperty("occupancy_status") String occupancyStatus) {}

    public record Summary(
            @JsonProperty("parking_total") int parkingTotal,
            @JsonProperty("parking_available") long parkingAvailable,
            @JsonProperty("parking_allocated") long parkingAllocated,
            @JsonProperty("active_vendors") long activeVendors,
            @JsonProperty("active_categories") long activeCategories,
            @JsonProperty("active_charge_rules") long activeChargeRules) {}

    public record SetupCheck(String code, String label, boolean complete, boolean required) {}

    public record Setup(
            boolean ready,
            @JsonProperty("setup_completed") boolean setupCompleted,
            @JsonProperty("property_groups") int propertyGroups,
            long spaces,
            List<SetupCheck> checklist) {}

    public record PermissionOption(
            String code, String label, @JsonProperty("required_module") String requiredModule) {}

    public record Configuration(
            List<Parking> parking,
            List<Vendor> vendors,
            List<Category> complaintCategories,
            List<Charge> chargeRules,
            List<Space> spaces,
            Summary summary,
            Setup setup,
            @JsonProperty("enabled_modules") List<String> enabledModules,
            @JsonProperty("staff_permission_options")
                    List<PermissionOption> staffPermissionOptions) {}

    public record Reference(
            List<Parking> parking,
            List<Vendor> vendors,
            @JsonProperty("complaint_categories") List<Category> complaintCategories,
            @JsonProperty("charge_rules") List<Charge> chargeRules,
            @JsonProperty("enabled_modules") List<String> enabledModules,
            @JsonProperty("viewer_role") String viewerRole,
            @JsonProperty("viewer_id") UUID viewerId) {}

    public record Permissions(@JsonProperty("staffId") UUID staffId, Set<String> permissions) {}

    public record Completion(
            @JsonProperty("setup_completed") boolean setupCompleted, Setup setup) {}
}
