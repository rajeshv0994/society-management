package com.societyapp.application.service;

import com.societyapp.application.command.DeliveryLifecycleCoordinator;
import com.societyapp.application.exception.AuthorizationException;
import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.model.delivery.DeliveryModels;
import com.societyapp.application.port.in.DeliveryUseCase;
import com.societyapp.application.port.in.StructureAccessUseCase;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.query.DeliveryQueryCoordinator;
import com.societyapp.application.usecase.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.DeliveryEventType;
import com.societyapp.domain.type.DeliveryPaymentMethod;
import com.societyapp.domain.type.DeliveryStatus;
import com.societyapp.domain.type.NotificationStatus;
import com.societyapp.domain.type.PaymentMethod;
import com.societyapp.domain.type.PaymentStatus;
import com.societyapp.domain.type.StaffPermissionCode;
import com.societyapp.domain.type.UserRole;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DeliveryService implements DeliveryUseCase {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Set<PaymentMethod> ONLINE_METHODS =
            Set.of(PaymentMethod.UPI, PaymentMethod.CARD, PaymentMethod.NET_BANKING);

    private final DeliveryRepository deliveries;
    private final DeliveryEventRepository events;
    private final UserAccountJpaRepository users;
    private final FlatRepository flats;
    private final FloorRepository floors;
    private final BlockRepository blocks;
    private final StructureAccessUseCase access;
    private final StaffPermissionRepository permissions;
    private final SocietyPaymentSettingRepository paymentSettings;
    private final NotificationOutboxRepository notifications;
    private final DeliveryQueryCoordinator queries;
    private final DeliveryLifecycleCoordinator lifecycle;

    public DeliveryService(
            DeliveryRepository deliveries,
            DeliveryEventRepository events,
            UserAccountJpaRepository users,
            FlatRepository flats,
            FloorRepository floors,
            BlockRepository blocks,
            StructureAccessUseCase access,
            StaffPermissionRepository permissions,
            SocietyPaymentSettingRepository paymentSettings,
            NotificationOutboxRepository notifications) {
        this.deliveries = deliveries;
        this.events = events;
        this.users = users;
        this.flats = flats;
        this.floors = floors;
        this.blocks = blocks;
        this.access = access;
        this.permissions = permissions;
        this.paymentSettings = paymentSettings;
        this.notifications = notifications;
        this.queries =
                new DeliveryQueryCoordinator(
                        deliveries, events, users, flats, floors, blocks, access, permissions);
        this.lifecycle =
                new DeliveryLifecycleCoordinator(
                        deliveries, events, users, permissions, notifications, queries);
    }

    @Override
    public DeliveryModels.Workspace workspace(DeliveryModels.Actor actor) {
        return queries.workspace(actor);
    }

    @Override
    public List<DeliveryModels.HistoryEntry> history(DeliveryModels.Actor actor, UUID deliveryId) {
        return queries.history(actor, deliveryId);
    }

    @Override
    public DeliveryModels.View create(
            DeliveryModels.Actor actor, DeliveryModels.CreateCommand command) {
        UUID residentId;
        boolean receivedNow = command.receivedNow();
        if (actor.role() == UserRole.RESIDENT) {
            residentId = actor.userId();
            receivedNow = false;
        } else {
            requireManager(actor.userId(), actor.role());
            if (command.residentId() == null) {
                throw new IllegalArgumentException("Resident is required");
            }
            residentId = command.residentId();
        }
        var resident = requireResident(actor.societyId(), residentId);
        access.requireFlat(command.spaceId(), actor.societyId());
        if (!access.occupiedBy(command.spaceId(), residentId)) {
            throw new IllegalArgumentException("Resident does not occupy the selected space");
        }
        if (!receivedNow
                && command.expectedAt() != null
                && command.expectedAt().isBefore(OffsetDateTime.now().minusMinutes(5))) {
            throw new IllegalArgumentException("Expected delivery time cannot be in the past");
        }
        var item = new Delivery();
        item.setSocietyId(actor.societyId());
        item.setFlatId(command.spaceId());
        item.setResidentId(residentId);
        item.setDeliveryNumber(deliveryNumber());
        item.setTrackingNumber(normalizeTracking(command.trackingNumber()));
        item.setProvider(command.provider().trim());
        item.setSenderName(blankToNull(command.senderName()));
        item.setDeliveryPersonName(blankToNull(command.deliveryPersonName()));
        item.setDeliveryPersonMobile(blankToNull(command.deliveryPersonMobile()));
        item.setParcelType(command.parcelType().code());
        item.setPackageCount(command.packageCount());
        item.setParcelSize(command.parcelSize().code());
        item.setFragile(command.fragile());
        item.setStorageLocation(blankToNull(command.storageLocation()));
        item.setExpectedAt(command.expectedAt());
        item.setNotes(blankToNull(command.notes()));
        item.setCodAmount(command.codAmount() == null ? BigDecimal.ZERO : command.codAmount());
        item.setCodPaymentStatus(
                item.getCodAmount().signum() > 0
                        ? PaymentStatus.PENDING.code()
                        : PaymentStatus.NOT_REQUIRED.code());
        item.setStatus(
                receivedNow ? DeliveryStatus.RECEIVED.code() : DeliveryStatus.EXPECTED.code());
        item.setCreatedBy(actor.userId());
        item.setCreatedAt(OffsetDateTime.now());
        item.setUpdatedAt(OffsetDateTime.now());
        if (receivedNow) {
            item.setReceivedBy(actor.userId());
            item.setReceivedAt(OffsetDateTime.now());
            item.setPickupCode(pickupCode());
        }
        item = deliveries.save(item);
        addEvent(
                item,
                actor.userId(),
                actor.role(),
                DeliveryEventType.CREATED,
                null,
                item.getStatus(),
                command.notes());
        if (receivedNow) notifyResident(resident, item, "DELIVERY_RECEIVED");
        return queries.view(item, actor.role());
    }

    @Override
    public DeliveryModels.View action(
            DeliveryModels.Actor actor, UUID deliveryId, DeliveryModels.ActionCommand command) {
        return lifecycle.action(actor, deliveryId, command);
    }

    @Override
    public DeliveryModels.View checkout(
            DeliveryModels.Actor actor, UUID deliveryId, DeliveryModels.PaymentCommand command) {
        var requestedMethod = command.paymentMethod();
        if (!ONLINE_METHODS.contains(requestedMethod)) {
            throw new IllegalArgumentException("Invalid Razorpay payment method");
        }
        var item = requireDelivery(actor.societyId(), deliveryId);
        requireOwnerOrManager(item, actor.userId(), actor.role());
        requirePendingCod(item);
        var setting =
                paymentSettings
                        .findBySocietyId(actor.societyId())
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Society online payment account is not configured"));
        if (!setting.isTestMode()) {
            throw new IllegalStateException("Live Razorpay delivery payments are not enabled yet");
        }
        item.setCodPaymentStatus(PaymentStatus.PAID.code());
        item.setCodPaymentMethod(
                DeliveryPaymentMethod.valueOf("RAZORPAY_" + requestedMethod.name()).code());
        item.setCodPaymentReference(
                "pay_delivery_test_"
                        + UUID.randomUUID().toString().replace("-", "").substring(0, 18));
        item.setCodPaidAt(OffsetDateTime.now());
        item.setUpdatedAt(OffsetDateTime.now());
        item = deliveries.save(item);
        addEvent(
                item,
                actor.userId(),
                actor.role(),
                DeliveryEventType.COD_PAID_ONLINE,
                item.getStatus(),
                item.getStatus(),
                null);
        return queries.view(item, actor.role());
    }

    @Override
    public DeliveryModels.View recordCod(
            DeliveryModels.Actor actor, UUID deliveryId, DeliveryModels.PaymentCommand command) {
        requireManager(actor.userId(), actor.role());
        var requestedMethod = command.paymentMethod();
        if (!Set.of(PaymentMethod.CASH, PaymentMethod.UPI, PaymentMethod.CARD)
                .contains(requestedMethod)) {
            throw new IllegalArgumentException("Invalid COD collection method");
        }
        var item = requireDelivery(actor.societyId(), deliveryId);
        requirePendingCod(item);
        item.setCodPaymentStatus(PaymentStatus.PAID.code());
        item.setCodPaymentMethod(requestedMethod.code());
        item.setCodPaymentReference(
                "COD-"
                        + UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 12)
                                .toUpperCase());
        item.setCodPaidAt(OffsetDateTime.now());
        item.setUpdatedAt(OffsetDateTime.now());
        item = deliveries.save(item);
        addEvent(
                item,
                actor.userId(),
                actor.role(),
                DeliveryEventType.COD_RECORDED,
                item.getStatus(),
                item.getStatus(),
                requestedMethod.code());
        return queries.view(item, actor.role());
    }

    private void requireOwnerOrManager(Delivery item, UUID actorId, UserRole role) {
        if (role == UserRole.RESIDENT) {
            requireResidentOwner(item, actorId, role);
        } else {
            requireManager(actorId, role);
        }
    }

    private void requireResidentOwner(Delivery item, UUID actorId, UserRole role) {
        if (role != UserRole.RESIDENT || !actorId.equals(item.getResidentId())) {
            throw new AuthorizationException("You can only manage your own deliveries");
        }
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

    private void requirePendingCod(Delivery item) {
        if (!PaymentStatus.PENDING.matches(item.getCodPaymentStatus())) {
            throw new IllegalStateException("Delivery does not have a pending COD payment");
        }
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
            DeliveryEventType eventType,
            String fromStatus,
            String toStatus,
            String note) {
        var event = new DeliveryEvent();
        event.setDeliveryId(item.getId());
        event.setSocietyId(item.getSocietyId());
        event.setEventType(eventType.code());
        event.setFromStatus(fromStatus);
        event.setToStatus(toStatus);
        event.setNote(blankToNull(note));
        event.setActorId(actorId);
        event.setActorRole(role.code());
        event.setOccurredAt(OffsetDateTime.now());
        events.save(event);
    }

    private String deliveryNumber() {
        return "DLV-"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"))
                + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private String pickupCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private String normalizeTracking(String value) {
        return value == null || value.isBlank()
                ? null
                : value.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
