package com.societyapp.application.query;

import com.societyapp.application.exception.AuthorizationException;
import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.model.delivery.DeliveryModels;
import com.societyapp.application.port.in.StructureAccessUseCase;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.util.*;

public final class DeliveryQueryCoordinator {
    private static final Set<DeliveryStatus> OPEN_STATUSES =
            Set.of(
                    DeliveryStatus.EXPECTED, DeliveryStatus.RECEIVED,
                    DeliveryStatus.READY_FOR_PICKUP, DeliveryStatus.OUT_FOR_DELIVERY,
                    DeliveryStatus.REJECTED, DeliveryStatus.DAMAGED);

    private final DeliveryRepository deliveries;
    private final DeliveryEventRepository events;
    private final UserAccountJpaRepository users;
    private final FlatRepository flats;
    private final FloorRepository floors;
    private final BlockRepository blocks;
    private final StructureAccessUseCase access;
    private final StaffPermissionRepository permissions;

    public DeliveryQueryCoordinator(
            DeliveryRepository deliveries,
            DeliveryEventRepository events,
            UserAccountJpaRepository users,
            FlatRepository flats,
            FloorRepository floors,
            BlockRepository blocks,
            StructureAccessUseCase access,
            StaffPermissionRepository permissions) {
        this.deliveries = deliveries;
        this.events = events;
        this.users = users;
        this.flats = flats;
        this.floors = floors;
        this.blocks = blocks;
        this.access = access;
        this.permissions = permissions;
    }

    public DeliveryModels.Workspace workspace(DeliveryModels.Actor actor) {
        if (actor.role() == UserRole.STAFF) requireManager(actor.userId(), actor.role());
        var rows =
                actor.role() == UserRole.RESIDENT
                        ? deliveries.findBySocietyIdAndResidentIdOrderByCreatedAtDesc(
                                actor.societyId(), actor.userId())
                        : deliveries.findBySocietyIdOrderByCreatedAtDesc(actor.societyId());
        return new DeliveryModels.Workspace(
                rows.stream().map(item -> view(item, actor.role())).toList(),
                options(actor),
                rows.size(),
                rows.stream()
                        .filter(
                                item ->
                                        OPEN_STATUSES.contains(
                                                DeliveryStatus.from(item.getStatus())))
                        .count(),
                rows.stream()
                        .filter(item -> DeliveryStatus.READY_FOR_PICKUP.matches(item.getStatus()))
                        .count(),
                rows.stream()
                        .filter(
                                item ->
                                        Set.of(DeliveryStatus.COLLECTED, DeliveryStatus.DELIVERED)
                                                .contains(DeliveryStatus.from(item.getStatus())))
                        .count(),
                rows.stream()
                        .filter(item -> PaymentStatus.PENDING.matches(item.getCodPaymentStatus()))
                        .count());
    }

    public List<DeliveryModels.HistoryEntry> history(DeliveryModels.Actor actor, UUID deliveryId) {
        if (actor.role() == UserRole.STAFF) requireManager(actor.userId(), actor.role());
        var delivery = requireDelivery(actor.societyId(), deliveryId);
        requireOwnerOrManager(delivery, actor.userId(), actor.role());
        return events.findByDeliveryIdOrderByOccurredAt(deliveryId).stream()
                .map(this::eventView)
                .toList();
    }

