package com.societyapp.controller;

import com.societyapp.application.model.configuration.ConfigurationModels;
import com.societyapp.application.port.in.ConfigurationUseCase;
import com.societyapp.controller.mapper.ConfigurationHttpMapper;
import com.societyapp.controller.model.ConfigurationResponse;
import com.societyapp.domain.type.*;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/society/configuration")
@PreAuthorize("hasRole('SOCIETY_ADMIN')")
public class ConfigurationController {
    private final ConfigurationUseCase service;
    private final ConfigurationHttpMapper mapper = new ConfigurationHttpMapper();

    public ConfigurationController(ConfigurationUseCase service) {
        this.service = service;
    }

    public record Parking(
            @NotBlank @Size(max = 40) String slotNumber,
            @Pattern(regexp = "CAR|BIKE|VISITOR|ACCESSIBLE") String slotType) {}

    public record ParkingAction(
            @NotBlank @Pattern(regexp = "ALLOCATE|RELEASE|RESERVE|MAINTENANCE|ACTIVATE|DEACTIVATE")
                    String action,
            UUID flatId,
            @Size(max = 30) String vehicleNumber) {}

    public record Vendor(
            @NotBlank @Size(max = 150) String name,
            @NotBlank @Size(max = 80) String serviceType,
            @Size(max = 120) String contactPerson,
            @NotBlank @Pattern(regexp = "[0-9+() -]{7,20}") String mobile,
            @Email @Size(max = 180) String email,
            @Pattern(regexp = "^$|[A-Za-z0-9 -]{5,30}") String gstNumber) {}

    public record Category(
            @NotBlank @Size(max = 100) String name, @Min(1) @Max(8760) int slaHours) {}

    public record Charge(
            @NotBlank @Size(max = 120) String name,
            @Pattern(regexp = "FIXED|PER_SQFT|PERCENTAGE") String calculationType,
            @NotNull @DecimalMin("0") BigDecimal amount,
            @Pattern(regexp = "MONTHLY|QUARTERLY|YEARLY|ONE_TIME") String frequency,
            @NotNull LocalDate effectiveFrom,
            LocalDate effectiveTo) {}

    public record Active(@NotNull Boolean active) {}

    public record VendorStatus(@Pattern(regexp = "ACTIVE|INACTIVE") String status) {}

    public record Permissions(@NotEmpty Set<String> permissions) {}

    @GetMapping
    ConfigurationResponse.Configuration get() {
        return mapper.toResponse(service.get(CurrentUser.societyId()));
    }

    @PostMapping("/parking")
    ConfigurationResponse.Parking parking(@Valid @RequestBody Parking r) {
        return mapper.toResponse(
                service.addParking(CurrentUser.societyId(), CurrentUser.id(), parkingCommand(r)));
    }

    @PutMapping("/parking/{id}")
    ConfigurationResponse.Parking parking(
            @PathVariable UUID id, @Valid @RequestBody Parking request) {
        return mapper.toResponse(
                service.updateParking(
                        CurrentUser.societyId(), CurrentUser.id(), id, parkingCommand(request)));
    }

    @PatchMapping("/parking/{id}")
    ConfigurationResponse.Parking parkingAction(
            @PathVariable UUID id, @Valid @RequestBody ParkingAction request) {
        return mapper.toResponse(
                service.parkingAction(
                        CurrentUser.societyId(),
                        CurrentUser.id(),
                        id,
                        new ConfigurationModels.ParkingActionCommand(
                                com.societyapp.domain.type.ParkingAction.from(request.action()),
                                request.flatId(),
                                request.vehicleNumber())));
    }

    @DeleteMapping("/parking/{id}")
    ConfigurationResponse.Parking removeParking(@PathVariable UUID id) {
        return mapper.toResponse(
                service.parkingAction(
                        CurrentUser.societyId(),
                        CurrentUser.id(),
                        id,
                        new ConfigurationModels.ParkingActionCommand(
                                com.societyapp.domain.type.ParkingAction.DEACTIVATE, null, null)));
    }

    @PostMapping("/vendors")
    ConfigurationResponse.Vendor vendor(@Valid @RequestBody Vendor r) {
        return mapper.toResponse(
                service.addVendor(CurrentUser.societyId(), CurrentUser.id(), vendorCommand(r)));
    }

    @PutMapping("/vendors/{id}")
    ConfigurationResponse.Vendor vendor(@PathVariable UUID id, @Valid @RequestBody Vendor request) {
        return mapper.toResponse(
                service.updateVendor(
                        CurrentUser.societyId(), CurrentUser.id(), id, vendorCommand(request)));
    }

