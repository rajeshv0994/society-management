package com.societyapp.controller.mapper;

import com.societyapp.application.model.onboarding.SocietyOnboardingModels;
import com.societyapp.controller.model.SocietyOnboardingResponse;

public final class SocietyOnboardingHttpMapper {
    public SocietyOnboardingResponse.Plan toResponse(SocietyOnboardingModels.Plan value) {
        return new SocietyOnboardingResponse.Plan(
                value.code().code(),
                value.name(),
                value.approvalMode().code(),
                value.maxSpaces(),
                value.maxSpaces(),
                value.maxSpaces(),
                value.priceMonthly(),
                value.pricePerSpace(),
                value.trialDurationMonths(),
                value.trialDurationValue(),
                value.trialDurationUnit() == null ? null : value.trialDurationUnit().code(),
                value.maxStaff(),
                value.pricePerStaff());
    }

    public SocietyOnboardingResponse.ServiceModule toResponse(
            SocietyOnboardingModels.ServiceModule value) {
        return new SocietyOnboardingResponse.ServiceModule(
                value.code(),
                value.name(),
                value.description(),
                value.status().code(),
                value.sortOrder(),
                value.availabilityType().code(),
                value.pricePerSpace(),
                value.createdAt(),
                value.updatedAt());
    }

    public SocietyOnboardingResponse.ActiveSociety toResponse(
            SocietyOnboardingModels.ActiveSociety value) {
        return new SocietyOnboardingResponse.ActiveSociety(
                value.id(), value.name(), value.societyCode(), value.city(), value.state());
    }

    public SocietyOnboardingResponse.PendingSociety toResponse(
            SocietyOnboardingModels.PendingSociety value) {
        return new SocietyOnboardingResponse.PendingSociety(
                value.id(),
                value.name(),
                value.registrationNumber(),
                value.societyCode(),
                value.societyType(),
                value.totalBlocks(),
                value.totalSpaces(),
                value.totalSpaces(),
                value.totalSpaces(),
                value.approximateResidents(),
                value.plannedStaff(),
                value.address(),
                value.city(),
                value.state(),
                value.pinCode(),
                value.contactPerson(),
                value.email(),
                value.mobile(),
                value.planCode(),
                value.enabledModules(),
                value.status().code(),
                value.rejectionReason(),
                value.subscriptionStatus().code(),
                value.subscriptionStartedAt(),
                value.subscriptionExpiresAt(),
                value.subscriptionBillingCycle().code(),
                value.setupCompleted(),
                value.description(),
                value.website(),
                value.logoStorageKey(),
                value.logoContentType(),
                value.createdAt(),
                value.approvedAt(),
                value.updatedAt());
    }

    public SocietyOnboardingResponse.RegistrationResult toResponse(
            SocietyOnboardingModels.RegistrationResult value) {
        var setup = value.setupCode();
        return new SocietyOnboardingResponse.RegistrationResult(
                value.societyId(),
                value.registrationAccessToken(),
                value.status().code(),
                value.registrationNumber(),
                value.societyCode(),
                value.enabledModules(),
                value.spaceCount(),
                value.staffCount(),
                value.estimatedMonthlyPrice(),
                value.baseMonthlyPrice(),
                value.serviceMonthlyPrice(),
                value.servicePricing().stream().map(this::pricingLine).toList(),
                value.pricePerSpace(),
                value.pricePerStaff(),
                value.trialDurationValue(),
                value.trialDurationUnit() == null ? null : value.trialDurationUnit().code(),
                setup == null ? null : setup.value(),
                setup == null ? null : setup.expiresAt());
    }

    public SocietyOnboardingResponse.Approved toResponse(SocietyOnboardingModels.Approved value) {
        return new SocietyOnboardingResponse.Approved(
                value.setupCode().value(), value.setupCode().expiresAt());
    }

    public SocietyOnboardingResponse.Rejected toResponse(SocietyOnboardingModels.Rejected value) {
        return new SocietyOnboardingResponse.Rejected(
                value.societyId(), value.status().code(), value.reason());
    }

    public SocietyOnboardingResponse.Activated toResponse(SocietyOnboardingModels.Activated value) {
        return new SocietyOnboardingResponse.Activated(
                value.societyId(), value.adminUserId(), value.status().code());
    }

    private SocietyOnboardingResponse.PricingLine pricingLine(
            SocietyOnboardingModels.PricingLine value) {
        return new SocietyOnboardingResponse.PricingLine(
                value.code(),
                value.name(),
                value.availabilityType().code(),
                value.pricePerSpace(),
                value.spaces(),
                value.monthlyAmount());
    }
}
