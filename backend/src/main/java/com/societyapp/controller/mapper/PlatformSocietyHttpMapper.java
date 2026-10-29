package com.societyapp.controller.mapper;

import com.societyapp.application.model.platform.PlatformNotificationView;
import com.societyapp.application.model.platformadmin.PlatformSocietyModels;
import com.societyapp.controller.model.*;
import com.societyapp.domain.type.DomainCode;

public final class PlatformSocietyHttpMapper {
    public PlatformAdminResponse.SocietyDetails toResponse(PlatformSocietyModels.Details v) {
        return new PlatformAdminResponse.SocietyDetails(
                toResponse(v.society()),
                toResponse(v.plan()),
                toResponse(v.structure()),
                v.users().stream().map(this::toResponse).toList(),
                toResponse(v.householdSummary()),
                toResponse(v.deliverySummary()),
                toResponse(v.emergencySummary()),
                toResponse(v.activitySummary()),
                v.documents().stream().map(this::toResponse).toList(),
                v.orders().stream().map(this::toResponse).toList(),
                v.customPlans().stream().map(this::toResponse).toList(),
                v.support().stream().map(this::toResponse).toList(),
                v.requirements().stream().map(this::toResponse).toList(),
                v.notifications().stream().map(this::toResponse).toList(),
                v.auditEvents().stream().map(this::toResponse).toList());
    }

    public SocietyOnboardingResponse.PendingSociety toResponse(PlatformSocietyModels.Society v) {
        return new SocietyOnboardingResponse.PendingSociety(
                v.id(),
                v.name(),
                v.registrationNumber(),
                v.societyCode(),
                v.societyType(),
                v.totalBlocks(),
                v.totalSpaces(),
                v.totalSpaces(),
                v.totalSpaces(),
                v.approximateResidents(),
                v.plannedStaff(),
                v.address(),
                v.city(),
                v.state(),
                v.pinCode(),
                v.contactPerson(),
                v.email(),
                v.mobile(),
                v.planCode(),
                v.enabledModules(),
                code(v.status()),
                v.rejectionReason(),
                code(v.subscriptionStatus()),
                v.subscriptionStartedAt(),
                v.subscriptionExpiresAt(),
                code(v.subscriptionBillingCycle()),
                v.setupCompleted(),
                v.description(),
                v.website(),
                v.logoStorageKey(),
                v.logoContentType(),
                v.createdAt(),
                v.approvedAt(),
                v.updatedAt());
    }

    private SocietyOnboardingResponse.Plan toResponse(PlatformSocietyModels.Plan v) {
        if (v == null) return null;
        return new SocietyOnboardingResponse.Plan(
                v.code(),
                v.name(),
                code(v.approvalMode()),
                v.maxSpaces(),
                v.maxSpaces(),
                v.maxSpaces(),
                v.priceMonthly(),
                v.pricePerSpace(),
                v.trialDurationMonths(),
                v.trialDurationValue(),
                code(v.trialDurationUnit()),
                v.maxStaff(),
                v.pricePerStaff());
    }

    private PlatformAdminResponse.Structure toResponse(PlatformSocietyModels.Structure v) {
        return new PlatformAdminResponse.Structure(
                v.blocks(),
                v.sections(),
                v.spaces(),
                v.parkingSlots(),
                v.amenities(),
                v.hierarchy().stream().map(this::toResponse).toList());
    }

    private PlatformAdminResponse.Block toResponse(PlatformSocietyModels.Block v) {
        return new PlatformAdminResponse.Block(
                v.id(),
                v.societyId(),
                v.name(),
                v.sections().stream().map(this::toResponse).toList());
    }

    private PlatformAdminResponse.Section toResponse(PlatformSocietyModels.Section v) {
        return new PlatformAdminResponse.Section(
                v.id(),
                v.blockId(),
                v.name(),
                v.sectionOrder(),
                v.sectionOrder(),
                v.spaces().stream().map(this::toResponse).toList());
    }

    private PlatformAdminResponse.Space toResponse(PlatformSocietyModels.Space v) {
        return new PlatformAdminResponse.Space(
                v.id(),
                v.sectionId(),
                v.sectionId(),
                v.spaceNumber(),
                v.spaceNumber(),
                v.spaceNumber(),
                code(v.occupancyStatus()),
                v.activeOwnerId(),
                v.activeTenantId(),
                v.areaSqft());
    }

    private PlatformAdminResponse.User toResponse(PlatformSocietyModels.User v) {
        return new PlatformAdminResponse.User(
                v.id(),
                v.fullName(),
                v.email(),
                v.mobile(),
                code(v.role()),
                code(v.status()),
                v.createdAt());
    }

