package com.societyapp.application.service;

import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.mapper.SocietyOnboardingModelMapper;
import com.societyapp.application.model.onboarding.SocietyOnboardingModels;
import com.societyapp.application.policy.ServiceCatalogPolicy;
import com.societyapp.application.port.in.SocietyOnboardingUseCase;
import com.societyapp.application.port.out.PasswordCodec;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.usecase.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.AccountStatus;
import com.societyapp.domain.type.ApprovalMode;
import com.societyapp.domain.type.SetupCodeStatus;
import com.societyapp.domain.type.SocietyStatus;
import com.societyapp.domain.type.SubscriptionPlanCode;
import com.societyapp.domain.type.SubscriptionStatus;
import com.societyapp.domain.type.TrialDurationUnit;
import com.societyapp.domain.type.UserRole;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.*;
import java.util.*;

public class SocietyOnboardingService implements SocietyOnboardingUseCase {
    private final SocietyRepository societies;
    private final SubscriptionPlanRepository plans;
    private final SetupCodeRepository setupCodes;
    private final PlatformServiceModuleRepository serviceModules;
    private final UserAccountJpaRepository users;
    private final PasswordCodec passwords;
    private final ServiceCatalogPolicy serviceCatalog;
    private final SocietyOnboardingModelMapper modelMapper;
    private final SecureRandom random = new SecureRandom();

    public SocietyOnboardingService(
            SocietyRepository societies,
            SubscriptionPlanRepository plans,
            SetupCodeRepository setupCodes,
            PlatformServiceModuleRepository serviceModules,
            UserAccountJpaRepository users,
            PasswordCodec passwords,
            ServiceCatalogPolicy serviceCatalog) {
        this.societies = societies;
        this.plans = plans;
        this.setupCodes = setupCodes;
        this.serviceModules = serviceModules;
        this.users = users;
        this.passwords = passwords;
        this.serviceCatalog = serviceCatalog;
        this.modelMapper = new SocietyOnboardingModelMapper(serviceCatalog);
    }

    @Override
    public List<SocietyOnboardingModels.Plan> plans() {
        return plans.findAll().stream()
                .filter(
                        plan ->
                                SubscriptionPlanCode.TRIAL.matches(plan.getCode())
                                        || SubscriptionPlanCode.STANDARD.matches(plan.getCode()))
                .sorted(Comparator.comparing(SubscriptionPlan::getPricePerSpace))
                .map(modelMapper::plan)
                .toList();
    }

    @Override
    public List<SocietyOnboardingModels.ServiceModule> services() {
        return serviceModules.findAllByOrderBySortOrderAscNameAsc().stream()
                .map(modelMapper::serviceModule)
                .toList();
    }

    @Override
    public List<SocietyOnboardingModels.ActiveSociety> activeSocieties() {
        return societies.findByStatusOrderByCreatedAtDesc(SocietyStatus.ACTIVE.code()).stream()
                .sorted(Comparator.comparing(Society::getName))
                .map(
                        society ->
                                new SocietyOnboardingModels.ActiveSociety(
                                        society.getId(),
                                        society.getName(),
                                        society.getSocietyCode(),
                                        society.getCity(),
                                        society.getState()))
                .toList();
    }

