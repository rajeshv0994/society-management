package com.societyapp.application.command;

import com.societyapp.application.exception.AuthorizationException;
import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.model.delivery.DeliveryModels;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.query.DeliveryQueryCoordinator;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.*;

public final class DeliveryLifecycleCoordinator {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final DeliveryRepository deliveries;
    private final DeliveryEventRepository events;
    private final UserAccountJpaRepository users;
    private final StaffPermissionRepository permissions;
    private final NotificationOutboxRepository notifications;
    private final DeliveryQueryCoordinator queries;

    public DeliveryLifecycleCoordinator(
            DeliveryRepository deliveries,
            DeliveryEventRepository events,
            UserAccountJpaRepository users,
            StaffPermissionRepository permissions,
            NotificationOutboxRepository notifications,
            DeliveryQueryCoordinator queries) {
        this.deliveries = deliveries;
        this.events = events;
        this.users = users;
        this.permissions = permissions;
        this.notifications = notifications;
        this.queries = queries;
    }

    public DeliveryModels.View action(
            DeliveryModels.Actor actor, UUID deliveryId, DeliveryModels.ActionCommand command) {
        var item = requireDelivery(actor.societyId(), deliveryId);
        String oldStatus = item.getStatus();
        var requestedAction = command.action();
        String note = command.note();
        String collectorName = command.collectorName();
        String pickupCode = command.pickupCode();
        switch (requestedAction) {
            case ACCEPT -> {
                requireResidentOwner(item, actor.userId(), actor.role());
                requireStatus(item, DeliveryStatus.RECEIVED);
                item.setResidentDecision(DeliveryDecision.ACCEPTED.code());
                item.setResidentDecisionAt(OffsetDateTime.now());
                item.setStatus(DeliveryStatus.READY_FOR_PICKUP.code());
            }
            case REJECT -> {
                requireResidentOwner(item, actor.userId(), actor.role());
                requireOneOf(
                        item,
                        "Only a received delivery can be rejected",
                        DeliveryStatus.RECEIVED,
                        DeliveryStatus.READY_FOR_PICKUP);
                requireNote(note, "Rejection reason is required");
                item.setResidentDecision(DeliveryDecision.REJECTED.code());
                item.setResidentDecisionAt(OffsetDateTime.now());
                item.setClosedReason(note.trim());
                item.setStatus(DeliveryStatus.REJECTED.code());
            }
            case AUTHORIZE -> {
                requireResidentOwner(item, actor.userId(), actor.role());
                requireOneOf(
                        item,
                        "Delivery is not available for authorization",
                        DeliveryStatus.RECEIVED,
                        DeliveryStatus.READY_FOR_PICKUP);
                if (collectorName == null || collectorName.isBlank())
                    throw new IllegalArgumentException("Authorized collector name is required");
                item.setAuthorizedCollector(collectorName.trim());
                item.setResidentDecision(DeliveryDecision.ACCEPTED.code());
                item.setResidentDecisionAt(OffsetDateTime.now());
                item.setStatus(DeliveryStatus.READY_FOR_PICKUP.code());
            }
            case CANCEL -> {
                requireResidentOwner(item, actor.userId(), actor.role());
                requireStatus(item, DeliveryStatus.EXPECTED);
                item.setStatus(DeliveryStatus.CANCELLED.code());
                item.setClosedReason(blank(note));
            }
            case RECEIVE -> {
                requireManager(actor.userId(), actor.role());
                requireStatus(item, DeliveryStatus.EXPECTED);
                item.setStatus(DeliveryStatus.RECEIVED.code());
                item.setReceivedBy(actor.userId());
                item.setReceivedAt(OffsetDateTime.now());
                item.setPickupCode(pickupCode());
                notifyResident(
                        requireResident(actor.societyId(), item.getResidentId()),
                        item,
                        "DELIVERY_RECEIVED");
            }
            case MARK_READY -> {
                requireManager(actor.userId(), actor.role());
                requireStatus(item, DeliveryStatus.RECEIVED);
                item.setStatus(DeliveryStatus.READY_FOR_PICKUP.code());
                notifyResident(
                        requireResident(actor.societyId(), item.getResidentId()),
                        item,
                        "DELIVERY_READY");
            }
            case OUT_FOR_DELIVERY -> {
                requireManager(actor.userId(), actor.role());
                requireAvailable(item);
                requireCodPaid(item);
                item.setStatus(DeliveryStatus.OUT_FOR_DELIVERY.code());
            }
            case HANDOVER -> {
                requireManager(actor.userId(), actor.role());
                requireOneOf(
                        item,
                        "Delivery is not ready for handover",
                        DeliveryStatus.RECEIVED,
                        DeliveryStatus.READY_FOR_PICKUP);
                requireCodPaid(item);
                if (pickupCode == null || !pickupCode.equals(item.getPickupCode()))
                    throw new IllegalArgumentException("Invalid pickup code");
                item.setCollectedByName(
                        collectorName == null || collectorName.isBlank()
                                ? requireResident(actor.societyId(), item.getResidentId())
                                        .getFullName()
                                : collectorName.trim());
                item.setHandedOverBy(actor.userId());
                item.setCollectedAt(OffsetDateTime.now());
                item.setStatus(DeliveryStatus.COLLECTED.code());
            }
            case DELIVER -> {
                requireManager(actor.userId(), actor.role());
                requireOneOf(
                        item,
                        "Delivery cannot be delivered in its current state",
                        DeliveryStatus.RECEIVED,
                        DeliveryStatus.READY_FOR_PICKUP,
                        DeliveryStatus.OUT_FOR_DELIVERY);
                requireCodPaid(item);
                item.setHandedOverBy(actor.userId());
                item.setCollectedByName(blank(collectorName));
                item.setDeliveredAt(OffsetDateTime.now());
                item.setStatus(DeliveryStatus.DELIVERED.code());
            }
            case RETURN -> {
                requireManager(actor.userId(), actor.role());
                requireOneOf(
                        item,
                        "Delivery cannot be returned in its current state",
                        DeliveryStatus.RECEIVED,
                        DeliveryStatus.READY_FOR_PICKUP,
                        DeliveryStatus.REJECTED,
                        DeliveryStatus.DAMAGED);
                requireNote(note, "Return reason is required");
                item.setClosedReason(note.trim());
                item.setReturnedAt(OffsetDateTime.now());
                item.setStatus(DeliveryStatus.RETURNED.code());
            }
            case DAMAGED -> {
                requireManager(actor.userId(), actor.role());
                requireAvailable(item);
                requireNote(note, "Damage details are required");
                item.setClosedReason(note.trim());
                item.setStatus(DeliveryStatus.DAMAGED.code());
            }
            case LOST -> {
                requireManager(actor.userId(), actor.role());
                requireAvailable(item);
                requireNote(note, "Loss details are required");
                item.setClosedReason(note.trim());
                item.setStatus(DeliveryStatus.LOST.code());
            }
        }
        item.setUpdatedAt(OffsetDateTime.now());
        item = deliveries.save(item);
        addEvent(
                item,
                actor.userId(),
                actor.role(),
                DeliveryEventType.from(requestedAction),
                oldStatus,
                item.getStatus(),
                note);
        return queries.view(item, actor.role());
    }