    @PatchMapping("/vendors/{id}")
    ConfigurationResponse.Vendor vendorStatus(
            @PathVariable UUID id, @Valid @RequestBody VendorStatus request) {
        return mapper.toResponse(
                service.setVendorStatus(
                        CurrentUser.societyId(),
                        CurrentUser.id(),
                        id,
                        com.societyapp.domain.type.VendorStatus.from(request.status())));
    }

    @DeleteMapping("/vendors/{id}")
    ConfigurationResponse.Vendor removeVendor(@PathVariable UUID id) {
        return mapper.toResponse(
                service.setVendorStatus(
                        CurrentUser.societyId(),
                        CurrentUser.id(),
                        id,
                        com.societyapp.domain.type.VendorStatus.INACTIVE));
    }

    @PostMapping("/complaint-categories")
    ConfigurationResponse.Category category(@Valid @RequestBody Category r) {
        return mapper.toResponse(
                service.addCategory(
                        CurrentUser.societyId(),
                        CurrentUser.id(),
                        new ConfigurationModels.CategoryCommand(r.name(), r.slaHours())));
    }

    @PutMapping("/complaint-categories/{id}")
    ConfigurationResponse.Category category(
            @PathVariable UUID id, @Valid @RequestBody Category request) {
        return mapper.toResponse(
                service.updateCategory(
                        CurrentUser.societyId(),
                        CurrentUser.id(),
                        id,
                        new ConfigurationModels.CategoryCommand(
                                request.name(), request.slaHours())));
    }

    @PatchMapping("/complaint-categories/{id}")
    ConfigurationResponse.Category categoryStatus(
            @PathVariable UUID id, @Valid @RequestBody Active request) {
        return mapper.toResponse(
                service.setCategoryStatus(
                        CurrentUser.societyId(), CurrentUser.id(), id, request.active()));
    }

    @DeleteMapping("/complaint-categories/{id}")
    ConfigurationResponse.Category removeCategory(@PathVariable UUID id) {
        return mapper.toResponse(
                service.setCategoryStatus(CurrentUser.societyId(), CurrentUser.id(), id, false));
    }

    @PostMapping("/charge-rules")
    ConfigurationResponse.Charge charge(@Valid @RequestBody Charge r) {
        return mapper.toResponse(
                service.addCharge(CurrentUser.societyId(), CurrentUser.id(), chargeCommand(r)));
    }

    @PutMapping("/charge-rules/{id}")
    ConfigurationResponse.Charge charge(@PathVariable UUID id, @Valid @RequestBody Charge request) {
        return mapper.toResponse(
                service.updateCharge(
                        CurrentUser.societyId(), CurrentUser.id(), id, chargeCommand(request)));
    }

    @PatchMapping("/charge-rules/{id}")
    ConfigurationResponse.Charge chargeStatus(
            @PathVariable UUID id, @Valid @RequestBody Active request) {
        return mapper.toResponse(
                service.setChargeStatus(
                        CurrentUser.societyId(), CurrentUser.id(), id, request.active()));
    }

    @DeleteMapping("/charge-rules/{id}")
    ConfigurationResponse.Charge removeCharge(@PathVariable UUID id) {
        return mapper.toResponse(
                service.setChargeStatus(CurrentUser.societyId(), CurrentUser.id(), id, false));
    }

    @PutMapping("/staff/{staffId}/permissions")
    ConfigurationResponse.Permissions permissions(
            @PathVariable UUID staffId, @Valid @RequestBody Permissions r) {
        var permissions =
                r.permissions().stream()
                        .map(StaffPermissionCode::from)
                        .collect(java.util.stream.Collectors.toUnmodifiableSet());
        return mapper.toResponse(
                service.setPermissions(
                        CurrentUser.societyId(), CurrentUser.id(), staffId, permissions));
    }

    @PostMapping("/complete")
    ConfigurationResponse.Completion complete() {
        return mapper.toResponse(service.complete(CurrentUser.societyId(), CurrentUser.id()));
    }

    private ConfigurationModels.ParkingCommand parkingCommand(Parking r) {
        return new ConfigurationModels.ParkingCommand(
                r.slotNumber(), ParkingSlotType.from(r.slotType()));
    }

    private ConfigurationModels.VendorCommand vendorCommand(Vendor r) {
        return new ConfigurationModels.VendorCommand(
                r.name(), r.serviceType(), r.contactPerson(), r.mobile(), r.email(), r.gstNumber());
    }

    private ConfigurationModels.ChargeCommand chargeCommand(Charge r) {
        return new ConfigurationModels.ChargeCommand(
                r.name(),
                ChargeCalculationType.from(r.calculationType()),
                r.amount(),
                ChargeFrequency.from(r.frequency()),
                r.effectiveFrom(),
                r.effectiveTo());
    }
}
