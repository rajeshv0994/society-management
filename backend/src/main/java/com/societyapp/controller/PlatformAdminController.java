package com.societyapp.controller;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.societyapp.application.model.platformadmin.PlatformAdminModels;
import com.societyapp.application.port.in.PlatformAdminUseCase;
import com.societyapp.application.port.in.PlatformMembershipBillingUseCase;
import com.societyapp.controller.mapper.PlatformAdminHttpMapper;
import com.societyapp.controller.mapper.PlatformBillingHttpMapper;
import com.societyapp.controller.model.PlatformAdminResponse;
import com.societyapp.controller.model.PlatformBillingResponse;
import com.societyapp.domain.type.*;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/platform")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class PlatformAdminController {
    private final PlatformAdminUseCase service;
    private final PlatformMembershipBillingUseCase membershipBilling;
    private final PlatformAdminHttpMapper mapper = new PlatformAdminHttpMapper();
    private final PlatformBillingHttpMapper billingMapper = new PlatformBillingHttpMapper();

    public PlatformAdminController(
            PlatformAdminUseCase service, PlatformMembershipBillingUseCase membershipBilling) {
        this.service = service;
        this.membershipBilling = membershipBilling;
    }

    public record CreateSociety(
            @NotBlank String name,
            @NotBlank String societyType,
            @Min(1) int totalBlocks,
            @JsonAlias({"totalSpaces", "totalUnits"}) @Min(1) int totalFlats,
            @Min(0) int plannedStaff,
            @NotBlank String address,
            @NotBlank String city,
            @NotBlank String state,
            @Pattern(regexp = "[0-9]{6}") String pinCode,
            @NotBlank String contactPerson,
            @Email String email,
            @NotBlank String mobile,
            @NotBlank String planCode,
            @NotNull List<String> enabledModules,
            Boolean issueInvoice) {}

    public record SocietyUpdate(
            @NotBlank String name,
            String description,
            String website,
            @NotBlank String contactPerson,
            @Email String email,
            @NotBlank String mobile,
            @NotBlank String address,
            @NotBlank String city,
            @NotBlank String state,
            @Pattern(regexp = "[0-9]{6}") String pinCode,
            @NotBlank String planCode,
            @Min(0) int plannedStaff,
            @Pattern(regexp = "ACTIVE|SUSPENDED|APPROVED") String status,
            @NotNull List<String> enabledModules) {}

    @GetMapping("/dashboard")
    PlatformAdminResponse.Dashboard dashboard() {
        return mapper.toResponse(service.dashboard());
    }

    @GetMapping("/societies")
    List<PlatformAdminResponse.SocietySummary> societies(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "search", required = false) String search) {
        return service
                .societies(status == null ? null : SocietyStatus.from(status), search)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @PostMapping("/societies")
    PlatformAdminResponse.SocietyCreated createSociety(@Valid @RequestBody CreateSociety request) {
        var created =
                service.createSociety(
                        CurrentUser.id(),
                        new PlatformAdminModels.CreateSocietyCommand(
                                request.name(),
                                request.societyType(),
                                request.totalBlocks(),
                                request.totalFlats(),
                                request.plannedStaff(),
                                request.address(),
                                request.city(),
                                request.state(),
                                request.pinCode(),
                                request.contactPerson(),
                                request.email(),
                                request.mobile(),
                                SubscriptionPlanCode.from(request.planCode()),
                                request.enabledModules()));
        PlatformBillingResponse.Order invoice = null;
        if (Boolean.TRUE.equals(request.issueInvoice())
                && !SubscriptionPlanCode.TRIAL.matches(request.planCode())) {
            var today = LocalDate.now();
            invoice =
                    billingMapper.toResponse(
                            membershipBilling.issue(
                                    created.societyId(),
                                    today.withDayOfMonth(1),
                                    today.plusDays(15)));
        }
        return mapper.toResponse(created, invoice);
    }

    @GetMapping("/societies/{id}")
    PlatformAdminResponse.SocietyDetails society(@PathVariable UUID id) {
        return mapper.toResponse(service.society(id));
    }

    @PutMapping("/societies/{id}")
    PlatformAdminResponse.SocietyUpdated updateSociety(
            @PathVariable UUID id, @Valid @RequestBody SocietyUpdate request) {
        return mapper.toResponse(
                service.updateSociety(
                        CurrentUser.id(),
                        id,
                        new PlatformAdminModels.UpdateSocietyCommand(
                                request.name(),
                                request.description(),
                                request.website(),
                                request.contactPerson(),
                                request.email(),
                                request.mobile(),
                                request.address(),
                                request.city(),
                                request.state(),
                                request.pinCode(),
                                SubscriptionPlanCode.from(request.planCode()),
                                request.plannedStaff(),
                                SocietyStatus.from(request.status()),
                                request.enabledModules())));
    }
}
