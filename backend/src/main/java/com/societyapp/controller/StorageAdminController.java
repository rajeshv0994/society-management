package com.societyapp.controller;

import com.societyapp.application.port.in.StorageMeterUseCase;
import com.societyapp.controller.mapper.StorageHttpMapper;
import com.societyapp.controller.model.StorageResponse;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/platform/storage")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class StorageAdminController {
    private final StorageMeterUseCase storage;
    private final StorageHttpMapper mapper;

    public StorageAdminController(StorageMeterUseCase storage, StorageHttpMapper mapper) {
        this.storage = storage;
        this.mapper = mapper;
    }

    public record PolicyUpdate(
            @Min(0) int defaultFreeGb,
            @Min(1) int purchaseBlockGb,
            @NotNull @DecimalMin("0.00") BigDecimal pricePerBlockMonthly) {}

    @GetMapping
    StorageResponse.AdminDashboard dashboard() {
        return mapper.toResponse(storage.adminDashboard());
    }

    @PutMapping("/policy")
    StorageResponse.Policy policy(@Valid @RequestBody PolicyUpdate request) {
        return mapper.toResponse(
                storage.updatePolicy(
                        CurrentUser.id(),
                        request.defaultFreeGb(),
                        request.purchaseBlockGb(),
                        request.pricePerBlockMonthly()));
    }
}
