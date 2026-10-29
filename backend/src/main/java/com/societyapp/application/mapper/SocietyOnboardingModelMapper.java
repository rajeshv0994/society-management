package com.societyapp.application.mapper;

import com.societyapp.application.model.onboarding.SocietyOnboardingModels;
import com.societyapp.application.policy.ServiceCatalogPolicy;
import com.societyapp.domain.model.PlatformServiceModule;
import com.societyapp.domain.model.Society;
import com.societyapp.domain.model.SubscriptionPlan;
import com.societyapp.domain.type.*;

public final class SocietyOnboardingModelMapper {
    private final ServiceCatalogPolicy serviceCatalog;

    public SocietyOnboardingModelMapper(ServiceCatalogPolicy serviceCatalog) {
        this.serviceCatalog = serviceCatalog;
    }

    public SocietyOnboardingModels.Plan plan(SubscriptionPlan value) {
        return new SocietyOnboardingModels.Plan(
                SubscriptionPlanCode.from(value.getCode()),
                value.getName(),
                ApprovalMode.from(value.getApprovalMode()),
                value.getMaxFlats(),
                value.getPriceMonthly(),
                value.getPricePerSpace(),
                value.getTrialDurationMonths(),
                value.getTrialDurationValue(),
                value.getTrialDurationUnit() == null
                        ? null
                        : TrialDurationUnit.from(value.getTrialDurationUnit()),
                value.getMaxStaff(),
                value.getPricePerStaff());
    }

    public SocietyOnboardingModels.ServiceModule serviceModule(PlatformServiceModule value) {
        return new SocietyOnboardingModels.ServiceModule(
                value.getCode(),
                value.getName(),
                value.getDescription(),
                PlatformServiceStatus.from(value.getStatus()),
                value.getSortOrder(),
                ServiceAvailabilityType.from(value.getAvailabilityType()),
                value.getPricePerSpace(),
                value.getCreatedAt(),
                value.getUpdatedAt());
    }

    public SocietyOnboardingModels.PendingSociety pendingSociety(Society value) {
        return new SocietyOnboardingModels.PendingSociety(
                value.getId(),
                value.getName(),
                value.getRegistrationNumber(),
                value.getSocietyCode(),
                value.getSocietyType(),
                value.getTotalBlocks(),
                value.getTotalFlats(),
                value.getApproximateResidents(),
                value.getPlannedStaff(),
                value.getAddress(),
                value.getCity(),
                value.getState(),
                value.getPinCode(),
                value.getContactPerson(),
                value.getEmail(),
                value.getMobile(),
                value.getPlanCode(),
                serviceCatalog.selected(value),
                SocietyStatus.from(value.getStatus()),
                value.getRejectionReason(),
                SubscriptionStatus.from(value.getSubscriptionStatus()),
                value.getSubscriptionStartedAt(),
                value.getSubscriptionExpiresAt(),
                BillingCycle.from(value.getSubscriptionBillingCycle()),
                value.isSetupCompleted(),
                value.getDescription(),
                value.getWebsite(),
                value.getLogoStorageKey(),
                value.getLogoContentType(),
                value.getCreatedAt(),
                value.getApprovedAt(),
                value.getUpdatedAt());
    }

    public SocietyOnboardingModels.PricingLine pricingLine(ServiceCatalogPolicy.PricingLine value) {
        return new SocietyOnboardingModels.PricingLine(
                value.code(),
                value.name(),
                value.availabilityType(),
                value.pricePerSpace(),
                value.spaces(),
                value.monthlyAmount());
    }
}
