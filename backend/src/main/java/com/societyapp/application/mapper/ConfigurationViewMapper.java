package com.societyapp.application.mapper;

import com.societyapp.application.model.configuration.ConfigurationModels;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;

public final class ConfigurationViewMapper {
    public ConfigurationModels.ParkingView parking(ParkingSlot value) {
        return new ConfigurationModels.ParkingView(
                value.getId(),
                value.getSocietyId(),
                value.getSlotNumber(),
                ParkingSlotType.from(value.getSlotType()),
                ParkingSlotStatus.from(value.getStatus()),
                value.getFlatId(),
                value.getVehicleNumber(),
                value.getCreatedAt(),
                value.getUpdatedAt());
    }

    public ConfigurationModels.VendorView vendor(Vendor value) {
        return new ConfigurationModels.VendorView(
                value.getId(),
                value.getSocietyId(),
                value.getName(),
                value.getServiceType(),
                value.getContactPerson(),
                value.getMobile(),
                value.getEmail(),
                value.getGstNumber(),
                VendorStatus.from(value.getStatus()),
                value.getCreatedAt(),
                value.getUpdatedAt());
    }

    public ConfigurationModels.CategoryView category(ComplaintCategory value) {
        return new ConfigurationModels.CategoryView(
                value.getId(),
                value.getSocietyId(),
                value.getName(),
                value.getSlaHours(),
                value.isActive(),
                value.getCreatedAt(),
                value.getUpdatedAt());
    }

    public ConfigurationModels.ChargeView charge(MaintenanceChargeRule value) {
        return new ConfigurationModels.ChargeView(
                value.getId(),
                value.getSocietyId(),
                value.getName(),
                ChargeCalculationType.from(value.getCalculationType()),
                value.getAmount(),
                ChargeFrequency.from(value.getFrequency()),
                value.getEffectiveFrom(),
                value.getEffectiveTo(),
                value.isActive(),
                value.getCreatedAt(),
                value.getUpdatedAt());
    }
}
