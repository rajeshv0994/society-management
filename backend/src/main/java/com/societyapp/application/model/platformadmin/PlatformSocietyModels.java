package com.societyapp.application.model.platformadmin;

import com.societyapp.application.model.platform.PlatformNotificationView;
import com.societyapp.domain.type.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class PlatformSocietyModels {
    private PlatformSocietyModels() {}

    public record Society(
            UUID id,
            String name,
            String registrationNumber,
            String societyCode,
            String societyType,
            int totalBlocks,
            int totalSpaces,
            Integer approximateResidents,
            int plannedStaff,
            String address,
            String city,
            String state,
            String pinCode,
            String contactPerson,
            String email,
            String mobile,
            String planCode,
            List<String> enabledModules,
            SocietyStatus status,
            String rejectionReason,
            SubscriptionStatus subscriptionStatus,
            OffsetDateTime subscriptionStartedAt,
            OffsetDateTime subscriptionExpiresAt,
            BillingCycle subscriptionBillingCycle,
            boolean setupCompleted,
            String description,
            String website,
            String logoStorageKey,
            String logoContentType,
            OffsetDateTime createdAt,
            OffsetDateTime approvedAt,
            OffsetDateTime updatedAt) {}

    public record Plan(
            String code,
            String name,
            ApprovalMode approvalMode,
            Integer maxSpaces,
            BigDecimal priceMonthly,
            BigDecimal pricePerSpace,
            Integer trialDurationMonths,
            Integer trialDurationValue,
            TrialDurationUnit trialDurationUnit,
            Integer maxStaff,
            BigDecimal pricePerStaff) {}

    public record Space(
            UUID id,
            UUID sectionId,
            String spaceNumber,
            FlatOccupancyStatus occupancyStatus,
            UUID activeOwnerId,
            UUID activeTenantId,
            BigDecimal areaSqft) {}

    public record Section(
            UUID id, UUID blockId, String name, int sectionOrder, List<Space> spaces) {}

    public record Block(UUID id, UUID societyId, String name, List<Section> sections) {}

    public record Structure(
            int blocks,
            long sections,
            long spaces,
            int parkingSlots,
            int amenities,
            List<Block> hierarchy) {}

    public record User(
            UUID id,
            String fullName,
            String email,
            String mobile,
            UserRole role,
            AccountStatus status,
            OffsetDateTime createdAt) {}

    public record HouseholdSummary(
            long households,
            long familyMembers,
            long vehicles,
            long emergencyContacts,
            long pendingVerification) {}

    public record DeliverySummary(long total, long active) {}

    public record EmergencySummary(long total, long open, long critical) {}

    public record ActivitySummary(
            long total,
            long published,
            long participations,
            long publishedPhotos,
            long pendingPhotos) {}

    public record Document(
            UUID id,
            UUID societyId,
            SocietyDocumentType documentType,
            String storageKey,
            VerificationStatus verificationStatus) {}

    public record Order(
            UUID id,
            UUID societyId,
            String invoiceNumber,
            LocalDate billingMonth,
            LocalDate dueDate,
            BillingCycle billingCycle,
            LocalDate periodStart,
            LocalDate periodEnd,
            String planCode,
            BigDecimal amount,
            String currency,
            String provider,
            String providerReference,
            PaymentStatus status,
            OffsetDateTime createdAt,
            OffsetDateTime paidAt,
            String paymentMethod,
            String paymentNote,
            OffsetDateTime submittedAt,
            OffsetDateTime reviewedAt,
            UUID reviewedBy,
            String rejectionReason,
            String pricingBreakdown) {}

    public record CustomPlan(
            UUID id,
            UUID societyId,
            UUID requirementId,
            String planName,
            String description,
            List<String> enabledModules,
            Integer maxSpaces,
            Integer maxStaff,
            Integer maxStorageGb,
            BigDecimal priceMonthly,
            BigDecimal pricePerSpace,
            BigDecimal pricePerStaff,
            BigDecimal setupFee,
            BillingCycle billingCycle,
            LocalDate validFrom,
            LocalDate validUntil,
            CustomPlanStatus status,
            UUID createdBy,
            OffsetDateTime createdAt,
            OffsetDateTime activatedAt,
            OffsetDateTime cancelledAt,
            UUID cancelledBy,
            String cancellationReason) {}

    public record SupportTicket(
            UUID id,
            UUID societyId,
            UUID createdBy,
            String subject,
            String description,
            SupportPriority priority,
            SupportTicketStatus status,
            UUID assignedTo,
            String resolution,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            OffsetDateTime closedAt) {}

    public record Requirement(
            UUID id,
            UUID societyId,
            String societyName,
            String contactPerson,
            String email,
            String mobile,
            String city,
            Integer estimatedSpaces,
            Integer estimatedStaff,
            List<String> requestedModules,
            String requirementDetails,
            BigDecimal budgetMonthly,
            RequirementStatus status,
            String adminNotes,
            UUID assignedTo,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            OffsetDateTime closedAt) {}

    public record AuditEvent(
            Long id,
            UUID societyId,
            UUID actorUserId,
            String action,
            String entityType,
            String entityId,
            boolean sensitive,
            String details,
            OffsetDateTime occurredAt) {}

    public record Details(
            Society society,
            Plan plan,
            Structure structure,
            List<User> users,
            HouseholdSummary householdSummary,
            DeliverySummary deliverySummary,
            EmergencySummary emergencySummary,
            ActivitySummary activitySummary,
            List<Document> documents,
            List<Order> orders,
            List<CustomPlan> customPlans,
            List<SupportTicket> support,
            List<Requirement> requirements,
            List<PlatformNotificationView> notifications,
            List<AuditEvent> auditEvents) {}
}
