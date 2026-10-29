package com.societyapp.application.model.finance;

import com.societyapp.domain.type.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class SocietyFinanceModels {
    private SocietyFinanceModels() {}

    public record Actor(UUID societyId, UUID userId, UserRole role) {}

    public record ExpenseCommand(
            ExpenseCategory category,
            String vendor,
            String description,
            BigDecimal amount,
            LocalDate expenseDate,
            PaymentMethod paymentMethod,
            String paymentReference) {}

    public record ExpenseView(
            UUID id,
            ExpenseCategory category,
            String vendor,
            String description,
            BigDecimal amount,
            LocalDate expenseDate,
            PaymentMethod paymentMethod,
            String paymentReference,
            OffsetDateTime createdAt) {}

    public record ExpenseCreated(UUID id, BigDecimal amount) {}

    public record ChargeSettingCommand(
            String name,
            MemberChargeType chargeType,
            BigDecimal amount,
            FinanceFrequency frequency,
            String description,
            UUID spaceId) {}

    public record ChargeSettingView(
            UUID id,
            UUID spaceId,
            String name,
            MemberChargeType chargeType,
            BigDecimal amount,
            FinanceFrequency frequency,
            String description,
            String spaceNumber,
            String residentName) {}

    public record ChargeSettingCreated(UUID id, String name) {}

    public record BudgetAddition(UUID id, BigDecimal amount, String note, OffsetDateTime addedAt) {}

    public record FinanceSummary(
            int year,
            BigDecimal totalBudget,
            BigDecimal receivedPayments,
            BigDecimal manualBudget,
            BigDecimal expenseSpent,
            BigDecimal salarySpent,
            BigDecimal totalSpent,
            BigDecimal remaining,
            BigDecimal overBudget,
            List<BudgetAddition> budgetAdditions) {}

    public record AddBudgetCommand(int year, BigDecimal amount, String note) {}

    public record SetBudgetCommand(int year, BigDecimal amount) {}

    public record BudgetResult(UUID id, Integer year, BigDecimal amount) {}

    public record SalaryPayment(
            UUID id,
            LocalDate salaryMonth,
            BigDecimal amount,
            LocalDate paidOn,
            PaymentMethod paymentMethod,
            String referenceNumber) {}

    public record StaffSalary(
            UUID staffId,
            String staffName,
            String staffType,
            BigDecimal monthlySalary,
            List<SalaryPayment> payments) {}
}
