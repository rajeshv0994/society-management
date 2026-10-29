package com.societyapp.application.model.platformadmin;

import com.societyapp.domain.type.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

public final class PlatformAdminModels {
    private PlatformAdminModels() {}

    public record CreateSocietyCommand(
            String name,
            String societyType,
            int totalBlocks,
            int totalFlats,
            int plannedStaff,
            String address,
            String city,
            String state,
            String pinCode,
            String contactPerson,
            String email,
            String mobile,
            SubscriptionPlanCode planCode,
            List<String> enabledModules) {}

    public record UpdateSocietyCommand(
            String name,
            String description,
            String website,
            String contactPerson,
            String email,
            String mobile,
            String address,
            String city,
            String state,
            String pinCode,
            SubscriptionPlanCode planCode,
            int plannedStaff,
            SocietyStatus status,
            List<String> enabledModules) {}

    public record ServiceCommand(
            String code,
            String name,
            String description,
            PlatformServiceStatus status,
            int sortOrder,
            ServiceAvailabilityType availabilityType,
            BigDecimal pricePerSpace) {}

    public record PlanCommand(
            String name,
            ApprovalMode approvalMode,
            Integer maxFlats,
            BigDecimal pricePerSpace,
            Integer maxStaff,
            BigDecimal pricePerStaff,
            Integer trialDurationValue,
            TrialDurationUnit trialDurationUnit,
            Integer trialDurationMonths) {}

    public record Dashboard(
            int totalSocieties,
            long activeSocieties,
            long pendingApprovals,
            long activeMembers,
            long openRequirements,
            long openTickets,
            long failedNotifications,
            BigDecimal monthlyPlanValue) {}

    public record SocietySummary(
            PlatformSocietyModels.Society society,
            int documentCount,
            long configuredSpaceCount,
            long activeMemberCount,
            BigDecimal subscriptionEstimatedMonthly,
            BigDecimal subscriptionServiceMonthly) {}

    public record SocietyCreated(
            UUID societyId,
            SocietyStatus status,
            String registrationNumber,
            List<String> enabledModules,
            String setupCode,
            OffsetDateTime expiresAt) {}

    public record SocietyUpdated(
            UUID societyId,
            SocietyStatus status,
            SubscriptionPlanCode planCode,
            List<String> enabledModules,
            boolean updated) {}

    public record PlanView(
            String code,
            String name,
            ApprovalMode approvalMode,
            Integer maxFlats,
            BigDecimal pricePerSpace,
            Integer maxStaff,
            BigDecimal pricePerStaff,
            Integer trialDurationValue,
            TrialDurationUnit trialDurationUnit,
            Integer trialDurationMonths,
            long societyCount) {}

    public record ServiceView(
            String code,
            String name,
            String description,
            PlatformServiceStatus status,
            int sortOrder,
            ServiceAvailabilityType availabilityType,
            BigDecimal pricePerSpace,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            int selectedSocietyCount,
            BigDecimal projectedMonthlyRevenue) {}

    public record PlanUpdated(String code, boolean updated) {}

    public record TicketView(
            UUID id,
            UUID societyId,
            String societyName,
            UUID createdBy,
            String createdByName,
            String subject,
            String description,
            SupportPriority priority,
            SupportTicketStatus status,
            UUID assignedTo,
            String resolution,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            OffsetDateTime closedAt,
            int messageCount) {}

    public record TicketUpdated(UUID id, SupportTicketStatus status) {}
}
