package com.societyapp.controller;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.societyapp.application.model.resident.ResidentModels;
import com.societyapp.application.port.in.ResidentUseCase;
import com.societyapp.controller.mapper.ResidentHttpMapper;
import com.societyapp.controller.model.ResidentResponse;
import com.societyapp.domain.type.ApprovalAction;
import com.societyapp.domain.type.ResidentType;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ResidentController {
    private final ResidentUseCase service;
    private final ResidentHttpMapper mapper = new ResidentHttpMapper();

    public ResidentController(ResidentUseCase service) {
        this.service = service;
    }

    public record Request(
            @NotNull UUID societyId,
            @JsonAlias({"spaceId", "unitId"}) @NotNull UUID flatId,
            @Pattern(regexp = "OWNER|TENANT") String residentType,
            @NotBlank String fullName,
            @NotBlank String mobile,
            @Email String email,
            LocalDate dob,
            String gender,
            String emergencyContact,
            LocalDate moveInDate,
            LocalDate moveOutDate) {}

    public record Review(
            @Pattern(regexp = "APPROVE|REJECT") String decision,
            String reason,
            @Size(min = 8) String temporaryPassword) {}

    public record AdminCreate(
            @JsonAlias({"spaceId", "unitId"}) @NotNull UUID flatId,
            @Pattern(regexp = "OWNER|TENANT") String residentType,
            @NotBlank String fullName,
            @NotBlank String mobile,
            @NotBlank @Email String email,
            LocalDate dob,
            String gender,
            String emergencyContact,
            LocalDate moveInDate,
            LocalDate moveOutDate,
            @NotBlank @Size(min = 8) String temporaryPassword) {}

    public record HouseholdMember(
            @NotBlank String fullName,
            @NotBlank String relationship,
            String mobile,
            LocalDate dateOfBirth) {}

    @GetMapping({
        "/public/societies/{societyId}/spaces",
        "/public/societies/{societyId}/units",
        "/public/societies/{societyId}/flats"
    })
    List<ResidentResponse.SpaceOption> flats(@PathVariable UUID societyId) {
        return service.spaces(societyId).stream().map(mapper::toResponse).toList();
    }

    @PostMapping("/public/residents/register")
    ResidentResponse.RegistrationResult register(@Valid @RequestBody Request r) {
        return mapper.toResponse(service.register(registration(r)));
    }

    @GetMapping("/society/residents/pending")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    List<ResidentResponse.Registration> pending() {
        return service.pending(CurrentUser.societyId()).stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/society/residents")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    List<ResidentResponse.Summary> residents() {
        return service.residents(CurrentUser.societyId()).stream().map(mapper::toResponse).toList();
    }

    @PostMapping("/society/residents")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    ResidentResponse.ReviewResult create(@Valid @RequestBody AdminCreate request) {
        var registration =
                new ResidentModels.RegistrationCommand(
                        CurrentUser.societyId(),
                        request.flatId(),
                        ResidentType.from(request.residentType()),
                        request.fullName(),
                        request.mobile(),
                        request.email(),
                        request.dob(),
                        request.gender(),
                        request.emergencyContact(),
                        request.moveInDate(),
                        request.moveOutDate());
        return mapper.toResponse(
                service.createResident(
                        new ResidentModels.AdminCreateCommand(
                                registration, CurrentUser.id(), request.temporaryPassword())));
    }

    @GetMapping("/society/residents/{id}")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    ResidentResponse.Detail resident(@PathVariable UUID id) {
        return mapper.toResponse(service.resident(CurrentUser.societyId(), id));
    }

    @PostMapping("/society/residents/{id}/household")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    ResidentResponse.EntityResult addHousehold(
            @PathVariable UUID id, @Valid @RequestBody HouseholdMember member) {
        return mapper.toResponse(
                service.addHouseholdMember(
                        CurrentUser.societyId(),
                        id,
                        new ResidentModels.HouseholdCommand(
                                member.fullName(),
                                member.relationship(),
                                member.mobile(),
                                member.dateOfBirth())));
    }

    @DeleteMapping("/society/residents/{residentId}/household/{memberId}")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    ResidentResponse.EntityResult removeHousehold(
            @PathVariable UUID residentId, @PathVariable UUID memberId) {
        return mapper.toResponse(
                service.removeHouseholdMember(CurrentUser.societyId(), residentId, memberId));
    }

    @PostMapping("/society/residents/{id}/review")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    ResidentResponse.ReviewResult review(@PathVariable UUID id, @Valid @RequestBody Review review) {
        return mapper.toResponse(
                service.review(
                        CurrentUser.societyId(),
                        id,
                        new ResidentModels.ReviewCommand(
                                CurrentUser.id(), ApprovalAction.from(review.decision()),
                                review.reason(), review.temporaryPassword())));
    }

    private ResidentModels.RegistrationCommand registration(Request value) {
        return new ResidentModels.RegistrationCommand(
                value.societyId(),
                value.flatId(),
                ResidentType.from(value.residentType()),
                value.fullName(),
                value.mobile(),
                value.email(),
                value.dob(),
                value.gender(),
                value.emergencyContact(),
                value.moveInDate(),
                value.moveOutDate());
    }
}
