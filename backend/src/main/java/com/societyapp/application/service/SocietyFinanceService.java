package com.societyapp.application.service;

import com.societyapp.application.model.finance.SocietyFinanceModels;
import com.societyapp.application.port.in.SocietyFinanceUseCase;
import com.societyapp.application.port.in.StructureAccessUseCase;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.usecase.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.ExpenseCategory;
import com.societyapp.domain.type.FinanceFrequency;
import com.societyapp.domain.type.MemberChargeType;
import com.societyapp.domain.type.PaymentMethod;
import com.societyapp.domain.type.PaymentStatus;
import com.societyapp.domain.type.UserRole;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class SocietyFinanceService implements SocietyFinanceUseCase {
    private static final Set<PaymentMethod> PAYMENT_METHODS =
            Set.of(
                    PaymentMethod.CASH,
                    PaymentMethod.CHEQUE,
                    PaymentMethod.BANK_TRANSFER,
                    PaymentMethod.UPI,
                    PaymentMethod.CARD);

    private final SocietyExpenseRepository expenses;
    private final MemberChargeSettingRepository settings;
    private final SocietyBudgetRepository budgets;
    private final SocietyBudgetAdditionRepository budgetAdditions;
    private final PaymentRepository memberPayments;
    private final StaffSalaryPaymentRepository salaryPayments;
    private final StaffProfileRepository staffProfiles;
    private final UserAccountJpaRepository users;
    private final FlatRepository flats;
    private final StructureAccessUseCase access;

    public SocietyFinanceService(
            SocietyExpenseRepository expenses,
            MemberChargeSettingRepository settings,
            SocietyBudgetRepository budgets,
            SocietyBudgetAdditionRepository budgetAdditions,
            PaymentRepository memberPayments,
            StaffSalaryPaymentRepository salaryPayments,
            StaffProfileRepository staffProfiles,
            UserAccountJpaRepository users,
            FlatRepository flats,
            StructureAccessUseCase access) {
        this.expenses = expenses;
        this.settings = settings;
        this.budgets = budgets;
        this.budgetAdditions = budgetAdditions;
        this.memberPayments = memberPayments;
        this.salaryPayments = salaryPayments;
        this.staffProfiles = staffProfiles;
        this.users = users;
        this.flats = flats;
        this.access = access;
    }

    @Override
    public List<SocietyFinanceModels.ExpenseView> expenses(SocietyFinanceModels.Actor actor) {
        return expenses
                .findBySocietyIdOrderByExpenseDateDescCreatedAtDesc(actor.societyId())
                .stream()
                .map(this::expenseView)
                .toList();
    }

    @Override
    public SocietyFinanceModels.ExpenseCreated createExpense(
            SocietyFinanceModels.Actor actor, SocietyFinanceModels.ExpenseCommand command) {
        var requestedCategory = command.category();
        var requestedMethod = command.paymentMethod();
        if (!PAYMENT_METHODS.contains(requestedMethod))
            throw new IllegalArgumentException("Invalid payment method");
        if (Set.of(PaymentMethod.CHEQUE, PaymentMethod.BANK_TRANSFER).contains(requestedMethod)
                && (command.paymentReference() == null || command.paymentReference().isBlank())) {
            throw new IllegalArgumentException("Payment reference is required");
        }
        var item = new SocietyExpense();
        item.setSocietyId(actor.societyId());
        item.setCategory(requestedCategory.code());
        item.setVendor(blankToNull(command.vendor()));
        item.setDescription(command.description().trim());
        item.setAmount(command.amount());
        item.setExpenseDate(command.expenseDate());
        item.setPaymentMethod(requestedMethod.code());
        item.setPaymentReference(blankToNull(command.paymentReference()));
        item.setCreatedBy(actor.userId());
        item.setCreatedAt(OffsetDateTime.now());
        item = expenses.save(item);
        return new SocietyFinanceModels.ExpenseCreated(item.getId(), item.getAmount());
    }

    @Override
    public List<SocietyFinanceModels.ChargeSettingView> chargeSettings(
            SocietyFinanceModels.Actor actor) {
        return settings.findBySocietyIdOrderByName(actor.societyId()).stream()
                .filter(MemberChargeSetting::isActive)
                .filter(
                        item ->
                                actor.role() != UserRole.RESIDENT
                                        || item.getFlatId() == null
                                        || access.occupiedBy(item.getFlatId(), actor.userId()))
                .map(this::settingView)
                .toList();
    }

    @Override
    public SocietyFinanceModels.ChargeSettingCreated createChargeSetting(
            SocietyFinanceModels.Actor actor, SocietyFinanceModels.ChargeSettingCommand command) {
        if (command.spaceId() != null) access.requireFlat(command.spaceId(), actor.societyId());
        var item = new MemberChargeSetting();
        item.setSocietyId(actor.societyId());
        item.setFlatId(command.spaceId());
        item.setName(command.name().trim());
        item.setChargeType(command.chargeType().code());
        item.setAmount(command.amount());
        item.setFrequency(command.frequency().code());
        item.setDescription(blankToNull(command.description()));
        item.setActive(true);
        item.setCreatedAt(OffsetDateTime.now());
        item = settings.save(item);
        return new SocietyFinanceModels.ChargeSettingCreated(item.getId(), item.getName());
    }

    @Override
    public SocietyFinanceModels.FinanceSummary financeSummary(
            SocietyFinanceModels.Actor actor, int year) {
        UUID societyId = actor.societyId();
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);
        BigDecimal expenseTotal =
                expenses.findBySocietyIdOrderByExpenseDateDescCreatedAtDesc(societyId).stream()
                        .filter(
                                item ->
                                        !item.getExpenseDate().isBefore(from)
                                                && !item.getExpenseDate().isAfter(to))
                        .map(SocietyExpense::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal salaryTotal =
                salaryPayments
                        .findBySocietyIdAndPaidOnBetweenOrderByPaidOnDesc(societyId, from, to)
                        .stream()
                        .map(StaffSalaryPayment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal received =
                memberPayments.findBySocietyIdOrderByPaidAtDesc(societyId).stream()
                        .filter(
                                payment ->
                                        !payment.getPaidAt().toLocalDate().isBefore(from)
                                                && !payment.getPaidAt().toLocalDate().isAfter(to)
                                                && PaymentStatus.SUCCESS.matches(
                                                        payment.getStatus()))
                        .map(Payment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        var additions =
                budgetAdditions.findBySocietyIdAndBudgetYearOrderByAddedAtDesc(societyId, year);
        BigDecimal manualBudget =
                additions.stream()
                        .map(SocietyBudgetAddition::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal budget = received.add(manualBudget);
        BigDecimal spent = expenseTotal.add(salaryTotal);
        BigDecimal balance = budget.subtract(spent);
        return new SocietyFinanceModels.FinanceSummary(
                year,
                budget,
                received,
                manualBudget,
                expenseTotal,
                salaryTotal,
                spent,
                balance.max(BigDecimal.ZERO),
                balance.signum() < 0 ? balance.abs() : BigDecimal.ZERO,
                additions.stream()
                        .map(
                                item ->
                                        new SocietyFinanceModels.BudgetAddition(
                                                item.getId(),
                                                item.getAmount(),
                                                item.getNote(),
                                                item.getAddedAt()))
                        .toList());
    }

    @Override
    public SocietyFinanceModels.BudgetResult addBudget(
            SocietyFinanceModels.Actor actor, SocietyFinanceModels.AddBudgetCommand command) {
        var addition = new SocietyBudgetAddition();
        addition.setSocietyId(actor.societyId());
        addition.setBudgetYear(command.year());
        addition.setAmount(command.amount());
        addition.setNote(blankToNull(command.note()));
        addition.setAddedBy(actor.userId());
        addition.setAddedAt(OffsetDateTime.now());
        addition = budgetAdditions.save(addition);
        return new SocietyFinanceModels.BudgetResult(addition.getId(), null, addition.getAmount());
    }

    @Override
    public SocietyFinanceModels.BudgetResult setBudget(
            SocietyFinanceModels.Actor actor, SocietyFinanceModels.SetBudgetCommand command) {
        var budget =
                budgets.findBySocietyIdAndBudgetYear(actor.societyId(), command.year())
                        .orElseGet(SocietyBudget::new);
        if (budget.getId() == null) {
            budget.setSocietyId(actor.societyId());
            budget.setBudgetYear(command.year());
        }
        budget.setAmount(command.amount());
        budget.setUpdatedBy(actor.userId());
        budget.setUpdatedAt(OffsetDateTime.now());
        budget = budgets.save(budget);
        return new SocietyFinanceModels.BudgetResult(null, command.year(), budget.getAmount());
    }

    @Override
    public List<SocietyFinanceModels.StaffSalary> staffSalaries(SocietyFinanceModels.Actor actor) {
        return users
                .findBySocietyIdAndRoleOrderByFullName(actor.societyId(), UserRole.STAFF.code())
                .stream()
                .map(
                        user -> {
                            var profile = staffProfiles.findById(user.getId()).orElse(null);
                            var history =
                                    salaryPayments
                                            .findByStaffUserIdOrderBySalaryMonthDesc(user.getId())
                                            .stream()
                                            .map(
                                                    payment ->
                                                            new SocietyFinanceModels.SalaryPayment(
                                                                    payment.getId(),
                                                                    payment.getSalaryMonth(),
                                                                    payment.getAmount(),
                                                                    payment.getPaidOn(),
                                                                    PaymentMethod.from(
                                                                            payment
                                                                                    .getPaymentMethod()),
                                                                    payment.getReferenceNumber()))
                                            .toList();
                            return new SocietyFinanceModels.StaffSalary(
                                    user.getId(),
                                    user.getFullName(),
                                    profile == null ? null : profile.getStaffType(),
                                    profile == null ? null : profile.getMonthlySalary(),
                                    history);
                        })
                .toList();
    }

    private SocietyFinanceModels.ExpenseView expenseView(SocietyExpense item) {
        return new SocietyFinanceModels.ExpenseView(
                item.getId(),
                ExpenseCategory.from(item.getCategory()),
                item.getVendor(),
                item.getDescription(),
                item.getAmount(),
                item.getExpenseDate(),
                PaymentMethod.from(item.getPaymentMethod()),
                item.getPaymentReference(),
                item.getCreatedAt());
    }

    private SocietyFinanceModels.ChargeSettingView settingView(MemberChargeSetting item) {
        String spaceNumber = null;
        String residentName = null;
        if (item.getFlatId() != null) {
            var flat = flats.findById(item.getFlatId()).orElse(null);
            if (flat != null) {
                spaceNumber = flat.getFlatNumber();
                UUID residentId =
                        flat.getActiveTenantId() != null
                                ? flat.getActiveTenantId()
                                : flat.getActiveOwnerId();
                residentName =
                        residentId == null
                                ? null
                                : users.findById(residentId)
                                        .map(UserAccount::getFullName)
                                        .orElse(null);
            }
        }
        return new SocietyFinanceModels.ChargeSettingView(
                item.getId(),
                item.getFlatId(),
                item.getName(),
                MemberChargeType.from(item.getChargeType()),
                item.getAmount(),
                FinanceFrequency.from(item.getFrequency()),
                item.getDescription(),
                spaceNumber,
                residentName);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
