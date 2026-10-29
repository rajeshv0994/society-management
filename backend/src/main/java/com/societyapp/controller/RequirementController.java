package com.societyapp.controller;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.societyapp.application.model.requirement.RequirementModels;
import com.societyapp.application.port.in.RequirementUseCase;
import com.societyapp.controller.mapper.RequirementHttpMapper;
import com.societyapp.controller.model.RequirementResponse;
import com.societyapp.domain.type.BillingCycle;
import com.societyapp.domain.type.CustomPlanStatus;
import com.societyapp.domain.type.RequirementStatus;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RequirementController {
    private final RequirementUseCase service;
    private final RequirementHttpMapper mapper = new RequirementHttpMapper();

    public RequirementController(RequirementUseCase service) {
        this.service = service;
    }

    public record Submit(
            UUID societyId,
            @NotBlank String societyName,
            @NotBlank String contactPerson,
            @Email String email,
            @NotBlank String mobile,
            String city,
            @JsonAlias({"estimatedSpaces", "estimatedUnits"}) @Min(1) Integer estimatedFlats,
            @Min(0) Integer estimatedStaff,
            @NotEmpty List<String> requestedModules,
            @NotBlank String requirementDetails,
            @DecimalMin("0") BigDecimal budgetMonthly) {}

    public record Review(
            @Pattern(regexp = "NEW|CONTACTED|ANALYSING|PROPOSAL_SENT|ACCEPTED|REJECTED|CLOSED")
                    String status,
            String adminNotes) {}

    public record CustomPlan(
            @NotNull UUID societyId,
            UUID requirementId,
            @NotBlank String planName,
            String description,
            @NotEmpty List<String> enabledModules,
            @JsonAlias({"maxSpaces", "maxUnits"}) @Min(1) Integer maxFlats,
            @Min(1) Integer maxStaff,
            @Min(1) Integer maxStorageGb,
            @JsonAlias("priceMonthly") @NotNull @DecimalMin("0") BigDecimal pricePerSpace,
            @NotNull @DecimalMin("0") BigDecimal pricePerStaff,
            @DecimalMin("0") BigDecimal fixedMonthlyPrice,
            @DecimalMin("0") BigDecimal setupFee,
            @NotBlank @Pattern(regexp = "MONTHLY|QUARTERLY|YEARLY") String billingCycle,
            @NotNull LocalDate validFrom,
            LocalDate validUntil,
            @NotBlank @Pattern(regexp = "DRAFT|ACTIVE") String status) {}

    public record CustomPlanUpdate(
            @NotBlank String planName,
            String description,
            @NotEmpty List<String> enabledModules,
            @JsonAlias({"maxSpaces", "maxUnits"}) @Min(1) Integer maxFlats,
            @Min(1) Integer maxStaff,
            @Min(1) Integer maxStorageGb,
            @NotNull @DecimalMin("0") BigDecimal pricePerSpace,
            @NotNull @DecimalMin("0") BigDecimal pricePerStaff,
            @DecimalMin("0") BigDecimal fixedMonthlyPrice,
            @DecimalMin("0") BigDecimal setupFee,
            @NotBlank @Pattern(regexp = "MONTHLY|QUARTERLY|YEARLY") String billingCycle,
            @NotNull LocalDate validFrom,
            LocalDate validUntil,
            @NotBlank @Pattern(regexp = "DRAFT|ACTIVE") String status) {}

    public record CancelCustomPlan(@NotBlank @Size(max = 300) String reason) {}

    @PostMapping("/public/requirements")
    RequirementResponse.Submitted submit(@Valid @RequestBody Submit r) {
        return mapper.toResponse(
                service.submit(
                        new RequirementModels.SubmitCommand(
                                r.societyId(),
                                r.societyName(),
                                r.contactPerson(),
                                r.email(),
                                r.mobile(),
                                r.city(),
                                r.estimatedFlats(),
                                r.estimatedStaff(),
                                r.requestedModules(),
                                r.requirementDetails(),
                                r.budgetMonthly())));
    }

    @GetMapping("/admin/platform/requirements")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    List<RequirementResponse.Requirement> list() {
        return service.list().stream().map(mapper::toResponse).toList();
    }

    @PatchMapping("/admin/platform/requirements/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    RequirementResponse.Status review(@PathVariable UUID id, @Valid @RequestBody Review r) {
        return mapper.toResponse(
                service.review(
                        id,
                        new RequirementModels.ReviewCommand(
                                CurrentUser.id(),
                                RequirementStatus.from(r.status()),
                                r.adminNotes())));
    }

    @PostMapping("/admin/platform/custom-plans")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    RequirementResponse.PlanResult createPlan(@Valid @RequestBody CustomPlan r) {
        return mapper.toResponse(service.createPlan(CurrentUser.id(), planCommand(r)));
    }

    @PutMapping("/admin/platform/custom-plans/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    RequirementResponse.PlanResult updatePlan(
            @PathVariable UUID id, @Valid @RequestBody CustomPlanUpdate r) {
        return mapper.toResponse(service.updatePlan(id, planCommand(r)));
    }

    @PostMapping("/admin/platform/custom-plans/{id}/cancel")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    RequirementResponse.PlanResult cancelPlan(
            @PathVariable UUID id, @Valid @RequestBody CancelCustomPlan request) {
        return mapper.toResponse(service.cancelPlan(id, CurrentUser.id(), request.reason()));
    }

    @GetMapping("/admin/platform/custom-plans")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    List<RequirementResponse.Plan> plans() {
        return service.plans().stream().map(mapper::toResponse).toList();
    }

    private RequirementModels.PlanCommand planCommand(CustomPlan r) {
        return new RequirementModels.PlanCommand(
                r.societyId(),
                r.requirementId(),
                r.planName(),
                r.description(),
                r.enabledModules(),
                r.maxFlats(),
                r.maxStaff(),
                r.maxStorageGb(),
                r.pricePerSpace(),
                r.pricePerStaff(),
                r.fixedMonthlyPrice(),
                r.setupFee(),
                BillingCycle.from(r.billingCycle()),
                r.validFrom(),
                r.validUntil(),
                CustomPlanStatus.from(r.status()));
    }

    private RequirementModels.PlanCommand planCommand(CustomPlanUpdate r) {
        return new RequirementModels.PlanCommand(
                null,
                null,
                r.planName(),
                r.description(),
                r.enabledModules(),
                r.maxFlats(),
                r.maxStaff(),
                r.maxStorageGb(),
                r.pricePerSpace(),
                r.pricePerStaff(),
                r.fixedMonthlyPrice(),
                r.setupFee(),
                BillingCycle.from(r.billingCycle()),
                r.validFrom(),
                r.validUntil(),
                CustomPlanStatus.from(r.status()));
    }
}
