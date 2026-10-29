package com.societyapp.application.service;

import com.societyapp.application.exception.ConflictException;
import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.model.subscription.SubscriptionQuantityModels;
import com.societyapp.application.policy.ServiceCatalogPolicy;
import com.societyapp.application.port.in.SubscriptionQuantityUseCase;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.usecase.*;
import com.societyapp.domain.model.SocietyCustomPlan;
import com.societyapp.domain.type.AccountStatus;
import com.societyapp.domain.type.BillingCycle;
import com.societyapp.domain.type.DomainCode;
import com.societyapp.domain.type.LifecycleStatus;
import com.societyapp.domain.type.PricingModel;
import com.societyapp.domain.type.SubscriptionStatus;
import com.societyapp.domain.type.UserRole;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

public class SubscriptionQuantityService implements SubscriptionQuantityUseCase {
    private final SocietyRepository societies;
    private final SubscriptionPlanRepository plans;
    private final SocietyCustomPlanRepository customPlans;
    private final BlockRepository blocks;
    private final FloorRepository sections;
    private final FlatRepository spaces;
    private final UserAccountJpaRepository users;
    private final ServiceCatalogPolicy serviceCatalog;

    public SubscriptionQuantityService(
            SocietyRepository societies,
            SubscriptionPlanRepository plans,
            SocietyCustomPlanRepository customPlans,
            BlockRepository blocks,
            FloorRepository sections,
            FlatRepository spaces,
            UserAccountJpaRepository users,
            ServiceCatalogPolicy serviceCatalog) {
        this.societies = societies;
        this.plans = plans;
        this.customPlans = customPlans;
        this.blocks = blocks;
        this.sections = sections;
        this.spaces = spaces;
        this.users = users;
        this.serviceCatalog = serviceCatalog;
    }

    @Override
    public SubscriptionQuantityModels.View get(UUID societyId) {
        var society =
                societies
                        .findById(societyId)
                        .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
        var pricing = pricing(societyId, society.getPlanCode());
        long configuredSpaces = configuredSpaces(societyId);
        long activeStaff = activeStaff(societyId);
        BigDecimal baseMonthly =
                pricing.fixedMonthlyPrice()
                        .add(
                                pricing.pricePerSpace()
                                        .multiply(BigDecimal.valueOf(society.getTotalFlats()))
                                        .add(
                                                pricing.pricePerStaff()
                                                        .multiply(
                                                                BigDecimal.valueOf(
                                                                        society
                                                                                .getPlannedStaff()))));
        var servicePricing = serviceCatalog.servicePricing(society);
        BigDecimal monthly = baseMonthly.add(servicePricing.monthlyAmount());
        var billingCycle =
                BillingCycle.from(
                        society.getSubscriptionBillingCycle() == null
                                ? BillingCycle.MONTHLY.code()
                                : society.getSubscriptionBillingCycle());
        int cycleMonths = cycleMonths(billingCycle.code());
        var breakdown = new ArrayList<SubscriptionQuantityModels.PricingLine>();
        if (pricing.fixedMonthlyPrice().signum() > 0) {
            breakdown.add(
                    new SubscriptionQuantityModels.PricingLine(
                            PricingModel.FIXED_BASE.code(),
                            "Fixed custom plan",
                            1,
                            null,
                            pricing.fixedMonthlyPrice(),
                            null,
                            pricing.fixedMonthlyPrice()));
        }
        breakdown.add(
                new SubscriptionQuantityModels.PricingLine(
                        "BASE_SPACE",
                        "Base space subscription",
                        society.getTotalFlats(),
                        null,
                        pricing.pricePerSpace(),
                        null,
                        pricing.pricePerSpace()
                                .multiply(BigDecimal.valueOf(society.getTotalFlats()))));
        breakdown.add(
                new SubscriptionQuantityModels.PricingLine(
                        "BASE_STAFF",
                        "Staff seats",
                        society.getPlannedStaff(),
                        null,
                        pricing.pricePerStaff(),
                        null,
                        pricing.pricePerStaff()
                                .multiply(BigDecimal.valueOf(society.getPlannedStaff()))));
        servicePricing.lines().stream().map(this::pricingLine).forEach(breakdown::add);
        var catalog = serviceCatalog.catalogFor(society).stream().map(this::serviceItem).toList();
        return new SubscriptionQuantityModels.View(
                society.getPlanCode(),
                society.getTotalFlats(),
                society.getPlannedStaff(),
                configuredSpaces,
                activeStaff,
                pricing.pricePerSpace(),
                pricing.pricePerStaff(),
                pricing.fixedMonthlyPrice(),
                pricing.fixedMonthlyPrice().signum() > 0
                        ? PricingModel.FIXED_CUSTOM
                        : PricingModel.USAGE_BASED,
                pricing.maxSpaces(),
                pricing.maxStaff(),
                baseMonthly,
                servicePricing.monthlyAmount(),
                monthly,
                billingCycle,
                cycleMonths,
                monthly.multiply(BigDecimal.valueOf(cycleMonths)),
                DomainCode.parse(
                        SubscriptionStatus.class,
                        society.getSubscriptionStatus(),
                        "subscription status"),
                society.getSubscriptionExpiresAt(),
                serviceCatalog.selected(society),
                catalog,
                breakdown);
    }

