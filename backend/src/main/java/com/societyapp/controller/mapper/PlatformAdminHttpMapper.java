package com.societyapp.controller.mapper;

import com.societyapp.application.model.platformadmin.PlatformAdminModels;
import com.societyapp.application.model.platformadmin.PlatformSocietyModels;
import com.societyapp.controller.model.PlatformAdminResponse;
import com.societyapp.controller.model.PlatformBillingResponse;

public final class PlatformAdminHttpMapper {
    private final PlatformSocietyHttpMapper societyMapper = new PlatformSocietyHttpMapper();

    public PlatformAdminResponse.Dashboard toResponse(PlatformAdminModels.Dashboard v) {
        return new PlatformAdminResponse.Dashboard(
                v.totalSocieties(),
                v.activeSocieties(),
                v.pendingApprovals(),
                v.activeMembers(),
                v.openRequirements(),
                v.openTickets(),
                v.failedNotifications(),
                v.monthlyPlanValue());
    }

    public PlatformAdminResponse.SocietySummary toResponse(PlatformAdminModels.SocietySummary v) {
        return new PlatformAdminResponse.SocietySummary(
                societyMapper.toResponse(v.society()),
                v.documentCount(),
                v.configuredSpaceCount(),
                v.configuredSpaceCount(),
                v.configuredSpaceCount(),
                v.activeMemberCount(),
                v.subscriptionEstimatedMonthly(),
                v.subscriptionServiceMonthly());
    }

    public PlatformAdminResponse.SocietyCreated toResponse(
            PlatformAdminModels.SocietyCreated v, PlatformBillingResponse.Order invoice) {
        return new PlatformAdminResponse.SocietyCreated(
                v.societyId(),
                v.status().code(),
                v.registrationNumber(),
                v.enabledModules(),
                v.setupCode(),
                v.expiresAt(),
                invoice);
    }

    public PlatformAdminResponse.SocietyUpdated toResponse(PlatformAdminModels.SocietyUpdated v) {
        return new PlatformAdminResponse.SocietyUpdated(
                v.societyId(),
                v.status().code(),
                v.planCode().code(),
                v.enabledModules(),
                v.updated());
    }

    public PlatformAdminResponse.SocietyDetails toResponse(PlatformSocietyModels.Details v) {
        return societyMapper.toResponse(v);
    }

    public PlatformAdminResponse.Plan toResponse(PlatformAdminModels.PlanView v) {
        return new PlatformAdminResponse.Plan(
                v.code(),
                v.name(),
                v.approvalMode().code(),
                v.maxFlats(),
                v.pricePerSpace(),
                v.maxStaff(),
                v.pricePerStaff(),
                v.trialDurationValue(),
                v.trialDurationUnit().code(),
                v.trialDurationMonths(),
                v.societyCount());
    }

    public PlatformAdminResponse.Service toResponse(PlatformAdminModels.ServiceView v) {
        return new PlatformAdminResponse.Service(
                v.code(),
                v.name(),
                v.description(),
                v.status().code(),
                v.sortOrder(),
                v.availabilityType().code(),
                v.pricePerSpace(),
                v.createdAt(),
                v.updatedAt(),
                v.selectedSocietyCount(),
                v.projectedMonthlyRevenue());
    }

    public PlatformAdminResponse.PlanUpdated toResponse(PlatformAdminModels.PlanUpdated v) {
        return new PlatformAdminResponse.PlanUpdated(v.code(), v.updated());
    }

    public PlatformAdminResponse.Ticket toResponse(PlatformAdminModels.TicketView v) {
        return new PlatformAdminResponse.Ticket(
                v.id(),
                v.societyId(),
                v.societyName(),
                v.createdBy(),
                v.createdByName(),
                v.subject(),
                v.description(),
                v.priority().code(),
                v.status().code(),
                v.assignedTo(),
                v.resolution(),
                v.createdAt(),
                v.updatedAt(),
                v.closedAt(),
                v.messageCount());
    }

    public PlatformAdminResponse.TicketUpdated toResponse(PlatformAdminModels.TicketUpdated v) {
        return new PlatformAdminResponse.TicketUpdated(v.id(), v.status().code());
    }
}
