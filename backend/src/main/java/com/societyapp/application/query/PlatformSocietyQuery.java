package com.societyapp.application.query;

import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.mapper.PlatformSocietyViewMapper;
import com.societyapp.application.model.platformadmin.PlatformSocietyModels;
import com.societyapp.application.port.in.PlatformNotificationUseCase;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.type.*;
import java.util.*;

public final class PlatformSocietyQuery {
    private final SocietyRepository societies;
    private final SubscriptionPlanRepository plans;
    private final SocietyDocumentRepository documents;
    private final BlockRepository blocks;
    private final FloorRepository floors;
    private final FlatRepository flats;
    private final ParkingSlotRepository parking;
    private final AmenityRepository amenities;
    private final UserAccountJpaRepository users;
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
    private final RequirementRequestRepository requirements;
    private final AuditEventRepository audits;
    private final PlatformNotificationUseCase notifications;
    private final PlatformSocietyViewMapper mapper;

    public PlatformSocietyQuery(
            SocietyRepository societies,
            SubscriptionPlanRepository plans,
            SocietyDocumentRepository documents,
            BlockRepository blocks,
            FloorRepository floors,
            FlatRepository flats,
            ParkingSlotRepository parking,
            AmenityRepository amenities,
            UserAccountJpaRepository users,
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
            RequirementRequestRepository requirements,
            AuditEventRepository audits,
            PlatformNotificationUseCase notifications,
            PlatformSocietyViewMapper mapper) {
        this.societies = societies;
        this.plans = plans;
        this.documents = documents;
        this.blocks = blocks;
        this.floors = floors;
        this.flats = flats;
        this.parking = parking;
        this.amenities = amenities;
        this.users = users;
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
        this.requirements = requirements;
        this.audits = audits;
        this.notifications = notifications;
        this.mapper = mapper;
    }

    public PlatformSocietyModels.Details get(UUID societyId) {
        var society =
                societies
                        .findById(societyId)
                        .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
        var societyBlocks = blocks.findBySocietyIdOrderByName(societyId);
        var hierarchy =
                societyBlocks.stream()
                        .map(
                                block ->
                                        mapper.block(
                                                block,
                                                floors
                                                        .findByBlockIdOrderByFloorNumber(
                                                                block.getId())
                                                        .stream()
                                                        .map(
                                                                floor ->
                                                                        mapper.section(
                                                                                floor,
                                                                                flats
                                                                                        .findByFloorIdOrderByFlatNumber(
                                                                                                floor
                                                                                                        .getId())
                                                                                        .stream()
                                                                                        .map(
                                                                                                mapper
                                                                                                        ::space)
                                                                                        .toList()))
                                                        .toList()))
                        .toList();
        long sectionCount = hierarchy.stream().mapToLong(x -> x.sections().size()).sum();
        long spaceCount =
                hierarchy.stream()
                        .flatMap(x -> x.sections().stream())
                        .mapToLong(x -> x.spaces().size())
                        .sum();
        var structure =
                new PlatformSocietyModels.Structure(
                        societyBlocks.size(),
                        sectionCount,
                        spaceCount,
                        parking.findBySocietyIdOrderBySlotNumber(societyId).size(),
                        amenities.findBySocietyIdOrderByName(societyId).size(),
                        hierarchy);
        var societyUsers =
                users.findAll().stream()
                        .filter(user -> societyId.equals(user.getSocietyId()))
                        .map(
                                user ->
                                        new PlatformSocietyModels.User(
                                                user.getId(),
                                                user.getFullName(),
                                                user.getEmail(),
                                                user.getMobile(),
                                                UserRole.from(user.getRole()),
                                                AccountStatus.from(user.getStatus()),
                                                user.getCreatedAt()))
                        .toList();
        long residents =
                societyUsers.stream()
                        .filter(
                                user ->
                                        user.role() == UserRole.RESIDENT
                                                && user.status() == AccountStatus.ACTIVE)
                        .count();
        var household =
                new PlatformSocietyModels.HouseholdSummary(
                        residents,
                        householdMembers.countBySocietyIdAndActiveTrue(societyId),
                        householdVehicles.countBySocietyIdAndActiveTrue(societyId),
                        householdContacts.countBySocietyIdAndActiveTrue(societyId),
                        householdMembers.countBySocietyIdAndActiveTrueAndVerificationStatus(
                                        societyId, VerificationStatus.PENDING.code())
                                + householdVehicles
                                        .countBySocietyIdAndActiveTrueAndVerificationStatus(
                                                societyId, VerificationStatus.PENDING.code()));
        var delivery =
                new PlatformSocietyModels.DeliverySummary(
                        deliveries.countBySocietyId(societyId),
                        deliveries.countBySocietyIdAndStatusIn(
                                societyId,
                                Set.of(
                                        DeliveryStatus.EXPECTED.code(),
                                        DeliveryStatus.RECEIVED.code(),
                                        DeliveryStatus.READY_FOR_PICKUP.code(),
                                        DeliveryStatus.OUT_FOR_DELIVERY.code(),
                                        DeliveryStatus.REJECTED.code(),
                                        DeliveryStatus.DAMAGED.code())));
        var open =
                Set.of(
                        EmergencyIncidentStatus.REPORTED.code(),
                        EmergencyIncidentStatus.ACKNOWLEDGED.code(),
                        EmergencyIncidentStatus.RESPONDING.code(),
                        EmergencyIncidentStatus.ON_SCENE.code(),
                        EmergencyIncidentStatus.STABILIZED.code());
        var emergency =
                new PlatformSocietyModels.EmergencySummary(
                        emergencyIncidents.countBySocietyId(societyId),
                        emergencyIncidents.countBySocietyIdAndStatusIn(societyId, open),
                        emergencyIncidents.countBySocietyIdAndSeverityAndStatusIn(
                                societyId, IncidentSeverity.CRITICAL.code(), open));
        var activity =
                new PlatformSocietyModels.ActivitySummary(
                        activities.countBySocietyId(societyId),
                        activities.countBySocietyIdAndStatus(
                                societyId, ActivityStatus.PUBLISHED.code()),
                        activityParticipations.countBySocietyId(societyId),
                        activityPhotos.countBySocietyIdAndModerationStatus(
                                societyId, ModerationStatus.PUBLISHED.code()),
                        activityPhotos.countBySocietyIdAndModerationStatus(
                                societyId, ModerationStatus.PENDING.code()));
        return new PlatformSocietyModels.Details(
                mapper.society(society),
                mapper.plan(plans.findById(society.getPlanCode()).orElse(null)),
                structure,
                societyUsers,
                household,
                delivery,
                emergency,
                activity,
                documents.findBySocietyId(societyId).stream().map(mapper::document).toList(),
                orders.findAll().stream()
                        .filter(x -> societyId.equals(x.getSocietyId()))
                        .map(mapper::order)
                        .toList(),
                customPlans.findAll().stream()
                        .filter(x -> societyId.equals(x.getSocietyId()))
                        .map(mapper::customPlan)
                        .toList(),
                tickets.findBySocietyIdOrderByCreatedAtDesc(societyId).stream()
                        .map(mapper::support)
                        .toList(),
                requirements.findBySocietyIdOrderByCreatedAtDesc(societyId).stream()
                        .map(mapper::requirement)
                        .toList(),
                notifications.findBySociety(societyId),
                audits.findBySocietyIdOrderByOccurredAtDesc(societyId).stream()
                        .map(mapper::audit)
                        .toList());
    }
}