    @Override
    public SubscriptionQuantityModels.View update(
            UUID societyId, SubscriptionQuantityModels.UpdateCommand command) {
        var society =
                societies
                        .findById(societyId)
                        .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
        long configuredSpaces = configuredSpaces(societyId);
        long activeStaff = activeStaff(societyId);
        if (command.totalSpaces() < configuredSpaces) {
            throw new ConflictException(
                    "Selected spaces cannot be below the "
                            + configuredSpaces
                            + " spaces currently configured.");
        }
        if (command.totalStaff() < activeStaff) {
            throw new ConflictException(
                    "Selected staff cannot be below the "
                            + activeStaff
                            + " active staff accounts.");
        }
        var pricing = pricing(societyId, society.getPlanCode());
        if (pricing.maxSpaces() != null && command.totalSpaces() > pricing.maxSpaces()) {
            throw new ConflictException(
                    "This plan allows a maximum of " + pricing.maxSpaces() + " spaces.");
        }
        if (pricing.maxStaff() != null && command.totalStaff() > pricing.maxStaff()) {
            throw new ConflictException(
                    "This plan allows a maximum of " + pricing.maxStaff() + " staff.");
        }
        society.setTotalFlats(command.totalSpaces());
        society.setPlannedStaff(command.totalStaff());
        society.setSubscriptionBillingCycle(command.billingCycle().code());
        if (command.enabledModules() != null) {
            society.setEnabledModules(
                    serviceCatalog.writeSelection(
                            serviceCatalog.normalizeSelection(command.enabledModules())));
        }
        society.setUpdatedAt(OffsetDateTime.now());
        societies.save(society);
        return get(societyId);
    }

    private SubscriptionQuantityModels.PricingLine pricingLine(
            ServiceCatalogPolicy.PricingLine source) {
        return new SubscriptionQuantityModels.PricingLine(
                source.code(),
                source.name(),
                null,
                source.spaces(),
                null,
                source.pricePerSpace(),
                source.monthlyAmount());
    }

    private SubscriptionQuantityModels.ServiceItem serviceItem(
            ServiceCatalogPolicy.CatalogItem source) {
        return new SubscriptionQuantityModels.ServiceItem(
                source.code(),
                source.name(),
                source.description(),
                source.status(),
                source.availabilityType(),
                source.pricePerSpace(),
                source.selected(),
                source.required(),
                source.monthlyAmount());
    }

    private int cycleMonths(String cycle) {
        return switch (BillingCycle.from(cycle == null ? BillingCycle.MONTHLY.code() : cycle)) {
            case QUARTERLY -> 3;
            case YEARLY -> 12;
            case MONTHLY -> 1;
        };
    }

    private long configuredSpaces(UUID societyId) {
        return blocks.findBySocietyIdOrderByName(societyId).stream()
                .flatMap(block -> sections.findByBlockIdOrderByFloorNumber(block.getId()).stream())
                .mapToLong(section -> spaces.findByFloorIdOrderByFlatNumber(section.getId()).size())
                .sum();
    }

    private long activeStaff(UUID societyId) {
        return users
                .findBySocietyIdAndRoleOrderByFullName(societyId, UserRole.STAFF.code())
                .stream()
                .filter(user -> AccountStatus.ACTIVE.matches(user.getStatus()))
                .count();
    }

    private Pricing pricing(UUID societyId, String planCode) {
        Optional<SocietyCustomPlan> custom =
                customPlans.findAllByOrderByCreatedAtDesc().stream()
                        .filter(plan -> societyId.equals(plan.getSocietyId()))
                        .filter(plan -> LifecycleStatus.ACTIVE.matches(plan.getStatus()))
                        .findFirst();
        if (custom.isPresent()) {
            var plan = custom.get();
            var fixedMonthly =
                    plan.getPricePerSpace().signum() == 0 && plan.getPricePerStaff().signum() == 0
                            ? plan.getPriceMonthly()
                            : BigDecimal.ZERO;
            return new Pricing(
                    plan.getPricePerSpace(),
                    plan.getPricePerStaff(),
                    fixedMonthly,
                    plan.getMaxFlats(),
                    plan.getMaxStaff());
        }
        var plan =
                plans.findById(planCode)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Subscription plan not found"));
        return new Pricing(
                plan.getPricePerSpace(),
                plan.getPricePerStaff(),
                BigDecimal.ZERO,
                plan.getMaxFlats(),
                plan.getMaxStaff());
    }

    private record Pricing(
            BigDecimal pricePerSpace,
            BigDecimal pricePerStaff,
            BigDecimal fixedMonthlyPrice,
            Integer maxSpaces,
            Integer maxStaff) {}
}
