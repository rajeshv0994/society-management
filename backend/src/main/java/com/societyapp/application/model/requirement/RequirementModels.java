package com.societyapp.application.model.requirement;

import com.societyapp.domain.type.BillingCycle;
import com.societyapp.domain.type.CustomPlanStatus;
import com.societyapp.domain.type.RequirementStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class RequirementModels {
    private RequirementModels() {}

    public record SubmitCommand(
            UUID societyId,
            String societyName,
            String contactPerson,
            String email,
            String mobile,
            String city,
            Integer estimatedSpaces,
            Integer estimatedStaff,
            List<String> requestedModules,
            String requirementDetails,
            BigDecimal budgetMonthly) {}

    public record Submitted(UUID requirementId, RequirementStatus status, String message) {}

    public record ReviewCommand(UUID reviewerId, RequirementStatus status, String notes) {}

    public record Status(UUID id, RequirementStatus status) {}

    public record PlanCommand(
            UUID societyId,
            UUID requirementId,
            String name,
            String description,
            List<String> enabledModules,
            Integer maxSpaces,
            Integer maxStaff,
            Integer maxStorageGb,
            BigDecimal pricePerSpace,
            BigDecimal pricePerStaff,
            BigDecimal fixedMonthlyPrice,
            BigDecimal setupFee,
            BillingCycle billingCycle,
            LocalDate validFrom,
            LocalDate validUntil,
            CustomPlanStatus status) {}

    public record PlanResult(UUID customPlanId, UUID societyId, CustomPlanStatus status) {}

    public record RequirementView(
            UUID id,
            UUID societyId,
            String societyName,
            String contactPerson,
            String email,
            String mobile,
            String city,
            Integer estimatedSpaces,
            Integer estimatedStaff,
            List<String> requestedModules,
            String requirementDetails,
            BigDecimal budgetMonthly,
            RequirementStatus status,
            String adminNotes,
            UUID assignedTo,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            OffsetDateTime closedAt,
            String linkedSocietyName,
            String assignedToName) {}

    public record PlanView(
            UUID id,
            UUID societyId,
            UUID requirementId,
            String planName,
            String description,
            List<String> enabledModules,
            Integer maxSpaces,
            Integer maxStaff,
            Integer maxStorageGb,
            BigDecimal priceMonthly,
            BigDecimal pricePerSpace,
            BigDecimal pricePerStaff,
            BigDecimal setupFee,
            BillingCycle billingCycle,
            LocalDate validFrom,
            LocalDate validUntil,
            CustomPlanStatus status,
            UUID createdBy,
            OffsetDateTime createdAt,
            OffsetDateTime activatedAt,
            OffsetDateTime cancelledAt,
            UUID cancelledBy,
            String cancellationReason,
            String societyName,
            String requirementDetails) {}
}
