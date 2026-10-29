package com.societyapp.application.command;

import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.model.platformadmin.PlatformAdminModels;
import com.societyapp.application.policy.ServiceCatalogPolicy;
import com.societyapp.application.port.out.*;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

public final class PlatformCatalogCoordinator {
    private final SocietyRepository societies;
    private final SubscriptionPlanRepository plans;
    private final PlatformServiceModuleRepository serviceModules;
    private final AuditEventRepository audits;
    private final JsonCodec json;
    private final ServiceCatalogPolicy serviceCatalog;

    public PlatformCatalogCoordinator(
            SocietyRepository societies,
            SubscriptionPlanRepository plans,
            PlatformServiceModuleRepository serviceModules,
            AuditEventRepository audits,
            JsonCodec json,
            ServiceCatalogPolicy serviceCatalog) {
        this.societies = societies;
        this.plans = plans;
        this.serviceModules = serviceModules;
        this.audits = audits;
        this.json = json;
        this.serviceCatalog = serviceCatalog;
    }

    public List<PlatformAdminModels.PlanView> plans() {
        return plans.findAll().stream()
                .filter(
                        plan ->
                                SubscriptionPlanCode.TRIAL.matches(plan.getCode())
                                        || SubscriptionPlanCode.STANDARD.matches(plan.getCode()))
                .sorted(Comparator.comparing(SubscriptionPlan::getPricePerSpace))
                .map(
                        plan ->
                                planView(
                                        plan,
                                        societies.findAll().stream()
                                                .filter(
                                                        society ->
                                                                plan.getCode()
                                                                        .equals(
                                                                                society
                                                                                        .getPlanCode()))
                                                .count()))
                .toList();
    }

    public List<PlatformAdminModels.ServiceView> services() {
        return serviceModules.findAllByOrderBySortOrderAscNameAsc().stream()
                .map(
                        service -> {
                            var selectedSocieties =
                                    societies.findAll().stream()
                                            .filter(
                                                    society ->
                                                            serviceCatalog
                                                                    .selected(society)
                                                                    .contains(service.getCode()))
                                            .toList();
                            var revenue =
                                    ServiceAvailabilityType.PAID_PER_SPACE.matches(
                                                    service.getAvailabilityType())
                                            ? selectedSocieties.stream()
                                                    .map(
                                                            society ->
                                                                    service.getPricePerSpace()
                                                                            .multiply(
                                                                                    BigDecimal
                                                                                            .valueOf(
                                                                                                    society
                                                                                                            .getTotalFlats())))
                                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                                            : BigDecimal.ZERO;
                            return serviceView(service, selectedSocieties.size(), revenue);
                        })
                .toList();
    }

    public PlatformAdminModels.ServiceView createService(
            UUID actorId, PlatformAdminModels.ServiceCommand command) {
        String code = command.code();
        String name = command.name();
        String normalizedCode = code.trim().toUpperCase(Locale.ROOT);
        if (serviceModules.existsById(normalizedCode)) {
            throw new IllegalArgumentException("Service code already exists");
        }
        var module = new PlatformServiceModule();
        module.setCode(normalizedCode);
        module.setName(name.trim());
        module.setDescription(command.description());
        applyServiceCommercialPolicy(
                module, command.status(), command.availabilityType(), command.pricePerSpace());
        module.setSortOrder(command.sortOrder());
        module.setCreatedAt(OffsetDateTime.now());
        module.setUpdatedAt(OffsetDateTime.now());
        serviceModules.save(module);
        synchronizeServiceAvailability(module);
        audit(actorId, "SERVICE_CREATED", "PLATFORM_SERVICE", normalizedCode, name);
        return serviceView(module, 0, BigDecimal.ZERO);
    }

    public PlatformAdminModels.ServiceView updateService(
            UUID actorId, String code, PlatformAdminModels.ServiceCommand command) {
        String name = command.name();
        String normalizedCode = code.trim().toUpperCase(Locale.ROOT);
        var module =
                serviceModules
                        .findById(normalizedCode)
                        .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        module.setName(name.trim());
        module.setDescription(command.description());
        applyServiceCommercialPolicy(
                module, command.status(), command.availabilityType(), command.pricePerSpace());
        module.setSortOrder(command.sortOrder());
        module.setUpdatedAt(OffsetDateTime.now());
        serviceModules.save(module);
        synchronizeServiceAvailability(module);
        audit(actorId, "SERVICE_UPDATED", "PLATFORM_SERVICE", normalizedCode, name);
        return services().stream()
                .filter(value -> value.code().equals(normalizedCode))
                .findFirst()
                .orElseThrow();
    }

