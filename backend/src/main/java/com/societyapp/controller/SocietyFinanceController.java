package com.societyapp.controller;

import com.societyapp.application.model.finance.SocietyFinanceModels;
import com.societyapp.application.port.in.SocietyFinanceUseCase;
import com.societyapp.controller.mapper.SocietyFinanceHttpMapper;
import com.societyapp.controller.model.SocietyFinanceResponse;
import com.societyapp.domain.type.ExpenseCategory;
import com.societyapp.domain.type.FinanceFrequency;
import com.societyapp.domain.type.MemberChargeType;
import com.societyapp.domain.type.PaymentMethod;
import com.societyapp.domain.type.UserRole;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
public class SocietyFinanceController {
    private final SocietyFinanceUseCase service;
    private final SocietyFinanceHttpMapper mapper = new SocietyFinanceHttpMapper();

    public SocietyFinanceController(SocietyFinanceUseCase service) {
        this.service = service;
    }

    public record Expense(
            @NotBlank String category,
            @Size(max = 150) String vendor,
            @NotBlank @Size(max = 300) String description,
            @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal amount,
            @NotNull LocalDate expenseDate,
            @NotBlank String paymentMethod,
            @Size(max = 120) String paymentReference) {}

    public record ChargeSetting(
            @NotBlank @Size(max = 120) String name,
            @NotBlank String chargeType,
            @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal amount,
            @NotBlank String frequency,
            @Size(max = 240) String description,
            UUID flatId) {}

    public record Budget(@NotNull @DecimalMin("0") BigDecimal amount) {}

    public record BudgetAddition(
            @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal amount,
            @Size(max = 240) String note) {}

    @GetMapping("/expenses")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    List<SocietyFinanceResponse.Expense> expenses() {
        return service.expenses(actor()).stream().map(mapper::toResponse).toList();
    }

    @PostMapping("/expenses")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    SocietyFinanceResponse.ExpenseCreated expense(@Valid @RequestBody Expense request) {
        return mapper.toResponse(
                service.createExpense(
                        actor(),
                        new SocietyFinanceModels.ExpenseCommand(
                                ExpenseCategory.from(request.category()),
                                request.vendor(),
                                request.description(),
                                request.amount(),
                                request.expenseDate(),
                                PaymentMethod.from(request.paymentMethod()),
                                request.paymentReference())));
    }

    @GetMapping("/charge-settings")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    List<SocietyFinanceResponse.ChargeSetting> chargeSettings() {
        return service.chargeSettings(actor()).stream().map(mapper::toResponse).toList();
    }

    @PostMapping("/charge-settings")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    SocietyFinanceResponse.ChargeSettingCreated chargeSetting(
            @Valid @RequestBody ChargeSetting request) {
        return mapper.toResponse(
                service.createChargeSetting(
                        actor(),
                        new SocietyFinanceModels.ChargeSettingCommand(
                                request.name(), MemberChargeType.from(request.chargeType()),
                                request.amount(), FinanceFrequency.from(request.frequency()),
                                request.description(), request.flatId())));
    }

    @GetMapping("/finance-summary")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    SocietyFinanceResponse.FinanceSummary financeSummary() {
        return mapper.toResponse(service.financeSummary(actor(), LocalDate.now().getYear()));
    }

    @PutMapping("/budget")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    SocietyFinanceResponse.BudgetResult budget(@Valid @RequestBody Budget request) {
        return mapper.toResponse(
                service.setBudget(
                        actor(),
                        new SocietyFinanceModels.SetBudgetCommand(
                                LocalDate.now().getYear(), request.amount())));
    }

    @PostMapping("/budget/additions")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    SocietyFinanceResponse.BudgetResult addBudget(@Valid @RequestBody BudgetAddition request) {
        return mapper.toResponse(
                service.addBudget(
                        actor(),
                        new SocietyFinanceModels.AddBudgetCommand(
                                LocalDate.now().getYear(), request.amount(), request.note())));
    }

    @GetMapping("/staff-salaries")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    List<SocietyFinanceResponse.StaffSalary> staffSalaries() {
        return service.staffSalaries(actor()).stream().map(mapper::toResponse).toList();
    }

    private SocietyFinanceModels.Actor actor() {
        return new SocietyFinanceModels.Actor(
                CurrentUser.societyId(), CurrentUser.id(), UserRole.from(CurrentUser.role()));
    }
}