    private PlatformAdminResponse.HouseholdSummary toResponse(
            PlatformSocietyModels.HouseholdSummary v) {
        return new PlatformAdminResponse.HouseholdSummary(
                v.households(),
                v.familyMembers(),
                v.vehicles(),
                v.emergencyContacts(),
                v.pendingVerification());
    }

    private PlatformAdminResponse.DeliverySummary toResponse(
            PlatformSocietyModels.DeliverySummary v) {
        return new PlatformAdminResponse.DeliverySummary(v.total(), v.active());
    }

    private PlatformAdminResponse.EmergencySummary toResponse(
            PlatformSocietyModels.EmergencySummary v) {
        return new PlatformAdminResponse.EmergencySummary(v.total(), v.open(), v.critical());
    }

    private PlatformAdminResponse.ActivitySummary toResponse(
            PlatformSocietyModels.ActivitySummary v) {
        return new PlatformAdminResponse.ActivitySummary(
                v.total(),
                v.published(),
                v.participations(),
                v.publishedPhotos(),
                v.pendingPhotos());
    }

    private SocietyDocumentResponse.Document toResponse(PlatformSocietyModels.Document v) {
        return new SocietyDocumentResponse.Document(
                v.id(), code(v.documentType()), v.storageKey(), code(v.verificationStatus()));
    }

    private PlatformAdminResponse.Order toResponse(PlatformSocietyModels.Order v) {
        return new PlatformAdminResponse.Order(
                v.id(),
                v.societyId(),
                v.invoiceNumber(),
                v.billingMonth(),
                v.dueDate(),
                code(v.billingCycle()),
                v.periodStart(),
                v.periodEnd(),
                v.planCode(),
                v.amount(),
                v.currency(),
                v.provider(),
                v.providerReference(),
                code(v.status()),
                v.createdAt(),
                v.paidAt(),
                v.paymentMethod(),
                v.paymentNote(),
                v.submittedAt(),
                v.reviewedAt(),
                v.reviewedBy(),
                v.rejectionReason(),
                v.pricingBreakdown());
    }

    private RequirementResponse.Plan toResponse(PlatformSocietyModels.CustomPlan v) {
        return new RequirementResponse.Plan(
                v.id(),
                v.societyId(),
                v.requirementId(),
                v.planName(),
                v.description(),
                v.enabledModules(),
                v.maxSpaces(),
                v.maxSpaces(),
                v.maxSpaces(),
                v.maxStaff(),
                v.maxStorageGb(),
                v.priceMonthly(),
                v.pricePerSpace(),
                v.pricePerStaff(),
                v.setupFee(),
                code(v.billingCycle()),
                v.validFrom(),
                v.validUntil(),
                code(v.status()),
                v.createdBy(),
                v.createdAt(),
                v.activatedAt(),
                v.cancelledAt(),
                v.cancelledBy(),
                v.cancellationReason(),
                null,
                null);
    }

    private SupportResponse.Ticket toResponse(PlatformSocietyModels.SupportTicket v) {
        return new SupportResponse.Ticket(
                v.id(),
                v.societyId(),
                v.createdBy(),
                v.subject(),
                v.description(),
                code(v.priority()),
                code(v.status()),
                v.assignedTo(),
                v.resolution(),
                v.createdAt(),
                v.updatedAt(),
                v.closedAt(),
                null,
                0);
    }

    private RequirementResponse.Requirement toResponse(PlatformSocietyModels.Requirement v) {
        return new RequirementResponse.Requirement(
                v.id(),
                v.societyId(),
                v.societyName(),
                v.contactPerson(),
                v.email(),
                v.mobile(),
                v.city(),
                v.estimatedSpaces(),
                v.estimatedSpaces(),
                v.estimatedSpaces(),
                v.estimatedStaff(),
                v.requestedModules(),
                v.requirementDetails(),
                v.budgetMonthly(),
                code(v.status()),
                v.adminNotes(),
                v.assignedTo(),
                v.createdAt(),
                v.updatedAt(),
                v.closedAt(),
                null,
                null);
    }

    private PlatformNotificationResponse toResponse(PlatformNotificationView v) {
        return new PlatformNotificationResponse(
                v.id(),
                v.societyId(),
                v.societyName(),
                v.channel().code(),
                v.destination(),
                v.templateCode(),
                v.payload(),
                v.status().code(),
                v.attempts(),
                v.createdAt(),
                v.sentAt(),
                v.lastError());
    }

    private PlatformAdminResponse.AuditEvent toResponse(PlatformSocietyModels.AuditEvent v) {
        return new PlatformAdminResponse.AuditEvent(
                v.id(),
                v.societyId(),
                v.actorUserId(),
                v.action(),
                v.entityType(),
                v.entityId(),
                v.sensitive(),
                v.details(),
                v.occurredAt());
    }

    private String code(DomainCode value) {
        return value == null ? null : value.code();
    }
}
