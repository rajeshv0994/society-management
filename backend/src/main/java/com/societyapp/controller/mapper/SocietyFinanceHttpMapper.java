package com.societyapp.controller.mapper;

import com.societyapp.application.model.finance.SocietyFinanceModels;
import com.societyapp.controller.model.SocietyFinanceResponse;

public final class SocietyFinanceHttpMapper {
    public SocietyFinanceResponse.Expense toResponse(SocietyFinanceModels.ExpenseView value) {
        return new SocietyFinanceResponse.Expense(
                value.id(),
                value.category().code(),
                value.vendor(),
                value.description(),
                value.amount(),
                value.expenseDate(),
                value.paymentMethod().code(),
                value.paymentReference(),
                value.createdAt());
    }

    public SocietyFinanceResponse.ExpenseCreated toResponse(
            SocietyFinanceModels.ExpenseCreated value) {
        return new SocietyFinanceResponse.ExpenseCreated(value.id(), value.amount());
    }

    public SocietyFinanceResponse.ChargeSetting toResponse(
            SocietyFinanceModels.ChargeSettingView value) {
        return new SocietyFinanceResponse.ChargeSetting(
                value.id(),
                value.spaceId(),
                value.name(),
                value.chargeType().code(),
                value.amount(),
                value.frequency().code(),
                value.description(),
                value.spaceNumber(),
                value.residentName());
    }

    public SocietyFinanceResponse.ChargeSettingCreated toResponse(
            SocietyFinanceModels.ChargeSettingCreated value) {
        return new SocietyFinanceResponse.ChargeSettingCreated(value.id(), value.name());
    }

    public SocietyFinanceResponse.FinanceSummary toResponse(
            SocietyFinanceModels.FinanceSummary value) {
        return new SocietyFinanceResponse.FinanceSummary(
                value.year(),
                value.totalBudget(),
                value.receivedPayments(),
                value.manualBudget(),
                value.expenseSpent(),
                value.salarySpent(),
                value.totalSpent(),
                value.remaining(),
                value.overBudget(),
                value.budgetAdditions().stream()
                        .map(
                                item ->
                                        new SocietyFinanceResponse.BudgetAddition(
                                                item.id(),
                                                item.amount(),
                                                item.note(),
                                                item.addedAt()))
                        .toList());
    }

    public SocietyFinanceResponse.BudgetResult toResponse(SocietyFinanceModels.BudgetResult value) {
        return new SocietyFinanceResponse.BudgetResult(value.id(), value.year(), value.amount());
    }

    public SocietyFinanceResponse.StaffSalary toResponse(SocietyFinanceModels.StaffSalary value) {
        return new SocietyFinanceResponse.StaffSalary(
                value.staffId(),
                value.staffName(),
                value.staffType(),
                value.monthlySalary(),
                value.payments().stream()
                        .map(
                                item ->
                                        new SocietyFinanceResponse.SalaryPayment(
                                                item.id(),
                                                item.salaryMonth(),
                                                item.amount(),
                                                item.paidOn(),
                                                item.paymentMethod().code(),
                                                item.referenceNumber()))
                        .toList());
    }
}
