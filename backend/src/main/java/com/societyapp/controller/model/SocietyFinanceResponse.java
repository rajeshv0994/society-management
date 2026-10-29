package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class SocietyFinanceResponse {
    private SocietyFinanceResponse() {}

    public record Expense(
            UUID id,
            String category,
            String vendor,
            String description,
            BigDecimal amount,
            @JsonProperty("expense_date") LocalDate expenseDate,
            @JsonProperty("payment_method") String paymentMethod,
            @JsonProperty("payment_reference") String paymentReference,
            @JsonProperty("created_at") OffsetDateTime createdAt) {}

    public record ExpenseCreated(UUID id, BigDecimal amount) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ChargeSetting(
            UUID id,
            @JsonProperty("flat_id") UUID flatId,
            String name,
            @JsonProperty("charge_type") String chargeType,
            BigDecimal amount,
            String frequency,
            String description,
            @JsonProperty("space_number") String spaceNumber,
            @JsonProperty("resident_name") String residentName) {}

    public record ChargeSettingCreated(UUID id, String name) {}

    public record BudgetAddition(
            UUID id,
            BigDecimal amount,
            String note,
            @JsonProperty("added_at") OffsetDateTime addedAt) {}

    public record FinanceSummary(
            int year,
            @JsonProperty("total_budget") BigDecimal totalBudget,
            @JsonProperty("received_payments") BigDecimal receivedPayments,
            @JsonProperty("manual_budget") BigDecimal manualBudget,
            @JsonProperty("expense_spent") BigDecimal expenseSpent,
            @JsonProperty("salary_spent") BigDecimal salarySpent,
            @JsonProperty("total_spent") BigDecimal totalSpent,
            BigDecimal remaining,
            @JsonProperty("over_budget") BigDecimal overBudget,
            @JsonProperty("budget_additions") List<BudgetAddition> budgetAdditions) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record BudgetResult(UUID id, Integer year, BigDecimal amount) {}

    public record SalaryPayment(
            UUID id,
            @JsonProperty("salary_month") LocalDate salaryMonth,
            BigDecimal amount,
            @JsonProperty("paid_on") LocalDate paidOn,
            @JsonProperty("payment_method") String paymentMethod,
            @JsonProperty("reference_number") String referenceNumber) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record StaffSalary(
            @JsonProperty("staff_id") UUID staffId,
            @JsonProperty("staff_name") String staffName,
            @JsonProperty("staff_type") String staffType,
            @JsonProperty("monthly_salary") BigDecimal monthlySalary,
            List<SalaryPayment> payments) {}
}