    public DeliveryModels.View view(Delivery item, UserRole role) {
        var resident = users.findById(item.getResidentId()).orElse(null);
        var flat = flats.findById(item.getFlatId()).orElse(null);
        String block =
                flat == null
                        ? null
                        : floors.findById(flat.getFloorId())
                                .flatMap(floor -> blocks.findById(floor.getBlockId()))
                                .map(Block::getName)
                                .orElse(null);
        return new DeliveryModels.View(
                item.getId(),
                item.getDeliveryNumber(),
                item.getTrackingNumber(),
                item.getProvider(),
                item.getSenderName(),
                item.getDeliveryPersonName(),
                item.getDeliveryPersonMobile(),
                ParcelType.from(item.getParcelType()),
                item.getPackageCount(),
                ParcelSize.from(item.getParcelSize()),
                item.isFragile(),
                item.getStorageLocation(),
                item.getExpectedAt(),
                item.getNotes(),
                DeliveryStatus.from(item.getStatus()),
                item.getResidentDecision() == null
                        ? null
                        : DeliveryDecision.from(item.getResidentDecision()),
                item.getAuthorizedCollector(),
                role == UserRole.RESIDENT ? item.getPickupCode() : null,
                item.getPickupCode() != null,
                item.getCodAmount(),
                PaymentStatus.from(item.getCodPaymentStatus()),
                item.getCodPaymentMethod() == null
                        ? null
                        : DeliveryPaymentMethod.from(item.getCodPaymentMethod()),
                item.getCodPaymentReference(),
                item.getReceivedAt(),
                item.getCollectedByName(),
                item.getCollectedAt(),
                item.getDeliveredAt(),
                item.getReturnedAt(),
                item.getClosedReason(),
                item.getCreatedAt(),
                item.getResidentId(),
                resident == null ? null : resident.getFullName(),
                resident == null ? null : resident.getMobile(),
                item.getFlatId(),
                flat == null ? null : flat.getFlatNumber(),
                block);
    }

    private List<DeliveryModels.ResidentSpace> options(DeliveryModels.Actor actor) {
        var result = new ArrayList<DeliveryModels.ResidentSpace>();
        var residents =
                actor.role() == UserRole.RESIDENT
                        ? List.of(requireResident(actor.societyId(), actor.userId()))
                        : users
                                .findBySocietyIdAndRoleOrderByFullName(
                                        actor.societyId(), UserRole.RESIDENT.code())
                                .stream()
                                .filter(user -> AccountStatus.ACTIVE.matches(user.getStatus()))
                                .toList();
        for (var resident : residents)
            for (var flat : flats.findAll()) {
                if (!access.belongsToSociety(flat.getId(), actor.societyId())
                        || !access.occupiedBy(flat.getId(), resident.getId())) continue;
                var block =
                        floors.findById(flat.getFloorId())
                                .flatMap(floor -> blocks.findById(floor.getBlockId()))
                                .map(Block::getName)
                                .orElse(null);
                result.add(
                        new DeliveryModels.ResidentSpace(
                                resident.getId(),
                                resident.getFullName(),
                                resident.getMobile(),
                                flat.getId(),
                                flat.getFlatNumber(),
                                block));
            }
        return result;
    }

    private DeliveryModels.HistoryEntry eventView(DeliveryEvent item) {
        return new DeliveryModels.HistoryEntry(
                item.getId(),
                DeliveryEventType.from(item.getEventType()),
                item.getFromStatus() == null ? null : DeliveryStatus.from(item.getFromStatus()),
                item.getToStatus() == null ? null : DeliveryStatus.from(item.getToStatus()),
                item.getNote(),
                UserRole.from(item.getActorRole()),
                item.getOccurredAt(),
                users.findById(item.getActorId()).map(UserAccount::getFullName).orElse(null));
    }

    private UserAccount requireResident(UUID societyId, UUID residentId) {
        return users.findById(residentId)
                .filter(user -> societyId.equals(user.getSocietyId()))
                .filter(user -> UserRole.RESIDENT.matches(user.getRole()))
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found"));
    }

    private Delivery requireDelivery(UUID societyId, UUID deliveryId) {
        return deliveries
                .findByIdAndSocietyId(deliveryId, societyId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
    }

    private void requireOwnerOrManager(Delivery item, UUID actorId, UserRole role) {
        if (role == UserRole.RESIDENT) {
            if (!actorId.equals(item.getResidentId()))
                throw new AuthorizationException("You can only manage your own deliveries");
        } else requireManager(actorId, role);
    }

    private void requireManager(UUID actorId, UserRole role) {
        if (role == UserRole.SOCIETY_ADMIN) return;
        if (role == UserRole.STAFF
                && permissions.existsByStaffUserIdAndPermissionCode(
                        actorId, StaffPermissionCode.DELIVERIES.code())) return;
        throw new AuthorizationException("Deliveries permission is required");
    }
}
