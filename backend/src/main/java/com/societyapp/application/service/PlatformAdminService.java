package com.societyapp.application.service;

import com.societyapp.application.command.PlatformCatalogCoordinator;
import com.societyapp.application.command.PlatformSocietyCoordinator;
import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.mapper.PlatformSocietyViewMapper;
import com.societyapp.application.model.platformadmin.PlatformAdminModels;
import com.societyapp.application.model.platformadmin.PlatformSocietyModels;
import com.societyapp.application.policy.ServiceCatalogPolicy;
import com.societyapp.application.port.in.PlatformAdminUseCase;
import com.societyapp.application.port.in.PlatformNotificationUseCase;
import com.societyapp.application.port.in.SubscriptionQuantityUseCase;
import com.societyapp.application.port.out.JsonCodec;
import com.societyapp.application.port.out.PasswordCodec;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.query.PlatformDashboardQuery;
import com.societyapp.application.query.PlatformSocietyListQuery;
import com.societyapp.application.query.PlatformSocietyQuery;
import com.societyapp.application.usecase.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.SupportPriority;
import com.societyapp.domain.type.SupportTicketStatus;
import java.time.*;
import java.util.*;

public class PlatformAdminService implements PlatformAdminUseCase {
    private final SocietyRepository societies;
    private final SubscriptionPlanRepository plans;
    private final PlatformServiceModuleRepository serviceModules;
    private final SetupCodeRepository setupCodes;
    private final UserAccountJpaRepository users;
    private final SocietyDocumentRepository documents;
    private final BlockRepository blocks;
    private final FloorRepository floors;
    private final FlatRepository flats;
    private final ParkingSlotRepository parking;
    private final AmenityRepository amenities;
    private final HouseholdMemberRepository householdMembers;
    private final HouseholdVehicleRepository householdVehicles;
    private final HouseholdEmergencyContactRepository householdContacts;
    private final DeliveryRepository deliveries;
    private final EmergencyIncidentRepository emergencyIncidents;
    private final SocietyActivityRepository activities;
    private final ActivityParticipationRepository activityParticipations;
    private final ActivityGalleryPhotoRepository activityPhotos;
    private final SubscriptionOrderRepository orders;
    private final SocietyCustomPlanRepository customPlans;
    private final SupportTicketRepository tickets;
    private final SupportTicketMessageRepository ticketMessages;
    private final RequirementRequestRepository requirements;
    private final AuditEventRepository audits;
    private final PasswordCodec passwords;
    private final JsonCodec json;
    private final ServiceCatalogPolicy serviceCatalog;
    private final SubscriptionQuantityUseCase subscriptionQuantities;
    private final PlatformNotificationUseCase platformNotifications;
    private final PlatformDashboardQuery dashboardQuery;
    private final PlatformSocietyQuery societyQuery;
    private final PlatformSocietyListQuery societyListQuery;
    private final PlatformSocietyCoordinator societyCommands;
    private final PlatformCatalogCoordinator catalog;

    public PlatformAdminService(
            SocietyRepository societies,
            SubscriptionPlanRepository plans,
            PlatformServiceModuleRepository serviceModules,
            SetupCodeRepository setupCodes,
            UserAccountJpaRepository users,
            SocietyDocumentRepository documents,
            BlockRepository blocks,
            FloorRepository floors,
            FlatRepository flats,
            ParkingSlotRepository parking,
            AmenityRepository amenities,
            HouseholdMemberRepository householdMembers,
            HouseholdVehicleRepository householdVehicles,
            HouseholdEmergencyContactRepository householdContacts,
            DeliveryRepository deliveries,
            EmergencyIncidentRepository emergencyIncidents,
            SocietyActivityRepository activities,
            ActivityParticipationRepository activityParticipations,
            ActivityGalleryPhotoRepository activityPhotos,
            SubscriptionOrderRepository orders,
            SocietyCustomPlanRepository customPlans,
            SupportTicketRepository tickets,
            SupportTicketMessageRepository ticketMessages,
            RequirementRequestRepository requirements,
            AuditEventRepository audits,
            PasswordCodec passwords,
            JsonCodec json,
            ServiceCatalogPolicy serviceCatalog,
            SubscriptionQuantityUseCase subscriptionQuantities,
            PlatformNotificationUseCase platformNotifications) {
        this.societies = societies;
        this.plans = plans;
        this.serviceModules = serviceModules;
        this.setupCodes = setupCodes;
        this.users = users;
        this.documents = documents;
        this.blocks = blocks;
        this.floors = floors;
        this.flats = flats;
        this.parking = parking;
        this.amenities = amenities;
        this.householdMembers = householdMembers;
        this.householdVehicles = householdVehicles;
        this.householdContacts = householdContacts;
        this.deliveries = deliveries;
        this.emergencyIncidents = emergencyIncidents;
        this.activities = activities;
        this.activityParticipations = activityParticipations;
        this.activityPhotos = activityPhotos;
        this.orders = orders;
        this.customPlans = customPlans;
        this.tickets = tickets;
        this.ticketMessages = ticketMessages;
        this.requirements = requirements;
        this.audits = audits;
        this.passwords = passwords;
        this.json = json;
        this.serviceCatalog = serviceCatalog;
        this.subscriptionQuantities = subscriptionQuantities;
        this.platformNotifications = platformNotifications;
        var societyViewMapper = new PlatformSocietyViewMapper(json);
        this.dashboardQuery =
                new PlatformDashboardQuery(
                        societies,
                        plans,
                        users,
                        customPlans,
                        requirements,
                        tickets,
                        serviceCatalog,
                        platformNotifications);
        this.societyQuery =
                new PlatformSocietyQuery(
                        societies,
                        plans,
                        documents,
                        blocks,
                        floors,
                        flats,
                        parking,
                        amenities,
                        users,
                        householdMembers,
                        householdVehicles,
                        householdContacts,
                        deliveries,
                        emergencyIncidents,
                        activities,
                        activityParticipations,
                        activityPhotos,
                        orders,
                        customPlans,
                        tickets,
                        requirements,
                        audits,
                        platformNotifications,
                        societyViewMapper);
        this.societyListQuery =
                new PlatformSocietyListQuery(
                        societies,
                        documents,
                        blocks,
                        floors,
                        flats,
                        users,
                        subscriptionQuantities,
                        societyViewMapper);
        this.societyCommands =
                new PlatformSocietyCoordinator(
                        societies, plans, setupCodes, audits, passwords, json, serviceCatalog);
        this.catalog =
                new PlatformCatalogCoordinator(
                        societies, plans, serviceModules, audits, json, serviceCatalog);
    }

