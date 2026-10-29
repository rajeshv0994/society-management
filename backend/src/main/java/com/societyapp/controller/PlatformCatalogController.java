package com.societyapp.controller;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.societyapp.application.model.platformadmin.PlatformAdminModels;
import com.societyapp.application.port.in.PlatformAdminUseCase;
import com.societyapp.controller.mapper.PlatformAdminHttpMapper;
import com.societyapp.controller.model.PlatformAdminResponse;
import com.societyapp.domain.type.*;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/platform")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class PlatformCatalogController {
    private final PlatformAdminUseCase service;
    private final PlatformAdminHttpMapper mapper = new PlatformAdminHttpMapper();

    public PlatformCatalogController(PlatformAdminUseCase service) {
        this.service = service;
    }

    public record PlanUpdate(
            String name,
            @Pattern(regexp = "AUTO|MANUAL") String approvalMode,
            @JsonAlias({"maxSpaces", "maxUnits"}) @Min(1) Integer maxFlats,
            @JsonAlias("priceMonthly") @NotNull @DecimalMin("0") BigDecimal pricePerSpace,
            @Min(1) Integer maxStaff,
            @NotNull @DecimalMin("0") BigDecimal pricePerStaff,
            @Min(1) Integer trialDurationValue,
            @Pattern(regexp = "DAYS|MONTHS") String trialDurationUnit,
            @Min(1) Integer trialDurationMonths) {}

    public record ServiceDefinition(
            @NotBlank @Pattern(regexp = "[A-Z][A-Z0-9_]{1,39}") String code,
            @NotBlank String name,
            String description,
            @NotBlank @Pattern(regexp = "ACTIVE|COMING_SOON|RETIRED") String status,
            @Min(0) int sortOrder,
            @NotBlank @Pattern(regexp = "DEFAULT_FREE|OPTIONAL_FREE|PAID_PER_SPACE")
                    String availabilityType,
            @NotNull @DecimalMin("0.00") BigDecimal pricePerSpace) {}

    @GetMapping("/plans")
    List<PlatformAdminResponse.Plan> plans() {
        return service.plans().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/services")
    List<PlatformAdminResponse.Service> services() {
        return service.services().stream().map(mapper::toResponse).toList();
    }

    @PostMapping("/services")
    PlatformAdminResponse.Service createService(@Valid @RequestBody ServiceDefinition request) {
        return mapper.toResponse(service.createService(CurrentUser.id(), serviceCommand(request)));
    }

    @PutMapping("/services/{code}")
    PlatformAdminResponse.Service updateService(
            @PathVariable String code, @Valid @RequestBody ServiceDefinition request) {
        return mapper.toResponse(
                service.updateService(CurrentUser.id(), code, serviceCommand(request)));
    }

    @PutMapping("/plans/{code}")
    PlatformAdminResponse.PlanUpdated plan(
            @PathVariable String code, @Valid @RequestBody PlanUpdate request) {
        return mapper.toResponse(
                service.updatePlan(
                        CurrentUser.id(),
                        SubscriptionPlanCode.from(code),
                        new PlatformAdminModels.PlanCommand(
                                request.name(),
                                ApprovalMode.from(request.approvalMode()),
                                request.maxFlats(),
                                request.pricePerSpace(),
                                request.maxStaff(),
                                request.pricePerStaff(),
                                request.trialDurationValue(),
                                request.trialDurationUnit() == null
                                        ? null
                                        : TrialDurationUnit.from(request.trialDurationUnit()),
                                request.trialDurationMonths())));
    }

    private PlatformAdminModels.ServiceCommand serviceCommand(ServiceDefinition request) {
        return new PlatformAdminModels.ServiceCommand(
                request.code(),
                request.name(),
                request.description(),
                PlatformServiceStatus.from(request.status()),
                request.sortOrder(),
                ServiceAvailabilityType.from(request.availabilityType()),
                request.pricePerSpace());
    }
}
