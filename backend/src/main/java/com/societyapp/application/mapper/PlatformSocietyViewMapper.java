package com.societyapp.application.mapper;

import com.societyapp.application.model.platformadmin.PlatformSocietyModels;
import com.societyapp.application.port.out.JsonCodec;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.util.List;
import java.util.function.Function;

public final class PlatformSocietyViewMapper {
    private final JsonCodec json;

    public PlatformSocietyViewMapper(JsonCodec json) {
        this.json = json;
    }

    public PlatformSocietyModels.Society society(Society value) {
        return new PlatformSocietyModels.Society(
                value.getId(),
                value.getName(),
                value.getRegistrationNumber(),
                value.getSocietyCode(),
                value.getSocietyType(),
                value.getTotalBlocks(),
                value.getTotalFlats(),
                value.getApproximateResidents(),
                value.getPlannedStaff(),
                value.getAddress(),
                value.getCity(),
                value.getState(),
                value.getPinCode(),
                value.getContactPerson(),
                value.getEmail(),
                value.getMobile(),
                value.getPlanCode(),
                strings(value.getEnabledModules()),
                nullable(value.getStatus(), SocietyStatus::from),
                value.getRejectionReason(),
                nullable(value.getSubscriptionStatus(), SubscriptionStatus::from),
                value.getSubscriptionStartedAt(),
                value.getSubscriptionExpiresAt(),
                nullable(value.getSubscriptionBillingCycle(), BillingCycle::from),
                value.isSetupCompleted(),
                value.getDescription(),
                value.getWebsite(),
                value.getLogoStorageKey(),
                value.getLogoContentType(),
                value.getCreatedAt(),
                value.getApprovedAt(),
                value.getUpdatedAt());
    }

    public PlatformSocietyModels.Plan plan(SubscriptionPlan value) {
        if (value == null) return null;
        return new PlatformSocietyModels.Plan(
                value.getCode(),
                value.getName(),
                nullable(value.getApprovalMode(), ApprovalMode::from),
                value.getMaxFlats(),
                value.getPriceMonthly(),
                value.getPricePerSpace(),
                value.getTrialDurationMonths(),
                value.getTrialDurationValue(),
                nullable(value.getTrialDurationUnit(), TrialDurationUnit::from),
                value.getMaxStaff(),
                value.getPricePerStaff());
    }

    public PlatformSocietyModels.Block block(
            Block value, List<PlatformSocietyModels.Section> sections) {
        return new PlatformSocietyModels.Block(
                value.getId(), value.getSocietyId(), value.getName(), sections);
    }

    public PlatformSocietyModels.Section section(
            Floor value, List<PlatformSocietyModels.Space> spaces) {
        return new PlatformSocietyModels.Section(
                value.getId(), value.getBlockId(), value.getName(), value.getFloorNumber(), spaces);
    }

    public PlatformSocietyModels.Space space(Flat value) {
        return new PlatformSocietyModels.Space(
                value.getId(),
                value.getFloorId(),
                value.getFlatNumber(),
                nullable(value.getOccupancyStatus(), FlatOccupancyStatus::from),
                value.getActiveOwnerId(),
                value.getActiveTenantId(),
                value.getAreaSqft());
    }

    public PlatformSocietyModels.Document document(SocietyDocument value) {
        return new PlatformSocietyModels.Document(
                value.getId(),
                value.getSocietyId(),
                nullable(value.getDocumentType(), SocietyDocumentType::from),
                value.getStorageKey(),
                nullable(value.getVerificationStatus(), VerificationStatus::from));
    }

    public PlatformSocietyModels.Order order(SubscriptionOrder value) {
        return new PlatformSocietyModels.Order(
                value.getId(),
                value.getSocietyId(),
                value.getInvoiceNumber(),
                value.getBillingMonth(),
                value.getDueDate(),
                nullable(value.getBillingCycle(), BillingCycle::from),
                value.getPeriodStart(),
                value.getPeriodEnd(),
                value.getPlanCode(),
                value.getAmount(),
                value.getCurrency(),
                value.getProvider(),
                value.getProviderReference(),
                nullable(value.getStatus(), PaymentStatus::from),
                value.getCreatedAt(),
                value.getPaidAt(),
                value.getPaymentMethod(),
                value.getPaymentNote(),
                value.getSubmittedAt(),
                value.getReviewedAt(),
                value.getReviewedBy(),
                value.getRejectionReason(),
                value.getPricingBreakdown());
    }

    public PlatformSocietyModels.CustomPlan customPlan(SocietyCustomPlan value) {
        return new PlatformSocietyModels.CustomPlan(
                value.getId(),
                value.getSocietyId(),
                value.getRequirementId(),
                value.getPlanName(),
                value.getDescription(),
                strings(value.getEnabledModules()),
                value.getMaxFlats(),
                value.getMaxStaff(),
                value.getMaxStorageGb(),
                value.getPriceMonthly(),
                value.getPricePerSpace(),
                value.getPricePerStaff(),
                value.getSetupFee(),
                nullable(value.getBillingCycle(), BillingCycle::from),
                value.getValidFrom(),
                value.getValidUntil(),
                nullable(value.getStatus(), CustomPlanStatus::from),
                value.getCreatedBy(),
                value.getCreatedAt(),
                value.getActivatedAt(),
                value.getCancelledAt(),
                value.getCancelledBy(),
                value.getCancellationReason());
    }

    public PlatformSocietyModels.SupportTicket support(SupportTicket value) {
        return new PlatformSocietyModels.SupportTicket(
                value.getId(),
                value.getSocietyId(),
                value.getCreatedBy(),
                value.getSubject(),
                value.getDescription(),
                nullable(value.getPriority(), SupportPriority::from),
                nullable(value.getStatus(), SupportTicketStatus::from),
                value.getAssignedTo(),
                value.getResolution(),
                value.getCreatedAt(),
                value.getUpdatedAt(),
                value.getClosedAt());
    }

    public PlatformSocietyModels.Requirement requirement(RequirementRequest value) {
        return new PlatformSocietyModels.Requirement(
                value.getId(),
                value.getSocietyId(),
                value.getSocietyName(),
                value.getContactPerson(),
                value.getEmail(),
                value.getMobile(),
                value.getCity(),
                value.getEstimatedFlats(),
                value.getEstimatedStaff(),
                strings(value.getRequestedModules()),
                value.getRequirementDetails(),
                value.getBudgetMonthly(),
                nullable(value.getStatus(), RequirementStatus::from),
                value.getAdminNotes(),
                value.getAssignedTo(),
                value.getCreatedAt(),
                value.getUpdatedAt(),
                value.getClosedAt());
    }

    public PlatformSocietyModels.AuditEvent audit(AuditEvent value) {
        return new PlatformSocietyModels.AuditEvent(
                value.getId(),
                value.getSocietyId(),
                value.getActorUserId(),
                value.getAction(),
                value.getEntityType(),
                value.getEntityId(),
                value.isSensitive(),
                value.getDetails(),
                value.getOccurredAt());
    }

    private List<String> strings(String value) {
        return value == null || value.isBlank() ? List.of() : json.readStrings(value);
    }

    private <T> T nullable(String value, Function<String, T> parser) {
        return value == null || value.isBlank() ? null : parser.apply(value);
    }
}
