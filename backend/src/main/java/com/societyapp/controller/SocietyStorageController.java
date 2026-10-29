package com.societyapp.controller;

import com.societyapp.application.port.in.StorageCleanupUseCase;
import com.societyapp.application.port.in.StorageMeterUseCase;
import com.societyapp.controller.mapper.StorageCleanupHttpMapper;
import com.societyapp.controller.mapper.StorageHttpMapper;
import com.societyapp.controller.model.StorageDeletionResponse;
import com.societyapp.controller.model.StorageResponse;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/society/storage")
@PreAuthorize("hasRole('SOCIETY_ADMIN')")
public class SocietyStorageController {
    private final StorageMeterUseCase storage;
    private final StorageCleanupUseCase cleanup;
    private final StorageCleanupHttpMapper cleanupMapper;
    private final StorageHttpMapper storageMapper;

    public SocietyStorageController(
            StorageMeterUseCase storage,
            StorageCleanupUseCase cleanup,
            StorageCleanupHttpMapper cleanupMapper,
            StorageHttpMapper storageMapper) {
        this.storage = storage;
        this.cleanup = cleanup;
        this.cleanupMapper = cleanupMapper;
        this.storageMapper = storageMapper;
    }

    public record Purchase(@Min(1) @Max(100) int blocks) {}

    @GetMapping
    StorageResponse.SocietyDashboard dashboard() {
        return storageMapper.toResponse(storage.societyDashboard(CurrentUser.societyId()));
    }

    @PostMapping("/purchases")
    StorageResponse.Purchase purchase(@Valid @RequestBody Purchase request) {
        return storageMapper.toResponse(
                storage.purchase(CurrentUser.societyId(), CurrentUser.id(), request.blocks()));
    }

    @DeleteMapping("/files/{id}")
    StorageDeletionResponse delete(@PathVariable UUID id) {
        return cleanupMapper.toResponse(
                cleanup.delete(CurrentUser.societyId(), CurrentUser.id(), id));
    }
}
