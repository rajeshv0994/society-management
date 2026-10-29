package com.societyapp.application.usecase;

import com.societyapp.application.policy.StaffPermissionCatalog;
import com.societyapp.application.port.in.ServiceEntitlementUseCase;
import com.societyapp.application.port.out.persistence.SocietyRepository;
import com.societyapp.domain.type.BuiltInServiceCode;
import java.util.UUID;

/** Application use case for feature-module access decisions. */
public class ServiceEntitlementService implements ServiceEntitlementUseCase {
    private final SocietyRepository societies;

    public ServiceEntitlementService(SocietyRepository societies) {
        this.societies = societies;
    }

    @Override
    public boolean isEnabled(UUID societyId, BuiltInServiceCode serviceCode) {
        return societies
                .findById(societyId)
                .map(
                        society ->
                                StaffPermissionCatalog.moduleEnabled(
                                        society.getEnabledModules(), serviceCode.code()))
                .orElse(false);
    }
}