    @Override
    public SocietyOnboardingModels.RegistrationResult register(
            SocietyOnboardingModels.RegistrationCommand command) {
        var enabledModules = serviceCatalog.normalizeSelection(command.enabledModules());
        var plan =
                plans.findById(command.planCode().code())
                        .orElseThrow(() -> new IllegalArgumentException("Unknown plan"));
        if (plan.getMaxFlats() != null && command.totalSpaces() > plan.getMaxFlats()) {
            throw new IllegalArgumentException(
                    "This plan supports at most " + plan.getMaxFlats() + " spaces");
        }
        if (plan.getMaxStaff() != null && command.plannedStaff() > plan.getMaxStaff()) {
            throw new IllegalArgumentException(
                    "This plan supports at most " + plan.getMaxStaff() + " staff members");
        }
        var status =
                ApprovalMode.AUTO.matches(plan.getApprovalMode())
                        ? SocietyStatus.APPROVED
                        : SocietyStatus.PENDING_VERIFICATION;
        String accessToken = randomPart(32);
        var now = OffsetDateTime.now();
        var society = new Society();
        society.setName(command.name());
        society.setRegistrationNumber(generateRegistrationNumber());
        society.setSocietyCode(generateSocietyCode(command.name()));
        society.setSocietyType(command.societyType());
        society.setTotalBlocks(command.totalBlocks());
        society.setTotalFlats(command.totalSpaces());
        society.setPlannedStaff(command.plannedStaff());
        society.setApproximateResidents(command.approximateResidents());
        society.setAddress(command.address());
        society.setCity(command.city());
        society.setState(command.state());
        society.setPinCode(command.pinCode());
        society.setContactPerson(command.contactPerson());
        society.setEmail(command.email());
        society.setMobile(command.mobile());
        society.setPlanCode(plan.getCode());
        society.setEnabledModules(serviceCatalog.writeSelection(enabledModules));
        society.setStatus(status.code());
        society.setApprovedAt(status == SocietyStatus.APPROVED ? now : null);
        society.setRegistrationAccessHash(passwords.encode(accessToken));
        society.setSubscriptionStatus(SubscriptionStatus.PENDING.code());
        society.setSetupCompleted(false);
        society.setCreatedAt(now);
        society.setUpdatedAt(now);
        society = societies.save(society);
        var spacePrice =
                plan.getPricePerSpace().multiply(BigDecimal.valueOf(command.totalSpaces()));
        var staffPrice =
                plan.getPricePerStaff().multiply(BigDecimal.valueOf(command.plannedStaff()));
        var servicePricing = serviceCatalog.servicePricing(society);
        var trial = SubscriptionPlanCode.TRIAL.matches(plan.getCode());
        return new SocietyOnboardingModels.RegistrationResult(
                society.getId(),
                accessToken,
                status,
                society.getRegistrationNumber(),
                society.getSocietyCode(),
                enabledModules,
                command.totalSpaces(),
                command.plannedStaff(),
                spacePrice.add(staffPrice).add(servicePricing.monthlyAmount()),
                spacePrice.add(staffPrice),
                servicePricing.monthlyAmount(),
                servicePricing.lines().stream().map(modelMapper::pricingLine).toList(),
                plan.getPricePerSpace(),
                plan.getPricePerStaff(),
                trial ? plan.getTrialDurationValue() : null,
                trial ? TrialDurationUnit.from(plan.getTrialDurationUnit()) : null,
                status == SocietyStatus.APPROVED ? generateCode(society.getId()) : null);
    }

    @Override
    public List<SocietyOnboardingModels.PendingSociety> pending() {
        return societies
                .findByStatusOrderByCreatedAtDesc(SocietyStatus.PENDING_VERIFICATION.code())
                .stream()
                .map(modelMapper::pendingSociety)
                .toList();
    }

    @Override
    public SocietyOnboardingModels.Approved approve(UUID societyId) {
        var society =
                societies
                        .findById(societyId)
                        .filter(
                                item ->
                                        SocietyStatus.PENDING_VERIFICATION.matches(
                                                item.getStatus()))
                        .orElseThrow(() -> new IllegalStateException("Society is not pending"));
        society.setStatus(SocietyStatus.APPROVED.code());
        society.setApprovedAt(OffsetDateTime.now());
        society.setUpdatedAt(OffsetDateTime.now());
        societies.save(society);
        return new SocietyOnboardingModels.Approved(generateCode(societyId));
    }

    @Override
    public SocietyOnboardingModels.Rejected reject(UUID societyId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        var society =
                societies
                        .findById(societyId)
                        .filter(
                                item ->
                                        SocietyStatus.PENDING_VERIFICATION.matches(
                                                item.getStatus()))
                        .orElseThrow(() -> new IllegalStateException("Society is not pending"));
        society.setStatus(SocietyStatus.REJECTED.code());
        society.setRejectionReason(reason);
        society.setUpdatedAt(OffsetDateTime.now());
        societies.save(society);
        return new SocietyOnboardingModels.Rejected(societyId, SocietyStatus.REJECTED, reason);
    }

