package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public final class PlatformAdminResponse {
    private PlatformAdminResponse() {}

    public record Dashboard(
            @JsonProperty("total_societies") int totalSocieties,
            @JsonProperty("active_societies") long activeSocieties,
            @JsonProperty("pending_approvals") long pendingApprovals,
            @JsonProperty("active_members") long activeMembers,
            @JsonProperty("open_requirements") long openRequirements,
            @JsonProperty("open_tickets") long openTickets,
            @JsonProperty("failed_notifications") long failedNotifications,
            @JsonProperty("monthly_plan_value") BigDecimal monthlyPlanValue) {}

    public record SocietySummary(
            SocietyOnboardingResponse.PendingSociety society,
            @JsonProperty("document_count") int documentCount,
            @JsonProperty("configured_flat_count") long configuredFlatCount,
            @JsonProperty("configured_unit_count") long configuredUnitCount,
            @JsonProperty("configured_space_count") long configuredSpaceCount,
            @JsonProperty("active_member_count") long activeMemberCount,
            @JsonProperty("subscription_estimated_monthly") BigDecimal subscriptionEstimatedMonthly,
            @JsonProperty("subscription_service_monthly") BigDecimal subscriptionServiceMonthly) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SocietyCreated(
            UUID societyId,
            String status,
            String registrationNumber,
            List<String> enabledModules,
            String setupCode,
            OffsetDateTime expiresAt,
            PlatformBillingResponse.Order initialInvoice) {}

    public record SocietyUpdated(
            UUID societyId,
            String status,
            String planCode,
            List<String> enabledModules,
            boolean updated) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Space(
            UUID id,
            @JsonProperty("section_id") UUID sectionId,
            @JsonProperty("floor_id") UUID floorId,
            @JsonProperty("space_number") String spaceNumber,
            @JsonProperty("unit_number") String unitNumber,
            @JsonProperty("flat_number") String flatNumber,
            @JsonProperty("occupancy_status") String occupancyStatus,
            @JsonProperty("active_owner_id") UUID activeOwnerId,
            @JsonProperty("active_tenant_id") UUID activeTenantId,
            @JsonProperty("area_sqft") BigDecimal areaSqft) {}

    public record Section(
            UUID id,
            @JsonProperty("block_id") UUID blockId,
            String name,
            @JsonProperty("section_order") int sectionOrder,
            @JsonProperty("floor_number") int floorNumber,
            List<Space> spaces) {}

    public record Block(
            UUID id,
            @JsonProperty("society_id") UUID societyId,
            String name,
            List<Section> sections) {}

    public record Structure(
            int blocks,
            long sections,
            long spaces,
            @JsonProperty("parking_slots") int parkingSlots,
            int amenities,
            List<Block> hierarchy) {}

    public record User(
            UUID id,
            @JsonProperty("full_name") String fullName,
            String email,
            String mobile,
            String role,
            String status,
            @JsonProperty("created_at") OffsetDateTime createdAt) {}

    public record HouseholdSummary(
            long households,
            @JsonProperty("family_members") long familyMembers,
            long vehicles,
            @JsonProperty("emergency_contacts") long emergencyContacts,
            @JsonProperty("pending_verification") long pendingVerification) {}

    public record DeliverySummary(long total, long active) {}

    public record EmergencySummary(long total, long open, long critical) {}

    public record ActivitySummary(
            long total,
            long published,
            long participations,
            @JsonProperty("published_photos") long publishedPhotos,
            @JsonProperty("pending_photos") long pendingPhotos) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Order(
            UUID id,
            @JsonProperty("society_id") UUID societyId,
            @JsonProperty("invoice_number") String invoiceNumber,
            @JsonProperty("billing_month") LocalDate billingMonth,
            @JsonProperty("due_date") LocalDate dueDate,
            @JsonProperty("billing_cycle") String billingCycle,
            @JsonProperty("period_start") LocalDate periodStart,
            @JsonProperty("period_end") LocalDate periodEnd,
            @JsonProperty("plan_code") String planCode,
            BigDecimal amount,
            String currency,
            String provider,
            @JsonProperty("provider_reference") String providerReference,
            String status,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("paid_at") OffsetDateTime paidAt,
            @JsonProperty("payment_method") String paymentMethod,
            @JsonProperty("payment_note") String paymentNote,
            @JsonProperty("submitted_at") OffsetDateTime submittedAt,
            @JsonProperty("reviewed_at") OffsetDateTime reviewedAt,
            @JsonProperty("reviewed_by") UUID reviewedBy,
            @JsonProperty("rejection_reason") String rejectionReason,
            @JsonProperty("pricing_breakdown") String pricingBreakdown) {}

    public record AuditEvent(
            Long id,
            @JsonProperty("society_id") UUID societyId,
            @JsonProperty("actor_user_id") UUID actorUserId,
            String action,
            @JsonProperty("entity_type") String entityType,
            @JsonProperty("entity_id") String entityId,
            boolean sensitive,
            String details,
            @JsonProperty("occurred_at") OffsetDateTime occurredAt) {}

    public record SocietyDetails(
            SocietyOnboardingResponse.PendingSociety society,
            SocietyOnboardingResponse.Plan plan,
            Structure structure,
            List<User> users,
            @JsonProperty("household_summary") HouseholdSummary householdSummary,
            @JsonProperty("delivery_summary") DeliverySummary deliverySummary,
            @JsonProperty("emergency_summary") EmergencySummary emergencySummary,
            @JsonProperty("activity_summary") ActivitySummary activitySummary,
            List<SocietyDocumentResponse.Document> documents,
            List<Order> orders,
            List<RequirementResponse.Plan> customPlans,
            List<SupportResponse.Ticket> support,
            List<RequirementResponse.Requirement> requirements,
            List<PlatformNotificationResponse> notifications,
            @JsonProperty("audit_events") List<AuditEvent> auditEvents) {}

    public record Plan(
            String code,
            String name,
            @JsonProperty("approval_mode") String approvalMode,
            @JsonProperty("max_flats") Integer maxFlats,
            @JsonProperty("price_per_space") BigDecimal pricePerSpace,
            @JsonProperty("max_staff") Integer maxStaff,
            @JsonProperty("price_per_staff") BigDecimal pricePerStaff,
            @JsonProperty("trial_duration_value") Integer trialDurationValue,
            @JsonProperty("trial_duration_unit") String trialDurationUnit,
            @JsonProperty("trial_duration_months") Integer trialDurationMonths,
            @JsonProperty("society_count") long societyCount) {}

    public record Service(
            String code,
            String name,
            String description,
            String status,
            @JsonProperty("sort_order") int sortOrder,
            @JsonProperty("availability_type") String availabilityType,
            @JsonProperty("price_per_space") BigDecimal pricePerSpace,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("updated_at") OffsetDateTime updatedAt,
            @JsonProperty("selected_society_count") int selectedSocietyCount,
            @JsonProperty("projected_monthly_revenue") BigDecimal projectedMonthlyRevenue) {}

    public record PlanUpdated(String code, boolean updated) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Ticket(
            UUID id,
            @JsonProperty("society_id") UUID societyId,
            @JsonProperty("society_name") String societyName,
            @JsonProperty("created_by") UUID createdBy,
            @JsonProperty("created_by_name") String createdByName,
            String subject,
            String description,
            String priority,
            String status,
            @JsonProperty("assigned_to") UUID assignedTo,
            String resolution,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("updated_at") OffsetDateTime updatedAt,
            @JsonProperty("closed_at") OffsetDateTime closedAt,
            @JsonProperty("message_count") int messageCount) {}

    public record TicketUpdated(UUID id, String status) {}
}