    private void requireOneOf(Delivery item, String message, DeliveryStatus... statuses) {
        if (Arrays.stream(statuses).noneMatch(status -> status.matches(item.getStatus())))
            throw new IllegalStateException(message);
    }

    private void requireStatus(Delivery item, DeliveryStatus status) {
        if (!status.matches(item.getStatus()))
            throw new IllegalStateException(
                    "Delivery must be " + status.code().toLowerCase(Locale.ROOT));
    }

    private void requireAvailable(Delivery item) {
        requireOneOf(
                item,
                "Delivery is not available",
                DeliveryStatus.RECEIVED,
                DeliveryStatus.READY_FOR_PICKUP,
                DeliveryStatus.OUT_FOR_DELIVERY);
    }

    private void requireCodPaid(Delivery item) {
        if (item.getCodAmount().signum() > 0
                && !PaymentStatus.PAID.matches(item.getCodPaymentStatus()))
            throw new IllegalStateException("COD payment must be completed before handover");
    }

    private void requireResidentOwner(Delivery item, UUID actorId, UserRole role) {
        if (role != UserRole.RESIDENT || !actorId.equals(item.getResidentId()))
            throw new AuthorizationException("You can only manage your own deliveries");
    }

    private void requireManager(UUID actorId, UserRole role) {
        if (role == UserRole.SOCIETY_ADMIN) return;
        if (role == UserRole.STAFF
                && permissions.existsByStaffUserIdAndPermissionCode(
                        actorId, StaffPermissionCode.DELIVERIES.code())) return;
        throw new AuthorizationException("Deliveries permission is required");
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

    private void notifyResident(UserAccount resident, Delivery item, String template) {
        if (resident.getEmail() == null || resident.getEmail().isBlank()) return;
        var notification = new NotificationOutbox();
        notification.setSocietyId(item.getSocietyId());
        notification.setChannel(com.societyapp.domain.type.NotificationChannel.EMAIL.code());
        notification.setDestination(resident.getEmail());
        notification.setTemplateCode(template);
        notification.setPayload(
                "{\"deliveryNumber\":\""
                        + item.getDeliveryNumber()
                        + "\",\"provider\":\""
                        + item.getProvider().replace("\"", "")
                        + "\"}");
        notification.setStatus(NotificationStatus.PENDING.code());
        notification.setAttempts(0);
        notification.setCreatedAt(OffsetDateTime.now());
        notifications.save(notification);
    }

    private void addEvent(
            Delivery item,
            UUID actorId,
            UserRole role,
            DeliveryEventType type,
            String fromStatus,
            String toStatus,
            String note) {
        var event = new DeliveryEvent();
        event.setDeliveryId(item.getId());
        event.setSocietyId(item.getSocietyId());
        event.setEventType(type.code());
        event.setFromStatus(fromStatus);
        event.setToStatus(toStatus);
        event.setNote(blank(note));
        event.setActorId(actorId);
        event.setActorRole(role.code());
        event.setOccurredAt(OffsetDateTime.now());
        events.save(event);
    }

    private void requireNote(String note, String message) {
        if (note == null || note.isBlank()) throw new IllegalArgumentException(message);
    }

    private String pickupCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
