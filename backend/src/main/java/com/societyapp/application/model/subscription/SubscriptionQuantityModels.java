package com.societyapp.application.model.subscription;

import com.societyapp.domain.type.BillingCycle;
import com.societyapp.domain.type.PlatformServiceStatus;
import com.societyapp.domain.type.PricingModel;
import com.societyapp.domain.type.ServiceAvailabilityType;
import com.societyapp.domain.type.SubscriptionStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public final class SubscriptionQuantityModels {
    private SubscriptionQuantityModels() {}

    public record UpdateCommand(
            int totalSpaces,
            int totalStaff,
            BillingCycle billingCycle,
            List<String> enabledModules) {
        public UpdateCommand {
            enabledModules = enabledModules == null ? null : List.copyOf(enabledModules);
        }
    }

    public record PricingLine(
            String code,
            String name,
            Integer quantity,
            Integer spaces,
            BigDecimal rate,
            BigDecimal pricePerSpace,
            BigDecimal monthlyAmount) {}

    public record ServiceItem(
            String code,
            String name,
            String description,
            PlatformServiceStatus status,
            ServiceAvailabilityType availabilityType,
            BigDecimal pricePerSpace,
            boolean selected,
            boolean required,
            BigDecimal monthlyAmount) {}

    public record View(
            String planCode,
            int selectedSpaces,
            int selectedStaff,
            long configuredSpaces,
            long activeStaff,
            BigDecimal pricePerSpace,
            BigDecimal pricePerStaff,
            BigDecimal fixedMonthlyPrice,
            PricingModel pricingModel,
            Integer maxSpaces,
            Integer maxStaff,
            BigDecimal baseMonthlyPrice,
            BigDecimal serviceMonthlyPrice,
            BigDecimal estimatedMonthlyPrice,
            BillingCycle billingCycle,
            int billingCycleMonths,
            BigDecimal billingCycleAmount,
            SubscriptionStatus subscriptionStatus,
            OffsetDateTime subscriptionExpiresAt,
            List<String> enabledModules,
            List<ServiceItem> serviceCatalog,
            List<PricingLine> pricingBreakdown) {
        public View {
            enabledModules = List.copyOf(enabledModules);
            serviceCatalog = List.copyOf(serviceCatalog);
            pricingBreakdown = List.copyOf(pricingBreakdown);
        }
    }
}
