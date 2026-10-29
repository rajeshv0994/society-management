package com.societyapp.application.port.in;

import com.societyapp.application.model.configuration.ConfigurationModels;
import com.societyapp.domain.type.*;
import java.util.Set;
import java.util.UUID;

public interface ConfigurationUseCase {
    ConfigurationModels.ConfigurationView get(UUID societyId);

    ConfigurationModels.ReferenceView reference(UUID societyId, UUID userId, UserRole role);

    ConfigurationModels.ParkingView addParking(
            UUID societyId, UUID actorId, ConfigurationModels.ParkingCommand command);

    ConfigurationModels.ParkingView updateParking(
            UUID societyId, UUID actorId, UUID id, ConfigurationModels.ParkingCommand command);

    ConfigurationModels.ParkingView parkingAction(
            UUID societyId,
            UUID actorId,
            UUID id,
            ConfigurationModels.ParkingActionCommand command);

    ConfigurationModels.VendorView addVendor(
            UUID societyId, UUID actorId, ConfigurationModels.VendorCommand command);

    ConfigurationModels.VendorView updateVendor(
            UUID societyId, UUID actorId, UUID id, ConfigurationModels.VendorCommand command);

    ConfigurationModels.VendorView setVendorStatus(
            UUID societyId, UUID actorId, UUID id, VendorStatus status);

    ConfigurationModels.CategoryView addCategory(
            UUID societyId, UUID actorId, ConfigurationModels.CategoryCommand command);

    ConfigurationModels.CategoryView updateCategory(
            UUID societyId, UUID actorId, UUID id, ConfigurationModels.CategoryCommand command);

    ConfigurationModels.CategoryView setCategoryStatus(
            UUID societyId, UUID actorId, UUID id, boolean active);

    ConfigurationModels.ChargeView addCharge(
            UUID societyId, UUID actorId, ConfigurationModels.ChargeCommand command);

    ConfigurationModels.ChargeView updateCharge(
            UUID societyId, UUID actorId, UUID id, ConfigurationModels.ChargeCommand command);

    ConfigurationModels.ChargeView setChargeStatus(
            UUID societyId, UUID actorId, UUID id, boolean active);

    ConfigurationModels.PermissionsView setPermissions(
            UUID societyId, UUID granterId, UUID staffId, Set<StaffPermissionCode> codes);

    ConfigurationModels.Completion complete(UUID societyId, UUID actorId);
}
