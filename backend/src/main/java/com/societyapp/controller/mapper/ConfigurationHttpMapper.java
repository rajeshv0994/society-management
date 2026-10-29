package com.societyapp.controller.mapper;

import com.societyapp.application.model.configuration.ConfigurationModels;
import com.societyapp.controller.model.ConfigurationResponse;
import java.util.stream.Collectors;

public final class ConfigurationHttpMapper {
    public ConfigurationResponse.Parking toResponse(ConfigurationModels.ParkingView v) {
        return new ConfigurationResponse.Parking(
                v.id(),
                v.societyId(),
                v.slotNumber(),
                v.slotType().code(),
                v.status().code(),
                v.spaceId(),
                v.vehicleNumber(),
                v.createdAt(),
                v.updatedAt());
    }

    public ConfigurationResponse.Vendor toResponse(ConfigurationModels.VendorView v) {
        return new ConfigurationResponse.Vendor(
                v.id(),
                v.societyId(),
                v.name(),
                v.serviceType(),
                v.contactPerson(),
                v.mobile(),
                v.email(),
                v.gstNumber(),
                v.status().code(),
                v.createdAt(),
                v.updatedAt());
    }

    public ConfigurationResponse.Category toResponse(ConfigurationModels.CategoryView v) {
        return new ConfigurationResponse.Category(
                v.id(),
                v.societyId(),
                v.name(),
                v.slaHours(),
                v.active(),
                v.createdAt(),
                v.updatedAt());
    }

    public ConfigurationResponse.Charge toResponse(ConfigurationModels.ChargeView v) {
        return new ConfigurationResponse.Charge(
                v.id(),
                v.societyId(),
                v.name(),
                v.calculationType().code(),
                v.amount(),
                v.frequency().code(),
                v.effectiveFrom(),
                v.effectiveTo(),
                v.active(),
                v.createdAt(),
                v.updatedAt());
    }

    public ConfigurationResponse.Setup toResponse(ConfigurationModels.Setup v) {
        return new ConfigurationResponse.Setup(
                v.ready(),
                v.setupCompleted(),
                v.propertyGroups(),
                v.spaces(),
                v.checklist().stream()
                        .map(
                                x ->
                                        new ConfigurationResponse.SetupCheck(
                                                x.code(), x.label(), x.complete(), x.required()))
                        .toList());
    }

    public ConfigurationResponse.Configuration toResponse(ConfigurationModels.ConfigurationView v) {
        return new ConfigurationResponse.Configuration(
                v.parking().stream().map(this::toResponse).toList(),
                v.vendors().stream().map(this::toResponse).toList(),
                v.complaintCategories().stream().map(this::toResponse).toList(),
                v.chargeRules().stream().map(this::toResponse).toList(),
                v.spaces().stream()
                        .map(
                                x ->
                                        new ConfigurationResponse.Space(
                                                x.id(),
                                                x.label(),
                                                x.spaceNumber(),
                                                x.occupancyStatus()))
                        .toList(),
                new ConfigurationResponse.Summary(
                        v.summary().parkingTotal(),
                        v.summary().parkingAvailable(),
                        v.summary().parkingAllocated(),
                        v.summary().activeVendors(),
                        v.summary().activeCategories(),
                        v.summary().activeChargeRules()),
                toResponse(v.setup()),
                v.enabledModules(),
                v.staffPermissionOptions().stream()
                        .map(
                                x ->
                                        new ConfigurationResponse.PermissionOption(
                                                x.code().code(),
                                                x.label(),
                                                x.requiredModule().code()))
                        .toList());
    }

    public ConfigurationResponse.Reference toResponse(ConfigurationModels.ReferenceView v) {
        return new ConfigurationResponse.Reference(
                v.parking().stream().map(this::toResponse).toList(),
                v.vendors().stream().map(this::toResponse).toList(),
                v.complaintCategories().stream().map(this::toResponse).toList(),
                v.chargeRules().stream().map(this::toResponse).toList(),
                v.enabledModules(),
                v.viewerRole().code(),
                v.viewerId());
    }

    public ConfigurationResponse.Permissions toResponse(ConfigurationModels.PermissionsView v) {
        return new ConfigurationResponse.Permissions(
                v.staffId(),
                v.permissions().stream()
                        .map(code -> code.code())
                        .collect(Collectors.toUnmodifiableSet()));
    }

    public ConfigurationResponse.Completion toResponse(ConfigurationModels.Completion v) {
        return new ConfigurationResponse.Completion(v.setupCompleted(), toResponse(v.setup()));
    }
}
