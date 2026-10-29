package com.societyapp.application.port.in;

import com.societyapp.application.model.finance.SocietyFinanceModels;
import java.util.List;

public interface SocietyFinanceUseCase {
    List<SocietyFinanceModels.ExpenseView> expenses(SocietyFinanceModels.Actor actor);

    SocietyFinanceModels.ExpenseCreated createExpense(
            SocietyFinanceModels.Actor actor, SocietyFinanceModels.ExpenseCommand command);

    List<SocietyFinanceModels.ChargeSettingView> chargeSettings(SocietyFinanceModels.Actor actor);

    SocietyFinanceModels.ChargeSettingCreated createChargeSetting(
            SocietyFinanceModels.Actor actor, SocietyFinanceModels.ChargeSettingCommand command);

    SocietyFinanceModels.FinanceSummary financeSummary(SocietyFinanceModels.Actor actor, int year);

    SocietyFinanceModels.BudgetResult addBudget(
            SocietyFinanceModels.Actor actor, SocietyFinanceModels.AddBudgetCommand command);

    SocietyFinanceModels.BudgetResult setBudget(
            SocietyFinanceModels.Actor actor, SocietyFinanceModels.SetBudgetCommand command);

    List<SocietyFinanceModels.StaffSalary> staffSalaries(SocietyFinanceModels.Actor actor);
}
