package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record SubscriptionQuantityResponse(
        @JsonProperty("plan_code") String planCode,
        @JsonProperty("selected_spaces") int selectedSpaces,
        @JsonProperty("selected_staff") int selectedStaff,
        @JsonProperty("configured_spaces") long configuredSpaces,
        @JsonProperty("active_staff") long activeStaff,
        @JsonProperty("price_per_space") BigDecimal pricePerSpace,
        @JsonProperty("price_per_staff") BigDecimal pricePerStaff,
        @JsonProperty("fixed_monthly_price") BigDecimal fixedMonthlyPrice,
        @JsonProperty("pricing_model") String pricingModel,
        @JsonProperty("max_spaces") Integer maxSpaces,
        @JsonProperty("max_staff") Integer maxStaff,
        @JsonProperty("base_monthly_price") BigDecimal baseMonthlyPrice,
        @JsonProperty("service_monthly_price") BigDecimal serviceMonthlyPrice,
        @JsonProperty("estimated_monthly_price") BigDecimal estimatedMonthlyPrice,
        @JsonProperty("billing_cycle") String billingCycle,
        @JsonProperty("billing_cycle_months") int billingCycleMonths,
        @JsonProperty("billing_cycle_amount") BigDecimal billingCycleAmount,
        @JsonProperty("subscription_status") String subscriptionStatus,
        @JsonProperty("subscription_expires_at") OffsetDateTime subscriptionExpiresAt,
        @JsonProperty("enabled_modules") List<String> enabledModules,
        @JsonProperty("service_catalog") List<ServiceItem> serviceCatalog,
        @JsonProperty("pricing_breakdown") List<PricingLine> pricingBreakdown) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PricingLine(
            String code,
            String name,
            Integer quantity,
            Integer spaces,
            BigDecimal rate,
            @JsonProperty("price_per_space") BigDecimal pricePerSpace,
            @JsonProperty("monthly_amount") BigDecimal monthlyAmount) {}

    public record ServiceItem(
            String code,
            String name,
            String description,
            String status,
            @JsonProperty("availability_type") String availabilityType,
            @JsonProperty("price_per_space") BigDecimal pricePerSpace,
            boolean selected,
            boolean required,
            @JsonProperty("monthly_amount") BigDecimal monthlyAmount) {}
}
