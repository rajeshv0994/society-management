package com.societyapp.application.mapper;

import com.societyapp.application.model.requirement.RequirementModels;
import com.societyapp.application.port.out.JsonCodec;
import com.societyapp.application.port.out.JsonCodecException;
import com.societyapp.application.port.out.persistence.RequirementRequestRepository;
import com.societyapp.application.port.out.persistence.SocietyRepository;
import com.societyapp.application.port.out.persistence.UserAccountJpaRepository;
import com.societyapp.domain.model.*;

public class RequirementViewMapper {
    private final JsonCodec json;
    private final SocietyRepository societies;
    private final UserAccountJpaRepository users;
    private final RequirementRequestRepository requirements;

    public RequirementViewMapper(
            JsonCodec json,
            SocietyRepository societies,
            UserAccountJpaRepository users,
            RequirementRequestRepository requirements) {
        this.json = json;
        this.societies = societies;
        this.users = users;
        this.requirements = requirements;
    }

    public RequirementModels.RequirementView requirement(RequirementRequest request) {
        return new RequirementModels.RequirementView(
                request.getId(),
                request.getSocietyId(),
                request.getSocietyName(),
                request.getContactPerson(),
                request.getEmail(),
                request.getMobile(),
                request.getCity(),
                request.getEstimatedFlats(),
                request.getEstimatedStaff(),
                strings(request.getRequestedModules()),
                request.getRequirementDetails(),
                request.getBudgetMonthly(),
                com.societyapp.domain.type.RequirementStatus.from(request.getStatus()),
                request.getAdminNotes(),
                request.getAssignedTo(),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                request.getClosedAt(),
                request.getSocietyId() == null
                        ? null
                        : societies
                                .findById(request.getSocietyId())
                                .map(Society::getName)
                                .orElse(null),
                request.getAssignedTo() == null
                        ? null
                        : users.findById(request.getAssignedTo())
                                .map(UserAccount::getFullName)
                                .orElse(null));
    }

    public RequirementModels.PlanView plan(SocietyCustomPlan plan) {
        return new RequirementModels.PlanView(
                plan.getId(),
                plan.getSocietyId(),
                plan.getRequirementId(),
                plan.getPlanName(),
                plan.getDescription(),
                strings(plan.getEnabledModules()),
                plan.getMaxFlats(),
                plan.getMaxStaff(),
                plan.getMaxStorageGb(),
                plan.getPriceMonthly(),
                plan.getPricePerSpace(),
                plan.getPricePerStaff(),
                plan.getSetupFee(),
                com.societyapp.domain.type.BillingCycle.from(plan.getBillingCycle()),
                plan.getValidFrom(),
                plan.getValidUntil(),
                com.societyapp.domain.type.CustomPlanStatus.from(plan.getStatus()),
                plan.getCreatedBy(),
                plan.getCreatedAt(),
                plan.getActivatedAt(),
                plan.getCancelledAt(),
                plan.getCancelledBy(),
                plan.getCancellationReason(),
                societies.findById(plan.getSocietyId()).map(Society::getName).orElse(null),
                plan.getRequirementId() == null
                        ? null
                        : requirements
                                .findById(plan.getRequirementId())
                                .map(RequirementRequest::getRequirementDetails)
                                .orElse(null));
    }

    private java.util.List<String> strings(String value) {
        try {
            return json.readStrings(value);
        } catch (JsonCodecException exception) {
            throw new IllegalStateException("Stored requirement modules are invalid", exception);
        }
    }
}
