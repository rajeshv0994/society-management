package com.societyapp.controller;

import com.societyapp.application.port.in.ConfigurationUseCase;
import com.societyapp.controller.mapper.ConfigurationHttpMapper;
import com.societyapp.controller.model.ConfigurationResponse;
import com.societyapp.domain.type.UserRole;
import com.societyapp.security.CurrentUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configuration/reference")
@PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
public class ConfigurationReferenceController {
    private final ConfigurationUseCase service;
    private final ConfigurationHttpMapper mapper = new ConfigurationHttpMapper();

    public ConfigurationReferenceController(ConfigurationUseCase service) {
        this.service = service;
    }

    @GetMapping
    public ConfigurationResponse.Reference reference() {
        return mapper.toResponse(
                service.reference(
                        CurrentUser.societyId(),
                        CurrentUser.id(),
                        UserRole.from(CurrentUser.role())));
    }
}
