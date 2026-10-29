package com.societyapp.controller.mapper;

import com.societyapp.application.model.subscription.SubscriptionQuantityModels;
import com.societyapp.controller.model.SubscriptionQuantityResponse;

public final class SubscriptionQuantityHttpMapper {
    public SubscriptionQuantityResponse toResponse(SubscriptionQuantityModels.View view) {
        return new SubscriptionQuantityResponse(
                view.planCode(),
                view.selectedSpaces(),
                view.selectedStaff(),
                view.configuredSpaces(),
                view.activeStaff(),
                view.pricePerSpace(),
                view.pricePerStaff(),
                view.fixedMonthlyPrice(),
                view.pricingModel().code(),
                view.maxSpaces(),
                view.maxStaff(),
                view.baseMonthlyPrice(),
                view.serviceMonthlyPrice(),
                view.estimatedMonthlyPrice(),
                view.billingCycle().code(),
                view.billingCycleMonths(),
                view.billingCycleAmount(),
                view.subscriptionStatus().code(),
                view.subscriptionExpiresAt(),
                view.enabledModules(),
                view.serviceCatalog().stream().map(this::toServiceItem).toList(),
                view.pricingBreakdown().stream().map(this::toPricingLine).toList());
    }

    private SubscriptionQuantityResponse.PricingLine toPricingLine(
            SubscriptionQuantityModels.PricingLine line) {
        return new SubscriptionQuantityResponse.PricingLine(
                line.code(),
                line.name(),
                line.quantity(),
                line.spaces(),
                line.rate(),
                line.pricePerSpace(),
                line.monthlyAmount());
    }

    private SubscriptionQuantityResponse.ServiceItem toServiceItem(
            SubscriptionQuantityModels.ServiceItem item) {
        return new SubscriptionQuantityResponse.ServiceItem(
                item.code(),
                item.name(),
                item.description(),
                item.status().code(),
                item.availabilityType().code(),
                item.pricePerSpace(),
                item.selected(),
                item.required(),
                item.monthlyAmount());
    }
}
