package com.societyapp.application.query;

import com.societyapp.application.model.platformadmin.PlatformAdminModels;
import com.societyapp.application.policy.ServiceCatalogPolicy;
import com.societyapp.application.port.in.PlatformNotificationUseCase;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public final class PlatformDashboardQuery {
    private final SocietyRepository societies;
    private final SubscriptionPlanRepository plans;
    private final UserAccountJpaRepository users;
    private final SocietyCustomPlanRepository customPlans;
    private final RequirementRequestRepository requirements;
    private final SupportTicketRepository tickets;
    private final ServiceCatalogPolicy services;
    private final PlatformNotificationUseCase notifications;

    public PlatformDashboardQuery(
            SocietyRepository societies,
            SubscriptionPlanRepository plans,
            UserAccountJpaRepository users,
            SocietyCustomPlanRepository customPlans,
            RequirementRequestRepository requirements,
            SupportTicketRepository tickets,
            ServiceCatalogPolicy services,
            PlatformNotificationUseCase notifications) {
        this.societies = societies;
        this.plans = plans;
        this.users = users;
        this.customPlans = customPlans;
        this.requirements = requirements;
        this.tickets = tickets;
        this.services = services;
        this.notifications = notifications;
    }

    public PlatformAdminModels.Dashboard get() {
        var societyList = societies.findAll();
        var customIds =
                customPlans.findAll().stream()
                        .filter(x -> LifecycleStatus.ACTIVE.matches(x.getStatus()))
                        .map(SocietyCustomPlan::getSocietyId)
                        .collect(Collectors.toSet());
        BigDecimal standard =
                societyList.stream()
                        .filter(x -> SocietyStatus.ACTIVE.matches(x.getStatus()))
                        .filter(x -> !customIds.contains(x.getId()))
                        .map(
                                x ->
                                        plans.findById(x.getPlanCode())
                                                .map(
                                                        p ->
                                                                p.getPricePerSpace()
                                                                        .multiply(
                                                                                BigDecimal.valueOf(
                                                                                        x
                                                                                                .getTotalFlats()))
                                                                        .add(
                                                                                p.getPricePerStaff()
                                                                                        .multiply(
                                                                                                BigDecimal
                                                                                                        .valueOf(
                                                                                                                x
                                                                                                                        .getPlannedStaff()))))
                                                .orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal custom =
                customPlans.findAll().stream()
                        .filter(x -> LifecycleStatus.ACTIVE.matches(x.getStatus()))
                        .map(
                                x ->
                                        societies
                                                .findById(x.getSocietyId())
                                                .map(
                                                        s ->
                                                                x.getPricePerSpace()
                                                                        .multiply(
                                                                                BigDecimal.valueOf(
                                                                                        s
                                                                                                .getTotalFlats()))
                                                                        .add(
                                                                                x.getPricePerStaff()
                                                                                        .multiply(
                                                                                                BigDecimal
                                                                                                        .valueOf(
                                                                                                                s
                                                                                                                        .getPlannedStaff()))))
                                                .orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal service =
                societyList.stream()
                        .filter(x -> SocietyStatus.ACTIVE.matches(x.getStatus()))
                        .map(x -> services.servicePricing(x).monthlyAmount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PlatformAdminModels.Dashboard(
                societyList.size(),
                count(societyList, SocietyStatus.ACTIVE),
                count(societyList, SocietyStatus.PENDING_VERIFICATION),
                users.findAll().stream()
                        .filter(x -> UserRole.RESIDENT.matches(x.getRole()))
                        .filter(x -> AccountStatus.ACTIVE.matches(x.getStatus()))
                        .count(),
                requirements.findAll().stream()
                        .filter(
                                x ->
                                        Set.of(
                                                        RequirementStatus.NEW.code(),
                                                        RequirementStatus.CONTACTED.code(),
                                                        RequirementStatus.ANALYSING.code())
                                                .contains(x.getStatus()))
                        .count(),
                tickets.findAll().stream()
                        .filter(
                                x ->
                                        Set.of(
                                                        SupportTicketStatus.OPEN.code(),
                                                        SupportTicketStatus.IN_PROGRESS.code())
                                                .contains(x.getStatus()))
                        .count(),
                notifications.failedCount(),
                standard.add(custom).add(service));
    }

    private long count(List<Society> values, SocietyStatus status) {
        return values.stream().filter(x -> status.matches(x.getStatus())).count();
    }
}
