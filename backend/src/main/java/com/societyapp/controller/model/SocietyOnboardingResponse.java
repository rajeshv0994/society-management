package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class SocietyOnboardingResponse {
    private SocietyOnboardingResponse() {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Plan(
            String code,
            String name,
            @JsonProperty("approval_mode") String approvalMode,
            @JsonProperty("max_flats") Integer maxFlats,
            @JsonProperty("max_units") Integer maxUnits,
            @JsonProperty("max_spaces") Integer maxSpaces,
            @JsonProperty("price_monthly") BigDecimal priceMonthly,
            @JsonProperty("price_per_space") BigDecimal pricePerSpace,
            @JsonProperty("trial_duration_months") Integer trialDurationMonths,
            @JsonProperty("trial_duration_value") Integer trialDurationValue,
            @JsonProperty("trial_duration_unit") String trialDurationUnit,
            @JsonProperty("max_staff") Integer maxStaff,
            @JsonProperty("price_per_staff") BigDecimal pricePerStaff) {}

    public record ServiceModule(
            String code,
            String name,
            String description,
            String status,
            @JsonProperty("sort_order") int sortOrder,
            @JsonProperty("availability_type") String availabilityType,
            @JsonProperty("price_per_space") BigDecimal pricePerSpace,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("updated_at") OffsetDateTime updatedAt) {}

    public record ActiveSociety(
            UUID id,
            String name,
            @JsonProperty("society_code") String societyCode,
            String city,
            String state) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PendingSociety(
            UUID id,
            String name,
            @JsonProperty("registration_number") String registrationNumber,
            @JsonProperty("society_code") String societyCode,
            @JsonProperty("society_type") String societyType,
            @JsonProperty("total_blocks") int totalBlocks,
            @JsonProperty("total_flats") int totalFlats,
            @JsonProperty("total_units") int totalUnits,
            @JsonProperty("total_spaces") int totalSpaces,
            @JsonProperty("approximate_residents") Integer approximateResidents,
            @JsonProperty("planned_staff") int plannedStaff,
            String address,
            String city,
            String state,
            @JsonProperty("pin_code") String pinCode,
            @JsonProperty("contact_person") String contactPerson,
            String email,
            String mobile,
            @JsonProperty("plan_code") String planCode,
            @JsonProperty("enabled_modules") List<String> enabledModules,
            String status,
            @JsonProperty("rejection_reason") String rejectionReason,
            @JsonProperty("subscription_status") String subscriptionStatus,
            @JsonProperty("subscription_started_at") OffsetDateTime subscriptionStartedAt,
            @JsonProperty("subscription_expires_at") OffsetDateTime subscriptionExpiresAt,
            @JsonProperty("subscription_billing_cycle") String subscriptionBillingCycle,
            @JsonProperty("setup_completed") boolean setupCompleted,
            String description,
            String website,
            @JsonProperty("logo_storage_key") String logoStorageKey,
            @JsonProperty("logo_content_type") String logoContentType,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("approved_at") OffsetDateTime approvedAt,
            @JsonProperty("updated_at") OffsetDateTime updatedAt) {}

    public record PricingLine(
            String code,
            String name,
            @JsonProperty("availability_type") String availabilityType,
            @JsonProperty("price_per_space") BigDecimal pricePerSpace,
            int spaces,
            @JsonProperty("monthly_amount") BigDecimal monthlyAmount) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RegistrationResult(
            UUID societyId,
            String registrationAccessToken,
            String status,
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
            String trialDurationUnit,
            String setupCode,
            OffsetDateTime expiresAt) {}

    public record Approved(String setupCode, OffsetDateTime expiresAt) {}

    public record Rejected(UUID societyId, String status, String reason) {}

    public record Activated(UUID societyId, UUID adminUserId, String status) {}
}
