package com.societyapp.application.model.onboarding;

import com.societyapp.domain.type.ApprovalMode;
import com.societyapp.domain.type.BillingCycle;
import com.societyapp.domain.type.PlatformServiceStatus;
import com.societyapp.domain.type.ServiceAvailabilityType;
import com.societyapp.domain.type.SocietyStatus;
import com.societyapp.domain.type.SubscriptionPlanCode;
import com.societyapp.domain.type.SubscriptionStatus;
import com.societyapp.domain.type.TrialDurationUnit;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class SocietyOnboardingModels {
    private SocietyOnboardingModels() {}

    public record RegistrationCommand(
            String name,
            String societyType,
            int totalBlocks,
            int totalSpaces,
            int plannedStaff,
            Integer approximateResidents,
            String address,
            String city,
            String state,
            String pinCode,
            String contactPerson,
            String email,
            String mobile,
            SubscriptionPlanCode planCode,
            List<String> enabledModules) {
        public RegistrationCommand {
            enabledModules = List.copyOf(enabledModules);
        }
    }

    public record ActivationCommand(
            String setupCode, String fullName, String email, String password, String mobile) {}

    public record Plan(
            SubscriptionPlanCode code,
            String name,
            ApprovalMode approvalMode,
            Integer maxSpaces,
            BigDecimal priceMonthly,
            BigDecimal pricePerSpace,
            Integer trialDurationMonths,
            Integer trialDurationValue,
            TrialDurationUnit trialDurationUnit,
            Integer maxStaff,
            BigDecimal pricePerStaff) {}

    public record ServiceModule(
            String code,
            String name,
            String description,
            PlatformServiceStatus status,
            int sortOrder,
            ServiceAvailabilityType availabilityType,
            BigDecimal pricePerSpace,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {}

    public record ActiveSociety(
            UUID id, String name, String societyCode, String city, String state) {}

    public record PendingSociety(
            UUID id,
            String name,
            String registrationNumber,
            String societyCode,
            String societyType,
            int totalBlocks,
            int totalSpaces,
            Integer approximateResidents,
            int plannedStaff,
            String address,
            String city,
            String state,
            String pinCode,
            String contactPerson,
            String email,
            String mobile,
            String planCode,
            List<String> enabledModules,
            SocietyStatus status,
            String rejectionReason,
            SubscriptionStatus subscriptionStatus,
            OffsetDateTime subscriptionStartedAt,
            OffsetDateTime subscriptionExpiresAt,
            BillingCycle subscriptionBillingCycle,
            boolean setupCompleted,
            String description,
            String website,
            String logoStorageKey,
            String logoContentType,
            OffsetDateTime createdAt,
            OffsetDateTime approvedAt,
            OffsetDateTime updatedAt) {}

    public record PricingLine(
            String code,
            String name,
            ServiceAvailabilityType availabilityType,
            BigDecimal pricePerSpace,
            int spaces,
            BigDecimal monthlyAmount) {}

    public record SetupCode(String value, OffsetDateTime expiresAt) {}

    public record RegistrationResult(
            UUID societyId,
            String registrationAccessToken,
            SocietyStatus status,
            String registrationNumber,
            String societyCode,
            List<String> enabledModules,
            int spaceCount,
            int staffCount,
            BigDecimal estimatedMonthlyPrice,
            BigDecimal baseMonthlyPrice,
            BigDecimal serviceMonthlyPrice,
            List<PricingLine> servicePricing,
            BigDecimal pricePerSpace,
            BigDecimal pricePerStaff,
            Integer trialDurationValue,
            TrialDurationUnit trialDurationUnit,
            SetupCode setupCode) {}

    public record Approved(SetupCode setupCode) {}

    public record Rejected(UUID societyId, SocietyStatus status, String reason) {}

    public record Activated(UUID societyId, UUID adminUserId, SocietyStatus status) {}
}
