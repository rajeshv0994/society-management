package com.societyapp.controller;

import com.societyapp.application.port.in.DashboardUseCase;
import com.societyapp.controller.mapper.DashboardHttpMapper;
import com.societyapp.controller.model.DashboardResponse;
import com.societyapp.security.CurrentUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardUseCase service;
    private final DashboardHttpMapper mapper;

    public DashboardController(DashboardUseCase service, DashboardHttpMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/platform")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    DashboardResponse.Platform platform() {
        return mapper.toResponse(service.platform());
    }

    @GetMapping("/society")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    DashboardResponse.Society society() {
        return mapper.toResponse(service.society(CurrentUser.societyId()));
    }
}
