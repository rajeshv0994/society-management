package com.societyapp.application.policy;

import com.societyapp.application.exception.ConflictException;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.SocietyCustomPlan;
import com.societyapp.domain.model.SubscriptionPlan;
import com.societyapp.domain.type.AccountStatus;
import com.societyapp.domain.type.LifecycleStatus;
import com.societyapp.domain.type.UserRole;
import java.util.Objects;
import java.util.UUID;

public class StaffCapacityPolicy {
    private final SocietyRepository societies;
    private final SubscriptionPlanRepository plans;
    private final SocietyCustomPlanRepository customPlans;
    private final UserAccountJpaRepository users;

    public StaffCapacityPolicy(
            SocietyRepository societies,
            SubscriptionPlanRepository plans,
            SocietyCustomPlanRepository customPlans,
            UserAccountJpaRepository users) {
        this.societies = societies;
        this.plans = plans;
        this.customPlans = customPlans;
        this.users = users;
    }

    public void ensureAvailable(UUID societyId) {
        var society =
                societies
                        .findById(societyId)
                        .orElseThrow(() -> new ConflictException("Society not found"));
        Integer planLimit =
                customPlans.findAllByOrderByCreatedAtDesc().stream()
                        .filter(plan -> societyId.equals(plan.getSocietyId()))
                        .filter(plan -> LifecycleStatus.ACTIVE.matches(plan.getStatus()))
                        .map(SocietyCustomPlan::getMaxStaff)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElseGet(
                                () ->
                                        plans.findById(society.getPlanCode())
                                                .map(SubscriptionPlan::getMaxStaff)
                                                .orElse(null));
        int purchasedSeats = society.getPlannedStaff();
        int allowed = planLimit == null ? purchasedSeats : Math.min(purchasedSeats, planLimit);
        long activeStaff =
                users
                        .findBySocietyIdAndRoleOrderByFullName(societyId, UserRole.STAFF.code())
                        .stream()
                        .filter(user -> AccountStatus.ACTIVE.matches(user.getStatus()))
                        .count();
        if (activeStaff >= allowed) {
            throw new ConflictException(
                    "Your subscription includes "
                            + allowed
                            + " staff seats. Increase the selected staff quantity first.");
        }
    }
}
