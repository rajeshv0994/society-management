package com.societyapp.application.command;

import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.model.platformadmin.PlatformAdminModels;
import com.societyapp.application.policy.ServiceCatalogPolicy;
import com.societyapp.application.port.out.*;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.security.SecureRandom;
import java.time.*;
import java.util.*;

public final class PlatformSocietyCoordinator {
    private final SocietyRepository societies;
    private final SubscriptionPlanRepository plans;
    private final SetupCodeRepository setupCodes;
    private final AuditEventRepository audits;
    private final PasswordCodec passwords;
    private final JsonCodec json;
    private final ServiceCatalogPolicy services;
    private final SecureRandom random = new SecureRandom();

    public PlatformSocietyCoordinator(
            SocietyRepository societies,
            SubscriptionPlanRepository plans,
            SetupCodeRepository setupCodes,
            AuditEventRepository audits,
            PasswordCodec passwords,
            JsonCodec json,
            ServiceCatalogPolicy services) {
        this.societies = societies;
        this.plans = plans;
        this.setupCodes = setupCodes;
        this.audits = audits;
        this.passwords = passwords;
        this.json = json;
        this.services = services;
    }

    public PlatformAdminModels.SocietyCreated create(
            UUID actor, PlatformAdminModels.CreateSocietyCommand command) {
        var modules = services.normalizeSelection(command.enabledModules());
        String planCode = command.planCode().code();
        var plan =
                plans.findById(planCode)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Subscription plan not found"));
        validate(plan, command.totalFlats(), command.plannedStaff());
        var now = OffsetDateTime.now();
        var society = new Society();
        society.setName(command.name());
        society.setRegistrationNumber(registration());
        society.setSocietyCode(code(command.name()));
        society.setSocietyType(command.societyType());
        society.setTotalBlocks(command.totalBlocks());
        society.setTotalFlats(command.totalFlats());
        society.setPlannedStaff(command.plannedStaff());
        society.setAddress(command.address());
        society.setCity(command.city());
        society.setState(command.state());
        society.setPinCode(command.pinCode());
        society.setContactPerson(command.contactPerson());
        society.setEmail(command.email());
        society.setMobile(command.mobile());
        society.setPlanCode(planCode);
        society.setEnabledModules(services.writeSelection(modules));
        society.setStatus(SocietyStatus.APPROVED.code());
        society.setSubscriptionStatus(SubscriptionStatus.PENDING.code());
        society.setApprovedAt(now);
        society.setCreatedAt(now);
        society.setUpdatedAt(now);
        society = societies.save(society);
        UUID id = society.getId();
        String setupCode = setupCode(id);
        var expires =
                setupCodes.findByStatusOrderByCreatedAtDesc(SetupCodeStatus.ACTIVE.code()).stream()
                        .filter(x -> x.getSocietyId().equals(id))
                        .findFirst()
                        .orElseThrow()
                        .getExpiresAt();
        audit(actor, "SOCIETY_CREATED", "SOCIETY", id.toString(), command.name());
        return new PlatformAdminModels.SocietyCreated(
                id,
                SocietyStatus.APPROVED,
                society.getRegistrationNumber(),
                modules,
                setupCode,
                expires);
    }

    public PlatformAdminModels.SocietyUpdated update(
            UUID actor, UUID id, PlatformAdminModels.UpdateSocietyCommand command) {
        var modules = services.normalizeSelection(command.enabledModules());
        String planCode = command.planCode().code();
        var plan =
                plans.findById(planCode)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Subscription plan not found"));
        var society =
                societies
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
        validate(plan, society.getTotalFlats(), command.plannedStaff());
        society.setName(command.name());
        society.setDescription(command.description());
        society.setWebsite(command.website());
        society.setContactPerson(command.contactPerson());
        society.setEmail(command.email());
        society.setMobile(command.mobile());
        society.setAddress(command.address());
        society.setCity(command.city());
        society.setState(command.state());
        society.setPinCode(command.pinCode());
        society.setPlanCode(planCode);
        society.setPlannedStaff(command.plannedStaff());
        society.setEnabledModules(services.writeSelection(modules));
        society.setStatus(command.status().code());
        society.setUpdatedAt(OffsetDateTime.now());
        societies.save(society);
        audit(actor, "SOCIETY_UPDATED", "SOCIETY", id.toString(), command.name());
        return new PlatformAdminModels.SocietyUpdated(
                id, command.status(), command.planCode(), modules, true);
    }

    private String setupCode(UUID societyId) {
        String raw =
                "SOC-"
                        + Year.now().getValue()
                        + "-"
                        + random(4)
                        + "-"
                        + (1000 + random.nextInt(9000));
        var setup = new SetupCode();
        setup.setSocietyId(societyId);
        setup.setCodeHash(passwords.encode(raw));
        setup.setCodeHint(raw.substring(raw.length() - 4));
        setup.setExpiresAt(OffsetDateTime.now().plusDays(7));
        setup.setMaxActivations(1);
        setup.setActivationCount(0);
        setup.setStatus(SetupCodeStatus.ACTIVE.code());
        setup.setCreatedAt(OffsetDateTime.now());
        setupCodes.save(setup);
        return raw;
    }

    private void validate(SubscriptionPlan plan, int spaces, int staff) {
        if (plan.getMaxFlats() != null && spaces > plan.getMaxFlats())
            throw new IllegalArgumentException(
                    "Plan allows at most " + plan.getMaxFlats() + " spaces");
        if (plan.getMaxStaff() != null && staff > plan.getMaxStaff())
            throw new IllegalArgumentException(
                    "Plan allows at most " + plan.getMaxStaff() + " staff members");
    }

    private String code(String name) {
        String base =
                name.toUpperCase(Locale.ROOT)
                        .replaceAll("[^A-Z0-9]+", "-")
                        .replaceAll("(^-|-$)", "");
        if (base.isBlank()) base = "SOCIETY";
        if (base.length() > 20) base = base.substring(0, 20);
        String candidate = base;
        while (societies.existsBySocietyCodeIgnoreCase(candidate))
            candidate = base + "-" + random(4);
        return candidate;
    }

    private String registration() {
        String candidate;
        do {
            candidate = "REG-" + Year.now().getValue() + "-" + random(6);
        } while (societies.existsByRegistrationNumberIgnoreCase(candidate));
        return candidate;
    }

    private String random(int size) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        var out = new StringBuilder();
        for (int i = 0; i < size; i++) out.append(chars.charAt(random.nextInt(chars.length())));
        return out.toString();
    }

    private void audit(UUID actor, String action, String type, String id, String name) {
        var event = new AuditEvent();
        event.setActorUserId(actor);
        event.setAction(action);
        event.setEntityType(type);
        event.setEntityId(id);
        try {
            event.setDetails(json.write(Map.of("name", name)));
        } catch (JsonCodecException e) {
            throw new IllegalStateException("Could not serialize audit details", e);
        }
        event.setOccurredAt(OffsetDateTime.now());
        audits.save(event);
    }
}