    @Override
    public SocietyOnboardingModels.Activated activate(
            SocietyOnboardingModels.ActivationCommand command) {
        var code =
                setupCodes.findByStatusOrderByCreatedAtDesc(SetupCodeStatus.ACTIVE.code()).stream()
                        .filter(item -> item.getCodeHint().equals(hint(command.setupCode())))
                        .filter(item -> passwords.matches(command.setupCode(), item.getCodeHash()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Invalid setup code"));
        if (code.getExpiresAt().isBefore(OffsetDateTime.now())
                || code.getActivationCount() >= code.getMaxActivations()) {
            throw new IllegalStateException("Setup code expired or already used");
        }
        var society =
                societies
                        .findById(code.getSocietyId())
                        .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
        var user = new UserAccount();
        user.setSocietyId(society.getId());
        user.setFullName(command.fullName());
        user.setEmail(command.email());
        user.setMobile(command.mobile());
        user.setPasswordHash(passwords.encode(command.password()));
        user.setRole(UserRole.SOCIETY_ADMIN.code());
        user.setStatus(AccountStatus.ACTIVE.code());
        user.setEmailVerified(true);
        user.setMobileVerified(true);
        user.setCreatedAt(OffsetDateTime.now());
        user = users.save(user);
        code.setActivationCount(code.getActivationCount() + 1);
        if (code.getActivationCount() >= code.getMaxActivations()) {
            code.setStatus(SetupCodeStatus.USED.code());
        }
        setupCodes.save(code);
        society.setStatus(SocietyStatus.ACTIVE.code());
        var plan =
                plans.findById(society.getPlanCode())
                        .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
        var subscriptionStartedAt = OffsetDateTime.now();
        society.setSubscriptionStartedAt(subscriptionStartedAt);
        if (SubscriptionPlanCode.TRIAL.matches(plan.getCode())) {
            society.setSubscriptionStatus(SubscriptionStatus.TRIAL.code());
            society.setSubscriptionExpiresAt(
                    TrialDurationUnit.DAYS.matches(plan.getTrialDurationUnit())
                            ? subscriptionStartedAt.plusDays(plan.getTrialDurationValue())
                            : subscriptionStartedAt.plusMonths(plan.getTrialDurationValue()));
        } else {
            society.setSubscriptionStatus(SubscriptionStatus.PENDING_PAYMENT.code());
            society.setSubscriptionExpiresAt(null);
        }
        society.setUpdatedAt(OffsetDateTime.now());
        societies.save(society);
        return new SocietyOnboardingModels.Activated(
                society.getId(), user.getId(), SocietyStatus.ACTIVE);
    }

    private SocietyOnboardingModels.SetupCode generateCode(UUID societyId) {
        String raw =
                "SOC-"
                        + Year.now().getValue()
                        + "-"
                        + randomPart(4)
                        + "-"
                        + (1000 + random.nextInt(9000));
        var expiresAt = OffsetDateTime.now().plusDays(7);
        var code = new SetupCode();
        code.setSocietyId(societyId);
        code.setCodeHash(passwords.encode(raw));
        code.setCodeHint(hint(raw));
        code.setExpiresAt(expiresAt);
        code.setMaxActivations(1);
        code.setActivationCount(0);
        code.setStatus(SetupCodeStatus.ACTIVE.code());
        code.setCreatedAt(OffsetDateTime.now());
        setupCodes.save(code);
        return new SocietyOnboardingModels.SetupCode(raw, expiresAt);
    }

    private String randomPart(int size) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        var value = new StringBuilder();
        for (int i = 0; i < size; i++) {
            value.append(chars.charAt(random.nextInt(chars.length())));
        }
        return value.toString();
    }

    private String hint(String value) {
        return value.length() < 4 ? value : value.substring(value.length() - 4);
    }

    private String generateSocietyCode(String name) {
        String base =
                name.toUpperCase(Locale.ROOT)
                        .replaceAll("[^A-Z0-9]+", "-")
                        .replaceAll("(^-|-$)", "");
        if (base.isBlank()) base = "SOCIETY";
        if (base.length() > 20) base = base.substring(0, 20);
        String candidate = base;
        while (societies.existsBySocietyCodeIgnoreCase(candidate)) {
            candidate = base + "-" + randomPart(4);
        }
        return candidate;
    }

    private String generateRegistrationNumber() {
        String candidate;
        do {
            candidate = "REG-" + Year.now().getValue() + "-" + randomPart(6);
        } while (societies.existsByRegistrationNumberIgnoreCase(candidate));
        return candidate;
    }
}