    public PlatformAdminModels.PlanUpdated updatePlan(
            UUID actorId, SubscriptionPlanCode code, PlatformAdminModels.PlanCommand command) {
        var plan =
                plans.findById(code.code())
                        .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
        if (command.name() != null && !command.name().isBlank()) {
            plan.setName(command.name());
        }
        plan.setApprovalMode(command.approvalMode().code());
        plan.setMaxFlats(command.maxFlats());
        plan.setPricePerSpace(command.pricePerSpace());
        plan.setMaxStaff(command.maxStaff());
        plan.setPricePerStaff(command.pricePerStaff());
        plan.setPriceMonthly(BigDecimal.ZERO);
        int duration =
                command.trialDurationValue() != null
                        ? command.trialDurationValue()
                        : command.trialDurationMonths() != null ? command.trialDurationMonths() : 1;
        var requestedDurationUnit =
                command.trialDurationUnit() != null
                        ? command.trialDurationUnit()
                        : command.trialDurationMonths() != null
                                ? TrialDurationUnit.MONTHS
                                : TrialDurationUnit.from(plan.getTrialDurationUnit());
        plan.setTrialDurationValue(duration);
        plan.setTrialDurationUnit(requestedDurationUnit.code());
        plan.setTrialDurationMonths(
                requestedDurationUnit == TrialDurationUnit.MONTHS ? duration : 1);
        plans.save(plan);
        audit(actorId, "PLAN_UPDATED", "SUBSCRIPTION_PLAN", code.code(), "Billing rates updated");
        return new PlatformAdminModels.PlanUpdated(code.code(), true);
    }

    private void applyServiceCommercialPolicy(
            PlatformServiceModule module,
            PlatformServiceStatus requestedStatus,
            ServiceAvailabilityType requestedType,
            BigDecimal pricePerSpace) {
        if (requestedType == ServiceAvailabilityType.DEFAULT_FREE
                && requestedStatus != PlatformServiceStatus.ACTIVE) {
            throw new IllegalArgumentException("A default service must remain active");
        }
        var rate = pricePerSpace == null ? BigDecimal.ZERO : pricePerSpace;
        if (requestedType == ServiceAvailabilityType.PAID_PER_SPACE) {
            if (rate.signum() <= 0) {
                throw new IllegalArgumentException(
                        "A paid service requires a positive per-space price");
            }
        } else if (rate.signum() != 0) {
            throw new IllegalArgumentException("Free services must have a zero price");
        }
        module.setStatus(requestedStatus.code());
        module.setAvailabilityType(requestedType.code());
        module.setPricePerSpace(rate);
    }

    private PlatformAdminModels.PlanView planView(SubscriptionPlan plan, long count) {
        return new PlatformAdminModels.PlanView(
                plan.getCode(),
                plan.getName(),
                ApprovalMode.from(plan.getApprovalMode()),
                plan.getMaxFlats(),
                plan.getPricePerSpace(),
                plan.getMaxStaff(),
                plan.getPricePerStaff(),
                plan.getTrialDurationValue(),
                TrialDurationUnit.from(plan.getTrialDurationUnit()),
                plan.getTrialDurationMonths(),
                count);
    }

    private PlatformAdminModels.ServiceView serviceView(
            PlatformServiceModule service, int count, BigDecimal revenue) {
        return new PlatformAdminModels.ServiceView(
                service.getCode(),
                service.getName(),
                service.getDescription(),
                PlatformServiceStatus.from(service.getStatus()),
                service.getSortOrder(),
                ServiceAvailabilityType.from(service.getAvailabilityType()),
                service.getPricePerSpace(),
                service.getCreatedAt(),
                service.getUpdatedAt(),
                count,
                revenue);
    }

    private void synchronizeServiceAvailability(PlatformServiceModule module) {
        boolean addDefault =
                PlatformServiceStatus.ACTIVE.matches(module.getStatus())
                        && ServiceAvailabilityType.DEFAULT_FREE.matches(
                                module.getAvailabilityType());
        boolean removeUnavailable = !PlatformServiceStatus.ACTIVE.matches(module.getStatus());
        if (!addDefault && !removeUnavailable) {
            return;
        }
        for (var society : societies.findAll()) {
            var selected = new LinkedHashSet<>(serviceCatalog.selected(society));
            if (addDefault) selected.add(module.getCode());
            if (removeUnavailable) selected.remove(module.getCode());
            society.setEnabledModules(
                    serviceCatalog.writeSelection(
                            serviceModules.findAllByOrderBySortOrderAscNameAsc().stream()
                                    .map(PlatformServiceModule::getCode)
                                    .filter(selected::contains)
                                    .toList()));
            society.setUpdatedAt(OffsetDateTime.now());
            societies.save(society);
        }
    }

    private void audit(UUID actorId, String action, String type, String entityId, String name) {
        var event = new AuditEvent();
        event.setActorUserId(actorId);
        event.setAction(action);
        event.setEntityType(type);
        event.setEntityId(entityId);
        try {
            event.setDetails(json.write(Map.of("name", name)));
        } catch (JsonCodecException exception) {
            throw new IllegalStateException("Could not serialize audit details", exception);
        }
        event.setOccurredAt(OffsetDateTime.now());
        audits.save(event);
    }
}
