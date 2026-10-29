package com.societyapp.controller;

import com.societyapp.application.model.household.HouseholdModels;
import com.societyapp.application.port.in.HouseholdUseCase;
import com.societyapp.controller.mapper.HouseholdHttpMapper;
import com.societyapp.controller.model.HouseholdResponse;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
@PreAuthorize("hasRole('RESIDENT')")
public class HouseholdController {
    private final HouseholdUseCase service;
    private final HouseholdHttpMapper mapper = new HouseholdHttpMapper();

    public HouseholdController(HouseholdUseCase service) {
        this.service = service;
    }

    public record Add(
            @NotBlank String fullName,
            @NotBlank String relationship,
            String mobile,
            LocalDate dateOfBirth) {}

    @GetMapping("/profile")
    HouseholdResponse.Household profile() {
        return mapper.toResponse(service.profile(CurrentUser.societyId(), CurrentUser.id()));
    }

    @PostMapping("/household")
    HouseholdResponse.Member add(@Valid @RequestBody Add r) {
        return mapper.toResponse(
                service.add(
                        CurrentUser.societyId(),
                        CurrentUser.id(),
                        new HouseholdModels.MemberCommand(
                                r.fullName(),
                                r.relationship(),
                                r.mobile(),
                                null,
                                r.dateOfBirth(),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null)));
    }

    @DeleteMapping("/household/{id}")
    HouseholdResponse.EntityResult remove(@PathVariable UUID id) {
        return mapper.toResponse(service.remove(CurrentUser.societyId(), CurrentUser.id(), id));
    }
}
