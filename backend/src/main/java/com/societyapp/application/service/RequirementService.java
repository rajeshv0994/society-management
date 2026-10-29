package com.societyapp.application.service;

import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.mapper.RequirementViewMapper;
import com.societyapp.application.model.requirement.RequirementModels;
import com.societyapp.application.port.in.RequirementUseCase;
import com.societyapp.application.port.out.JsonCodec;
import com.societyapp.application.port.out.JsonCodecException;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.usecase.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.CustomPlanStatus;
import com.societyapp.domain.type.RequirementStatus;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class RequirementService implements RequirementUseCase {
    private final RequirementRequestRepository requirements;
    private final SocietyCustomPlanRepository customPlans;
    private final SocietyRepository societies;
    private final JsonCodec json;
    private final RequirementViewMapper viewMapper;

    public RequirementService(
            RequirementRequestRepository requirements,
            SocietyCustomPlanRepository customPlans,
            SocietyRepository societies,
            JsonCodec json,
            RequirementViewMapper viewMapper) {
        this.requirements = requirements;
        this.customPlans = customPlans;
        this.societies = societies;
        this.json = json;
        this.viewMapper = viewMapper;
    }

    @Override
    public RequirementModels.Submitted submit(RequirementModels.SubmitCommand command) {
        if (command.societyId() != null && !societies.existsById(command.societyId())) {
            throw new ResourceNotFoundException("Society not found");
        }
        var request = new RequirementRequest();
        request.setSocietyId(command.societyId());
        request.setSocietyName(command.societyName());
        request.setContactPerson(command.contactPerson());
        request.setEmail(command.email());
        request.setMobile(command.mobile());
        request.setCity(command.city());
        request.setEstimatedFlats(command.estimatedSpaces());
        request.setEstimatedStaff(command.estimatedStaff());
        request.setRequestedModules(writeJson(command.requestedModules()));
        request.setRequirementDetails(command.requirementDetails());
        request.setBudgetMonthly(command.budgetMonthly());
        request.setStatus(RequirementStatus.NEW.code());
        request.setCreatedAt(OffsetDateTime.now());
        request.setUpdatedAt(OffsetDateTime.now());
        request = requirements.save(request);
        return new RequirementModels.Submitted(
                request.getId(), RequirementStatus.NEW, "Our platform team will contact you.");
    }

    @Override
    public List<RequirementModels.RequirementView> list() {
        return requirements.findAllByOrderByCreatedAtDesc().stream()
                .sorted(
                        Comparator.comparingInt(
                                item -> RequirementStatus.from(item.getStatus()).order()))
                .map(viewMapper::requirement)
                .toList();
    }

    @Override
    public RequirementModels.Status review(UUID id, RequirementModels.ReviewCommand command) {
        var request =
                requirements
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Requirement not found"));
        var requestedStatus = command.status();
        request.setStatus(requestedStatus.code());
        if (command.notes() != null) request.setAdminNotes(command.notes());
        request.setAssignedTo(command.reviewerId());
        request.setUpdatedAt(OffsetDateTime.now());
        request.setClosedAt(requestedStatus.isClosed() ? OffsetDateTime.now() : null);
        requirements.save(request);
        return new RequirementModels.Status(id, requestedStatus);
    }

    @Override
    public RequirementModels.PlanResult createPlan(
            UUID creatorId, RequirementModels.PlanCommand command) {
        validateDates(command.validFrom(), command.validUntil());
        if (!societies.existsById(command.societyId())) {
            throw new ResourceNotFoundException("Society not found");
        }
        var requestedStatus = command.status();
        if (requestedStatus == CustomPlanStatus.ACTIVE) {
            replaceOtherActivePlans(command.societyId(), null);
        }
        var plan = new SocietyCustomPlan();
        plan.setSocietyId(command.societyId());
        plan.setRequirementId(command.requirementId());
        applyPlanFields(plan, command);
        plan.setCreatedBy(creatorId);
        plan.setCreatedAt(OffsetDateTime.now());
        plan.setActivatedAt(
                requestedStatus == CustomPlanStatus.ACTIVE ? OffsetDateTime.now() : null);
        plan = customPlans.save(plan);
        if (command.requirementId() != null) {
            requirements
                    .findById(command.requirementId())
                    .ifPresent(
                            request -> {
                                request.setSocietyId(command.societyId());
                                request.setStatus(RequirementStatus.ACCEPTED.code());
                                request.setUpdatedAt(OffsetDateTime.now());
                                requirements.save(request);
                            });
        }
        return new RequirementModels.PlanResult(plan.getId(), command.societyId(), requestedStatus);
    }

    @Override
    public RequirementModels.PlanResult updatePlan(UUID id, RequirementModels.PlanCommand command) {
        validateDates(command.validFrom(), command.validUntil());
        var plan =
                customPlans
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Custom plan not found"));
        var currentStatus = CustomPlanStatus.from(plan.getStatus());
        var requestedStatus = command.status();
        if (currentStatus == CustomPlanStatus.REPLACED
                || currentStatus == CustomPlanStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "A replaced or cancelled custom plan is an immutable history record");
        }
        if (currentStatus == CustomPlanStatus.ACTIVE && requestedStatus == CustomPlanStatus.DRAFT) {
            throw new IllegalArgumentException(
                    "An active custom plan cannot return to draft; activate a replacement instead");
        }
        if (requestedStatus == CustomPlanStatus.ACTIVE) {
            replaceOtherActivePlans(plan.getSocietyId(), id);
        }
        applyPlanFields(plan, command);
        if (requestedStatus == CustomPlanStatus.ACTIVE && plan.getActivatedAt() == null) {
            plan.setActivatedAt(OffsetDateTime.now());
        }
        customPlans.save(plan);
        return new RequirementModels.PlanResult(id, plan.getSocietyId(), requestedStatus);
    }

    @Override
    public RequirementModels.PlanResult cancelPlan(UUID id, UUID actorId, String reason) {
        var plan =
                customPlans
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Custom plan not found"));
        var currentStatus = CustomPlanStatus.from(plan.getStatus());
        if (currentStatus != CustomPlanStatus.DRAFT && currentStatus != CustomPlanStatus.ACTIVE) {
            throw new IllegalStateException("Only a Draft or Active custom plan can be cancelled");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Cancellation reason is required");
        }
        plan.setStatus(CustomPlanStatus.CANCELLED.code());
        plan.setCancelledBy(actorId);
        plan.setCancelledAt(OffsetDateTime.now());
        plan.setCancellationReason(reason.trim());
        customPlans.save(plan);
        return new RequirementModels.PlanResult(
                id, plan.getSocietyId(), CustomPlanStatus.CANCELLED);
    }

    @Override
    public List<RequirementModels.PlanView> plans() {
        return customPlans.findAllByOrderByCreatedAtDesc().stream().map(viewMapper::plan).toList();
    }

    private void validateDates(LocalDate validFrom, LocalDate validUntil) {
        if (validUntil != null && validUntil.isBefore(validFrom)) {
            throw new IllegalArgumentException("Valid-until must be after valid-from");
        }
    }

    private void replaceOtherActivePlans(UUID societyId, UUID excludedPlanId) {
        customPlans.findAll().stream()
                .filter(plan -> societyId.equals(plan.getSocietyId()))
                .filter(plan -> CustomPlanStatus.ACTIVE.matches(plan.getStatus()))
                .filter(plan -> excludedPlanId == null || !excludedPlanId.equals(plan.getId()))
                .forEach(
                        plan -> {
                            plan.setStatus(CustomPlanStatus.REPLACED.code());
                            customPlans.save(plan);
                        });
    }

    private void applyPlanFields(SocietyCustomPlan plan, RequirementModels.PlanCommand command) {
        var fixed =
                command.fixedMonthlyPrice() == null ? BigDecimal.ZERO : command.fixedMonthlyPrice();
        if (fixed.signum() > 0
                && (command.pricePerSpace().signum() > 0 || command.pricePerStaff().signum() > 0)) {
            throw new IllegalArgumentException(
                    "Choose either a fixed monthly price or per-space/per-staff rates");
        }
        plan.setPlanName(command.name());
        plan.setDescription(command.description());
        plan.setEnabledModules(writeJson(command.enabledModules()));
        plan.setMaxFlats(command.maxSpaces());
        plan.setMaxStaff(command.maxStaff());
        plan.setMaxStorageGb(command.maxStorageGb());
        plan.setPricePerSpace(command.pricePerSpace());
        plan.setPricePerStaff(command.pricePerStaff());
        plan.setPriceMonthly(fixed);
        plan.setSetupFee(command.setupFee() == null ? BigDecimal.ZERO : command.setupFee());
        plan.setBillingCycle(command.billingCycle().code());
        plan.setValidFrom(command.validFrom());
        plan.setValidUntil(command.validUntil());
        plan.setStatus(command.status().code());
    }

    private String writeJson(List<String> values) {
        try {
            return json.write(values);
        } catch (JsonCodecException exception) {
            throw new IllegalArgumentException("Invalid module list", exception);
        }
    }
}
