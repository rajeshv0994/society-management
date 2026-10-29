package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class RequirementResponse {
    private RequirementResponse() {}

    public record Submitted(UUID requirementId, String status, String message) {}

    public record Status(UUID id, String status) {}

    public record PlanResult(UUID customPlanId, UUID societyId, String status) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Requirement(
            UUID id,
            @JsonProperty("society_id") UUID societyId,
            @JsonProperty("society_name") String societyName,
            @JsonProperty("contact_person") String contactPerson,
            String email,
            String mobile,
            String city,
            @JsonProperty("estimated_flats") Integer estimatedFlats,
            @JsonProperty("estimated_units") Integer estimatedUnits,
            @JsonProperty("estimated_spaces") Integer estimatedSpaces,
            @JsonProperty("estimated_staff") Integer estimatedStaff,
            @JsonProperty("requested_modules") List<String> requestedModules,
            @JsonProperty("requirement_details") String requirementDetails,
            @JsonProperty("budget_monthly") BigDecimal budgetMonthly,
            String status,
            @JsonProperty("admin_notes") String adminNotes,
            @JsonProperty("assigned_to") UUID assignedTo,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("updated_at") OffsetDateTime updatedAt,
            @JsonProperty("closed_at") OffsetDateTime closedAt,
            @JsonProperty("linked_society_name") String linkedSocietyName,
            @JsonProperty("assigned_to_name") String assignedToName) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Plan(
            UUID id,
            @JsonProperty("society_id") UUID societyId,
            @JsonProperty("requirement_id") UUID requirementId,
            @JsonProperty("plan_name") String planName,
            String description,
            @JsonProperty("enabled_modules") List<String> enabledModules,
            @JsonProperty("max_flats") Integer maxFlats,
            @JsonProperty("max_units") Integer maxUnits,
            @JsonProperty("max_spaces") Integer maxSpaces,
            @JsonProperty("max_staff") Integer maxStaff,
            @JsonProperty("max_storage_gb") Integer maxStorageGb,
            @JsonProperty("price_monthly") BigDecimal priceMonthly,
            @JsonProperty("price_per_space") BigDecimal pricePerSpace,
            @JsonProperty("price_per_staff") BigDecimal pricePerStaff,
            @JsonProperty("setup_fee") BigDecimal setupFee,
            @JsonProperty("billing_cycle") String billingCycle,
            @JsonProperty("valid_from") LocalDate validFrom,
            @JsonProperty("valid_until") LocalDate validUntil,
            String status,
            @JsonProperty("created_by") UUID createdBy,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("activated_at") OffsetDateTime activatedAt,
            @JsonProperty("cancelled_at") OffsetDateTime cancelledAt,
            @JsonProperty("cancelled_by") UUID cancelledBy,
            @JsonProperty("cancellation_reason") String cancellationReason,
            @JsonProperty("society_name") String societyName,
            @JsonProperty("requirement_details") String requirementDetails) {}
}
