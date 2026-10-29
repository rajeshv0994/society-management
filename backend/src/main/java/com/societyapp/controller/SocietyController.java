package com.societyapp.controller;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.societyapp.application.model.onboarding.SocietyOnboardingModels;
import com.societyapp.application.port.in.SocietyOnboardingUseCase;
import com.societyapp.controller.mapper.SocietyOnboardingHttpMapper;
import com.societyapp.controller.model.SocietyOnboardingResponse;
import com.societyapp.domain.type.SubscriptionPlanCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SocietyController {
    private final SocietyOnboardingUseCase service;
    private final SocietyOnboardingHttpMapper mapper = new SocietyOnboardingHttpMapper();

    public SocietyController(SocietyOnboardingUseCase service) {
        this.service = service;
    }

    public record Registration(
            @NotBlank String name,
            @NotBlank String societyType,
            @Min(1) int totalBlocks,
            @JsonAlias({"totalSpaces", "totalUnits"}) @Min(1) int totalFlats,
            @Min(0) int plannedStaff,
            Integer approximateResidents,
            @NotBlank String address,
            @NotBlank String city,
            @NotBlank String state,
            @Pattern(regexp = "[0-9]{6}") String pinCode,
            @NotBlank String contactPerson,
            @Email String email,
            @NotBlank String mobile,
            @NotBlank String planCode,
            @NotNull List<String> enabledModules) {}

    public record Activation(
            @NotBlank String setupCode,
            @NotBlank String fullName,
            @Email String email,
            @NotBlank @Size(min = 8) String password,
            String mobile) {}

    public record Rejection(@NotBlank String reason) {}

    @GetMapping("/public/plans")
    List<SocietyOnboardingResponse.Plan> plans() {
        return service.plans().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/public/services")
    List<SocietyOnboardingResponse.ServiceModule> services() {
        return service.services().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/public/societies")
    List<SocietyOnboardingResponse.ActiveSociety> societies() {
        return service.activeSocieties().stream().map(mapper::toResponse).toList();
    }

    @PostMapping("/public/societies/register")
    SocietyOnboardingResponse.RegistrationResult register(@Valid @RequestBody Registration r) {
        return mapper.toResponse(
                service.register(
                        new SocietyOnboardingModels.RegistrationCommand(
                                r.name(),
                                r.societyType(),
                                r.totalBlocks(),
                                r.totalFlats(),
                                r.plannedStaff(),
                                r.approximateResidents(),
                                r.address(),
                                r.city(),
                                r.state(),
                                r.pinCode(),
                                r.contactPerson(),
                                r.email(),
                                r.mobile(),
                                SubscriptionPlanCode.from(r.planCode()),
                                r.enabledModules())));
    }

    @GetMapping("/admin/societies/pending")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    List<SocietyOnboardingResponse.PendingSociety> pending() {
        return service.pending().stream().map(mapper::toResponse).toList();
    }

    @PostMapping("/admin/societies/{id}/approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    SocietyOnboardingResponse.Approved approve(@PathVariable UUID id) {
        return mapper.toResponse(service.approve(id));
    }

    @PostMapping("/admin/societies/{id}/reject")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    SocietyOnboardingResponse.Rejected reject(
            @PathVariable UUID id, @Valid @RequestBody Rejection body) {
        return mapper.toResponse(service.reject(id, body.reason()));
    }

    @PostMapping("/public/societies/activate")
    SocietyOnboardingResponse.Activated activate(@Valid @RequestBody Activation a) {
        return mapper.toResponse(
                service.activate(
                        new SocietyOnboardingModels.ActivationCommand(
                                a.setupCode(), a.fullName(), a.email(), a.password(), a.mobile())));
    }
}
