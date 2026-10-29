package com.societyapp.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.societyapp.application.exception.AuthorizationException;
import com.societyapp.application.model.delivery.DeliveryModels;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.service.DeliveryService;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeliveryServiceTest {
    private final DeliveryRepository deliveries = mock(DeliveryRepository.class);
    private final DeliveryEventRepository events = mock(DeliveryEventRepository.class);
    private final UserAccountJpaRepository users = mock(UserAccountJpaRepository.class);
    private final FlatRepository flats = mock(FlatRepository.class);
    private final FloorRepository floors = mock(FloorRepository.class);
    private final BlockRepository blocks = mock(BlockRepository.class);
    private final StructureAccessService access = mock(StructureAccessService.class);
    private final StaffPermissionRepository permissions = mock(StaffPermissionRepository.class);
    private final SocietyPaymentSettingRepository paymentSettings =
            mock(SocietyPaymentSettingRepository.class);
    private final NotificationOutboxRepository notifications =
            mock(NotificationOutboxRepository.class);
    private final DeliveryService typedService =
            new DeliveryService(
                    deliveries,
                    events,
                    users,
                    flats,
                    floors,
                    blocks,
                    access,
                    permissions,
                    paymentSettings,
                    notifications);
    private final DeliveryTestFacade service = new DeliveryTestFacade(typedService);

    private final UUID societyId = UUID.randomUUID();
    private final UUID residentId = UUID.randomUUID();
    private final UUID staffId = UUID.randomUUID();
    private final UUID flatId = UUID.randomUUID();
    private final UUID deliveryId = UUID.randomUUID();

    @BeforeEach
    void defaults() {
        when(deliveries.save(any(Delivery.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(events.save(any(DeliveryEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notifications.save(any(NotificationOutbox.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(users.findById(residentId)).thenReturn(Optional.of(resident()));
        when(flats.findById(flatId)).thenReturn(Optional.empty());
        when(access.occupiedBy(flatId, residentId)).thenReturn(true);
    }

    @Test
    void residentWorkspaceOnlyReturnsOwnDeliveries() {
        when(deliveries.findBySocietyIdAndResidentIdOrderByCreatedAtDesc(societyId, residentId))
                .thenReturn(List.of(delivery("READY_FOR_PICKUP", "NOT_REQUIRED")));

        var result = service.workspace(societyId, residentId, "RESIDENT");

        assertThat((List<?>) result.get("deliveries")).hasSize(1);
        assertThat(result)
                .containsEntry("total", 1)
                .containsEntry("ready_for_pickup", 1L)
                .containsEntry("completed", 0L);
    }

    @Test
    void staffRequiresDeliveriesPermission() {
        assertThatThrownBy(() -> service.workspace(societyId, staffId, "STAFF"))
                .isInstanceOf(AuthorizationException.class);

        when(permissions.existsByStaffUserIdAndPermissionCode(staffId, "DELIVERIES"))
                .thenReturn(true);
        when(deliveries.findBySocietyIdOrderByCreatedAtDesc(societyId)).thenReturn(List.of());
        assertThat(service.workspace(societyId, staffId, "STAFF")).containsEntry("total", 0);
    }

    @Test
    void residentCanPreRegisterExpectedDeliveryAndTrackingIsNormalized() {
        var result =
                service.create(
                        societyId,
                        residentId,
                        "RESIDENT",
                        UUID.randomUUID(),
                        flatId,
                        " ab 12 34 ",
                        "Amazon",
                        "Store",
                        null,
                        null,
                        "PACKAGE",
                        1,
                        "MEDIUM",
                        false,
                        null,
                        OffsetDateTime.now().plusDays(1),
                        null,
                        BigDecimal.ZERO,
                        true);

        assertThat(result)
                .containsEntry("tracking_number", "AB1234")
                .containsEntry("status", "EXPECTED")
                .containsEntry("cod_payment_status", "NOT_REQUIRED")
                .containsEntry("pickup_code_issued", false);
    }

    @Test
    void residentCannotRegisterDeliveryForAnUnoccupiedSpace() {
        when(access.occupiedBy(flatId, residentId)).thenReturn(false);

        assertThatThrownBy(
                        () ->
                                service.create(
                                        societyId,
                                        residentId,
                                        "RESIDENT",
                                        null,
                                        flatId,
                                        null,
                                        "Courier",
                                        null,
                                        null,
                                        null,
                                        "PACKAGE",
                                        1,
                                        "MEDIUM",
                                        false,
                                        null,
                                        OffsetDateTime.now().plusDays(1),
                                        null,
                                        BigDecimal.ZERO,
                                        false))
                .hasMessageContaining("does not occupy");
    }

    @Test
    void managerCanLogUnannouncedArrivalAndResidentIsNotified() {
        var result =
                service.create(
                        societyId,
                        staffId,
                        "SOCIETY_ADMIN",
                        residentId,
                        flatId,
                        "TRACK1",
                        "BlueDart",
                        "Sender",
                        "Courier Person",
                        "9000000000",
                        "DOCUMENT",
                        1,
                        "SMALL",
                        true,
                        "Desk A",
                        null,
                        "Handle carefully",
                        new BigDecimal("250"),
                        true);

        assertThat(result)
                .containsEntry("status", "RECEIVED")
                .containsEntry("pickup_code_issued", true)
                .containsEntry("cod_payment_status", "PENDING");
        verify(notifications).save(any(NotificationOutbox.class));
    }

    @Test
    void receiveExpectedDeliveryIssuesCodeAndAcceptMakesItReady() {
        Delivery item = delivery("EXPECTED", "NOT_REQUIRED");
        stubDelivery(item);

        var received =
                service.action(
                        societyId,
                        staffId,
                        "SOCIETY_ADMIN",
                        deliveryId,
                        "RECEIVE",
                        null,
                        null,
                        null);
        assertThat(received)
                .containsEntry("status", "RECEIVED")
                .containsEntry("pickup_code_issued", true);

        var ready =
                service.action(
                        societyId, residentId, "RESIDENT", deliveryId, "ACCEPT", null, null, null);
        assertThat(ready)
                .containsEntry("status", "READY_FOR_PICKUP")
                .containsEntry("resident_decision", "ACCEPTED");
    }

    @Test
    void residentCanAuthorizeCollectorAndRejectionRequiresReason() {
        Delivery item = delivery("RECEIVED", "NOT_REQUIRED");
        stubDelivery(item);

        assertThatThrownBy(
                        () ->
                                service.action(
                                        societyId,
                                        residentId,
                                        "RESIDENT",
                                        deliveryId,
                                        "REJECT",
                                        " ",
                                        null,
                                        null))
                .hasMessageContaining("Rejection reason");

        var authorized =
                service.action(
                        societyId,
                        residentId,
                        "RESIDENT",
                        deliveryId,
                        "AUTHORIZE",
                        null,
                        "Family Member",
                        null);
        assertThat(authorized)
                .containsEntry("status", "READY_FOR_PICKUP")
                .containsEntry("authorized_collector", "Family Member");
    }

    @Test
    void handoverRequiresPaidCodAndCorrectPickupCode() {
        Delivery item = delivery("READY_FOR_PICKUP", "PENDING");
        item.setCodAmount(new BigDecimal("400"));
        item.setPickupCode("123456");
        stubDelivery(item);

        assertThatThrownBy(
                        () ->
                                service.action(
                                        societyId,
                                        staffId,
                                        "SOCIETY_ADMIN",
                                        deliveryId,
                                        "HANDOVER",
                                        null,
                                        "Resident",
                                        "123456"))
                .hasMessageContaining("COD payment");

        service.recordCod(societyId, staffId, "SOCIETY_ADMIN", deliveryId, "CASH");
        assertThatThrownBy(
                        () ->
                                service.action(
                                        societyId,
                                        staffId,
                                        "SOCIETY_ADMIN",
                                        deliveryId,
                                        "HANDOVER",
                                        null,
                                        "Resident",
                                        "000000"))
                .hasMessageContaining("Invalid pickup code");

        var result =
                service.action(
                        societyId,
                        staffId,
                        "SOCIETY_ADMIN",
                        deliveryId,
                        "HANDOVER",
                        null,
                        "Resident",
                        "123456");
        assertThat(result)
                .containsEntry("status", "COLLECTED")
                .containsEntry("cod_payment_status", "PAID")
                .containsEntry("collected_by_name", "Resident");
    }

    @Test
    void onlineCodRequiresOwnerConfiguredTestAccountAndValidMethod() {
        Delivery item = delivery("READY_FOR_PICKUP", "PENDING");
        item.setCodAmount(new BigDecimal("200"));
        stubDelivery(item);

        assertThatThrownBy(
                        () ->
                                service.checkout(
                                        societyId,
                                        UUID.randomUUID(),
                                        "RESIDENT",
                                        deliveryId,
                                        "UPI"))
                .isInstanceOf(AuthorizationException.class);
        assertThatThrownBy(
                        () ->
                                service.checkout(
                                        societyId, residentId, "RESIDENT", deliveryId, "CASH"))
                .hasMessageContaining("Invalid Razorpay");
        assertThatThrownBy(
                        () ->
                                service.checkout(
                                        societyId, residentId, "RESIDENT", deliveryId, "UPI"))
                .hasMessageContaining("not configured");

        SocietyPaymentSetting setting = new SocietyPaymentSetting();
        setting.setTestMode(true);
        when(paymentSettings.findBySocietyId(societyId)).thenReturn(Optional.of(setting));
        var result = service.checkout(societyId, residentId, "RESIDENT", deliveryId, "card");
        assertThat(result)
                .containsEntry("cod_payment_status", "PAID")
                .containsEntry("cod_payment_method", "RAZORPAY_CARD");
        assertThat(result.get("cod_payment_reference").toString()).startsWith("pay_delivery_test_");
    }

    @Test
    void unitDeliveryFollowsOutForDeliveryThenDelivered() {
        Delivery item = delivery("READY_FOR_PICKUP", "NOT_REQUIRED");
        stubDelivery(item);

        assertThat(
                        service.action(
                                societyId,
                                staffId,
                                "SOCIETY_ADMIN",
                                deliveryId,
                                "OUT_FOR_DELIVERY",
                                null,
                                null,
                                null))
                .containsEntry("status", "OUT_FOR_DELIVERY");
        assertThat(
                        service.action(
                                societyId,
                                staffId,
                                "SOCIETY_ADMIN",
                                deliveryId,
                                "DELIVER",
                                null,
                                "Resident",
                                null))
                .containsEntry("status", "DELIVERED");
        assertThat(item.getDeliveredAt()).isNotNull();
    }

    @Test
    void damagedAndRejectedDeliveriesCanBeReturnedWithAuditReason() {
        Delivery damaged = delivery("RECEIVED", "NOT_REQUIRED");
        stubDelivery(damaged);
        assertThat(
                        service.action(
                                societyId,
                                staffId,
                                "SOCIETY_ADMIN",
                                deliveryId,
                                "DAMAGED",
                                "Box torn",
                                null,
                                null))
                .containsEntry("status", "DAMAGED");
        assertThat(
                        service.action(
                                societyId,
                                staffId,
                                "SOCIETY_ADMIN",
                                deliveryId,
                                "RETURN",
                                "Returned to courier",
                                null,
                                null))
                .containsEntry("status", "RETURNED");
        assertThat(damaged.getReturnedAt()).isNotNull();
    }

    @Test
    void residentCannotCancelAnotherResidentsExpectedDelivery() {
        Delivery item = delivery("EXPECTED", "NOT_REQUIRED");
        stubDelivery(item);

        assertThatThrownBy(
                        () ->
                                service.action(
                                        societyId,
                                        UUID.randomUUID(),
                                        "RESIDENT",
                                        deliveryId,
                                        "CANCEL",
                                        null,
                                        null,
                                        null))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void managerCanReadCompleteEventHistoryButUnauthorizedResidentCannot() {
        Delivery item = delivery("RECEIVED", "NOT_REQUIRED");
        stubDelivery(item);
        DeliveryEvent event = new DeliveryEvent();
        event.setEventType("RECEIVE");
        event.setFromStatus("EXPECTED");
        event.setToStatus("RECEIVED");
        event.setActorId(staffId);
        event.setActorRole("STAFF");
        event.setOccurredAt(OffsetDateTime.now());
        when(events.findByDeliveryIdOrderByOccurredAt(deliveryId)).thenReturn(List.of(event));

        assertThat(service.history(societyId, staffId, "SOCIETY_ADMIN", deliveryId))
                .singleElement()
                .satisfies(row -> assertThat(row).containsEntry("event_type", "RECEIVE"));
        assertThatThrownBy(
                        () -> service.history(societyId, UUID.randomUUID(), "RESIDENT", deliveryId))
                .isInstanceOf(AuthorizationException.class);
    }

    private void stubDelivery(Delivery item) {
        when(deliveries.findByIdAndSocietyId(deliveryId, societyId)).thenReturn(Optional.of(item));
    }

    private Delivery delivery(String status, String paymentStatus) {
        Delivery item = new Delivery();
        item.setSocietyId(societyId);
        item.setFlatId(flatId);
        item.setResidentId(residentId);
        item.setDeliveryNumber("DLV-TEST-1");
        item.setProvider("Courier");
        item.setParcelType("PACKAGE");
        item.setPackageCount(1);
        item.setParcelSize("MEDIUM");
        item.setStatus(status);
        item.setCodAmount(BigDecimal.ZERO);
        item.setCodPaymentStatus(paymentStatus);
        item.setCreatedBy(residentId);
        item.setCreatedAt(OffsetDateTime.now());
        item.setUpdatedAt(OffsetDateTime.now());
        return item;
    }

    private UserAccount resident() {
        UserAccount user = new UserAccount();
        user.setSocietyId(societyId);
        user.setFullName("Resident");
        user.setEmail("resident@example.com");
        user.setMobile("9000000000");
        user.setRole("RESIDENT");
        user.setStatus("ACTIVE");
        return user;
    }

    private static final class DeliveryTestFacade {
        private final DeliveryService delegate;

        private DeliveryTestFacade(DeliveryService delegate) {
            this.delegate = delegate;
        }

        Map<String, Object> workspace(UUID societyId, UUID userId, String role) {
            var value = delegate.workspace(actor(societyId, userId, role));
            return Map.of(
                    "deliveries", value.deliveries(),
                    "options", value.options(),
                    "total", value.total(),
                    "awaiting_action", value.awaitingAction(),
                    "ready_for_pickup", value.readyForPickup(),
                    "completed", value.completed(),
                    "pending_cod", value.pendingCod());
        }

        List<Map<String, Object>> history(
                UUID societyId, UUID userId, String role, UUID deliveryId) {
            return delegate.history(actor(societyId, userId, role), deliveryId).stream()
                    .map(item -> Map.<String, Object>of("event_type", item.eventType().code()))
                    .toList();
        }

        Map<String, Object> create(
                UUID societyId,
                UUID userId,
                String role,
                UUID residentId,
                UUID flatId,
                String trackingNumber,
                String provider,
                String senderName,
                String deliveryPersonName,
                String deliveryPersonMobile,
                String parcelType,
                int packageCount,
                String parcelSize,
                boolean fragile,
                String storageLocation,
                OffsetDateTime expectedAt,
                String notes,
                BigDecimal codAmount,
                boolean receivedNow) {
            return view(
                    delegate.create(
                            actor(societyId, userId, role),
                            new DeliveryModels.CreateCommand(
                                    residentId,
                                    flatId,
                                    trackingNumber,
                                    provider,
                                    senderName,
                                    deliveryPersonName,
                                    deliveryPersonMobile,
                                    ParcelType.from(parcelType),
                                    packageCount,
                                    ParcelSize.from(parcelSize),
                                    fragile,
                                    storageLocation,
                                    expectedAt,
                                    notes,
                                    codAmount,
                                    receivedNow)));
        }

        Map<String, Object> action(
                UUID societyId,
                UUID userId,
                String role,
                UUID deliveryId,
                String action,
                String note,
                String collectorName,
                String pickupCode) {
            return view(
                    delegate.action(
                            actor(societyId, userId, role),
                            deliveryId,
                            new DeliveryModels.ActionCommand(
                                    DeliveryAction.from(action), note, collectorName, pickupCode)));
        }

        Map<String, Object> checkout(
                UUID societyId, UUID userId, String role, UUID deliveryId, String method) {
            return view(
                    delegate.checkout(
                            actor(societyId, userId, role),
                            deliveryId,
                            new DeliveryModels.PaymentCommand(PaymentMethod.from(method))));
        }

        Map<String, Object> recordCod(
                UUID societyId, UUID userId, String role, UUID deliveryId, String method) {
            return view(
                    delegate.recordCod(
                            actor(societyId, userId, role),
                            deliveryId,
                            new DeliveryModels.PaymentCommand(PaymentMethod.from(method))));
        }

        private DeliveryModels.Actor actor(UUID societyId, UUID userId, String role) {
            return new DeliveryModels.Actor(societyId, userId, UserRole.from(role));
        }

        private Map<String, Object> view(DeliveryModels.View value) {
            var result = new LinkedHashMap<String, Object>();
            result.put("tracking_number", value.trackingNumber());
            result.put("status", value.status().code());
            result.put("resident_decision", code(value.residentDecision()));
            result.put("authorized_collector", value.authorizedCollector());
            result.put("pickup_code_issued", value.pickupCodeIssued());
            result.put("cod_payment_status", value.codPaymentStatus().code());
            result.put("cod_payment_method", code(value.codPaymentMethod()));
            result.put("cod_payment_reference", value.codPaymentReference());
            result.put("collected_by_name", value.collectedByName());
            return result;
        }

        private String code(DomainCode value) {
            return value == null ? null : value.code();
        }
    }
}