    public PlatformAdminModels.Dashboard dashboard() {
        return dashboardQuery.get();
    }

    public List<PlatformAdminModels.SocietySummary> societies(
            com.societyapp.domain.type.SocietyStatus status, String search) {
        return societyListQuery.get(status, search);
    }

    public PlatformAdminModels.SocietyCreated createSociety(
            UUID actorId, PlatformAdminModels.CreateSocietyCommand command) {
        return societyCommands.create(actorId, command);
    }

    public PlatformSocietyModels.Details society(UUID societyId) {
        return societyQuery.get(societyId);
    }

    public PlatformAdminModels.SocietyUpdated updateSociety(
            UUID actorId, UUID societyId, PlatformAdminModels.UpdateSocietyCommand command) {
        return societyCommands.update(actorId, societyId, command);
    }

    public List<PlatformAdminModels.PlanView> plans() {
        return catalog.plans();
    }

    public List<PlatformAdminModels.ServiceView> services() {
        return catalog.services();
    }

    public PlatformAdminModels.ServiceView createService(
            UUID actorId, PlatformAdminModels.ServiceCommand command) {
        return catalog.createService(actorId, command);
    }

    public PlatformAdminModels.ServiceView updateService(
            UUID actorId, String code, PlatformAdminModels.ServiceCommand command) {
        return catalog.updateService(actorId, code, command);
    }

    public PlatformAdminModels.PlanUpdated updatePlan(
            UUID actorId,
            com.societyapp.domain.type.SubscriptionPlanCode code,
            PlatformAdminModels.PlanCommand command) {
        return catalog.updatePlan(actorId, code, command);
    }

    public List<PlatformAdminModels.TicketView> tickets() {
        return tickets.findAllByOrderByCreatedAtDesc().stream()
                .sorted(Comparator.comparingInt(this::priorityOrder))
                .map(this::ticketView)
                .toList();
    }

    public PlatformAdminModels.TicketUpdated updateTicket(
            UUID actorId, UUID ticketId, SupportTicketStatus requestedStatus, String resolution) {
        var ticket =
                tickets.findById(ticketId)
                        .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        ticket.setStatus(requestedStatus.code());
        if (resolution != null) ticket.setResolution(resolution);
        ticket.setAssignedTo(actorId);
        ticket.setUpdatedAt(OffsetDateTime.now());
        ticket.setClosedAt(
                requestedStatus == SupportTicketStatus.RESOLVED
                                || requestedStatus == SupportTicketStatus.CLOSED
                        ? OffsetDateTime.now()
                        : null);
        tickets.save(ticket);
        return new PlatformAdminModels.TicketUpdated(ticketId, requestedStatus);
    }

    private PlatformAdminModels.TicketView ticketView(SupportTicket ticket) {
        String societyName =
                ticket.getSocietyId() == null
                        ? null
                        : societies
                                .findById(ticket.getSocietyId())
                                .map(Society::getName)
                                .orElse(null);
        String creatorName =
                ticket.getCreatedBy() == null
                        ? null
                        : users.findById(ticket.getCreatedBy())
                                .map(UserAccount::getFullName)
                                .orElse(null);
        return new PlatformAdminModels.TicketView(
                ticket.getId(),
                ticket.getSocietyId(),
                societyName,
                ticket.getCreatedBy(),
                creatorName,
                ticket.getSubject(),
                ticket.getDescription(),
                SupportPriority.from(ticket.getPriority()),
                SupportTicketStatus.from(ticket.getStatus()),
                ticket.getAssignedTo(),
                ticket.getResolution(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getClosedAt(),
                ticketMessages.findByTicketIdOrderByCreatedAt(ticket.getId()).size());
    }

    private int priorityOrder(SupportTicket ticket) {
        return SupportPriority.from(ticket.getPriority()).order();
    }
}
