package com.societyapp.application.usecase;

import com.societyapp.application.model.dashboard.PlatformDashboardView;
import com.societyapp.application.model.dashboard.SocietyDashboardView;
import com.societyapp.application.port.in.DashboardUseCase;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.type.AccountStatus;
import com.societyapp.domain.type.ComplaintStatus;
import com.societyapp.domain.type.PaymentStatus;
import com.societyapp.domain.type.SocietyStatus;
import com.societyapp.domain.type.UserRole;
import com.societyapp.domain.type.VisitorStatus;
import java.math.BigDecimal;
import java.util.*;

public class DashboardService implements DashboardUseCase {
    private final SocietyRepository societies;
    private final SubscriptionPlanRepository plans;
    private final UserAccountJpaRepository users;
    private final BlockRepository blocks;
    private final FloorRepository floors;
    private final FlatRepository flats;
    private final ComplaintRepository complaints;
    private final VisitorRepository visitors;
    private final MaintenanceInvoiceRepository invoices;

    public DashboardService(
            SocietyRepository societies,
            SubscriptionPlanRepository plans,
            UserAccountJpaRepository users,
            BlockRepository blocks,
            FloorRepository floors,
            FlatRepository flats,
            ComplaintRepository complaints,
            VisitorRepository visitors,
            MaintenanceInvoiceRepository invoices) {
        this.societies = societies;
        this.plans = plans;
        this.users = users;
        this.blocks = blocks;
        this.floors = floors;
        this.flats = flats;
        this.complaints = complaints;
        this.visitors = visitors;
        this.invoices = invoices;
    }

    public PlatformDashboardView platform() {
        var allSocieties = societies.findAll();
        long activeMembers =
                users.findAll().stream()
                        .filter(user -> UserRole.RESIDENT.matches(user.getRole()))
                        .filter(user -> AccountStatus.ACTIVE.matches(user.getStatus()))
                        .count();
        BigDecimal monthlyValue =
                allSocieties.stream()
                        .filter(society -> SocietyStatus.ACTIVE.matches(society.getStatus()))
                        .map(
                                society ->
                                        plans.findById(society.getPlanCode())
                                                .map(
                                                        plan ->
                                                                plan.getPricePerSpace()
                                                                        .multiply(
                                                                                BigDecimal.valueOf(
                                                                                        society
                                                                                                .getTotalFlats()))
                                                                        .add(
                                                                                plan.getPricePerStaff()
                                                                                        .multiply(
                                                                                                BigDecimal
                                                                                                        .valueOf(
                                                                                                                society
                                                                                                                        .getPlannedStaff()))))
                                                .orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PlatformDashboardView(
                allSocieties.size(),
                allSocieties.stream()
                        .filter(
                                society ->
                                        SocietyStatus.PENDING_VERIFICATION.matches(
                                                society.getStatus()))
                        .count(),
                activeMembers,
                monthlyValue);
    }

    public SocietyDashboardView society(UUID societyId) {
        long flatCount =
                blocks.findBySocietyIdOrderByName(societyId).stream()
                        .flatMap(
                                block ->
                                        floors
                                                .findByBlockIdOrderByFloorNumber(block.getId())
                                                .stream())
                        .mapToLong(
                                floor -> flats.findByFloorIdOrderByFlatNumber(floor.getId()).size())
                        .sum();
        long memberCount =
                users.findAll().stream()
                        .filter(user -> societyId.equals(user.getSocietyId()))
                        .filter(user -> UserRole.RESIDENT.matches(user.getRole()))
                        .filter(user -> AccountStatus.ACTIVE.matches(user.getStatus()))
                        .count();
        long openComplaints =
                complaints.findBySocietyIdOrderByCreatedAtDesc(societyId).stream()
                        .filter(item -> !ComplaintStatus.from(item.getStatus()).isTerminal())
                        .count();
        long inside =
                visitors.findBySocietyIdOrderByCreatedAtDesc(societyId).stream()
                        .filter(item -> VisitorStatus.CHECKED_IN.matches(item.getStatus()))
                        .count();
        long unpaid =
                invoices.findBySocietyIdOrderByBillingMonthDesc(societyId).stream()
                        .filter(
                                item ->
                                        Set.of(
                                                        PaymentStatus.DUE.code(),
                                                        PaymentStatus.PART_PAID.code(),
                                                        PaymentStatus.OVERDUE.code())
                                                .contains(item.getStatus()))
                        .count();
        return new SocietyDashboardView(flatCount, memberCount, openComplaints, inside, unpaid);
    }
}
