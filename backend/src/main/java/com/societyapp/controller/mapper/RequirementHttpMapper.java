package com.societyapp.controller.mapper;

import com.societyapp.application.model.requirement.RequirementModels;
import com.societyapp.controller.model.RequirementResponse;

public final class RequirementHttpMapper {
    public RequirementResponse.Submitted toResponse(RequirementModels.Submitted value) {
        return new RequirementResponse.Submitted(
                value.requirementId(), value.status().code(), value.message());
    }

    public RequirementResponse.Status toResponse(RequirementModels.Status value) {
        return new RequirementResponse.Status(value.id(), value.status().code());
    }

    public RequirementResponse.PlanResult toResponse(RequirementModels.PlanResult value) {
        return new RequirementResponse.PlanResult(
                value.customPlanId(), value.societyId(), value.status().code());
    }

    public RequirementResponse.Requirement toResponse(RequirementModels.RequirementView value) {
        return new RequirementResponse.Requirement(
                value.id(),
                value.societyId(),
                value.societyName(),
                value.contactPerson(),
                value.email(),
                value.mobile(),
                value.city(),
                value.estimatedSpaces(),
                value.estimatedSpaces(),
                value.estimatedSpaces(),
                value.estimatedStaff(),
                value.requestedModules(),
                value.requirementDetails(),
                value.budgetMonthly(),
                value.status().code(),
                value.adminNotes(),
                value.assignedTo(),
                value.createdAt(),
                value.updatedAt(),
                value.closedAt(),
                value.linkedSocietyName(),
                value.assignedToName());
    }

    public RequirementResponse.Plan toResponse(RequirementModels.PlanView value) {
        return new RequirementResponse.Plan(
                value.id(),
                value.societyId(),
                value.requirementId(),
                value.planName(),
                value.description(),
                value.enabledModules(),
                value.maxSpaces(),
                value.maxSpaces(),
                value.maxSpaces(),
                value.maxStaff(),
                value.maxStorageGb(),
                value.priceMonthly(),
                value.pricePerSpace(),
                value.pricePerStaff(),
                value.setupFee(),
                value.billingCycle().code(),
                value.validFrom(),
                value.validUntil(),
                value.status().code(),
                value.createdBy(),
                value.createdAt(),
                value.activatedAt(),
                value.cancelledAt(),
                value.cancelledBy(),
                value.cancellationReason(),
                value.societyName(),
                value.requirementDetails());
    }
}
