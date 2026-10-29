package com.societyapp.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.societyapp.application.exception.AuthorizationException;
import com.societyapp.application.model.activity.ActivityModels;
import com.societyapp.application.port.out.UploadedFile;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.service.ActivityService;
import com.societyapp.documents.LocalDocumentStorage;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ActivityServiceTest {
    private final SocietyActivityRepository activities = mock(SocietyActivityRepository.class);
    private final ActivityParticipationRepository participations =
            mock(ActivityParticipationRepository.class);
    private final ActivityGalleryPhotoRepository photos =
            mock(ActivityGalleryPhotoRepository.class);
    private final ActivityEventRepository events = mock(ActivityEventRepository.class);
    private final UserAccountJpaRepository users = mock(UserAccountJpaRepository.class);
    private final StaffPermissionRepository permissions = mock(StaffPermissionRepository.class);
    private final SocietyPaymentSettingRepository paymentSettings =
            mock(SocietyPaymentSettingRepository.class);
    private final NotificationOutboxRepository notifications =
            mock(NotificationOutboxRepository.class);
    private final LocalDocumentStorage storage = mock(LocalDocumentStorage.class);
    private final LegacyActivityFacade service =
            new LegacyActivityFacade(
                    new ActivityService(
                            activities,
                            participations,
                            photos,
                            events,
                            users,
                            permissions,
                            paymentSettings,
                            notifications,
                            storage));

    private final UUID societyId = UUID.randomUUID();
    private final UUID residentId = UUID.randomUUID();
    private final UUID staffId = UUID.randomUUID();
    private final UUID activityId = UUID.randomUUID();
    private final UUID participationId = UUID.randomUUID();
    private final UUID photoId = UUID.randomUUID();

    @BeforeEach
    void defaults() {
        when(activities.save(any(SocietyActivity.class))).thenAnswer(call -> call.getArgument(0));
        when(participations.save(any(ActivityParticipation.class)))
                .thenAnswer(call -> call.getArgument(0));
        when(photos.save(any(ActivityGalleryPhoto.class))).thenAnswer(call -> call.getArgument(0));
        when(events.save(any(ActivityEvent.class))).thenAnswer(call -> call.getArgument(0));
        when(notifications.save(any(NotificationOutbox.class)))
                .thenAnswer(call -> call.getArgument(0));
        when(activities.findByIdAndSocietyId(activityId, societyId))
                .thenReturn(Optional.of(activity(BigDecimal.ZERO, false)));
        when(participations.findByActivityId(activityId)).thenReturn(List.of());
        when(photos.findBySocietyIdOrderByCreatedAtDesc(societyId)).thenReturn(List.of());
    }

    @Test
    void residentWorkspaceShowsPublishedActivitiesAndOnlyOwnRegistrations() {
        var published = activity(BigDecimal.ZERO, false);
        var draft = activity(BigDecimal.ZERO, false);
        draft.setStatus("DRAFT");
        when(activities.findBySocietyIdOrderByStartsAtDesc(societyId))
                .thenReturn(List.of(published, draft));
        when(participations.findBySocietyIdAndParticipantIdOrderByCreatedAtDesc(
                        societyId, residentId))
                .thenReturn(List.of(participation("CONFIRMED", "FREE", BigDecimal.ZERO)));

        var result = service.workspace(societyId, residentId, "RESIDENT");

        assertThat((List<?>) result.get("activities")).hasSize(1);
        assertThat(result)
                .containsEntry("total_activities", 1)
                .containsEntry("my_registrations", 1L);
    }

    @Test
    void staffRequiresActivitiesPermission() {
        assertThatThrownBy(() -> service.workspace(societyId, staffId, "STAFF"))
                .isInstanceOf(AuthorizationException.class);
        allowStaff();
        when(activities.findBySocietyIdOrderByStartsAtDesc(societyId)).thenReturn(List.of());
        assertThat(service.workspace(societyId, staffId, "STAFF"))
                .containsEntry("total_activities", 0);
    }

    @Test
    void adminCanCreateFreePublishedActivityAndMembersAreNotified() {
        var result = create(BigDecimal.ZERO, true, false);

        assertThat(result)
                .containsEntry("status", "PUBLISHED")
                .containsEntry("is_paid", false)
                .containsEntry("fee_per_person", BigDecimal.ZERO);
        verify(events).save(any(ActivityEvent.class));
    }

    @Test
    void invalidScheduleIsRejected() {
        var now = OffsetDateTime.now();
        assertThatThrownBy(
                        () ->
                                service.create(
                                        societyId,
                                        staffId,
                                        "SOCIETY_ADMIN",
                                        "Bad activity",
                                        "SOCIAL",
                                        "Invalid schedule",
                                        "Hall",
                                        now.plusDays(2),
                                        now.plusDays(1),
                                        now,
                                        now.plusDays(1),
                                        20,
                                        BigDecimal.ZERO,
                                        false,
                                        0,
                                        "ALL_MEMBERS",
                                        null,
                                        null,
                                        null,
                                        false))
                .hasMessageContaining("end must be after start");
    }

    @Test
    void freeActivityConfirmsMemberImmediately() {
        stubActivity(activity(BigDecimal.ZERO, false));
        var result = join(2);

        assertThat(result)
                .containsEntry("participant_count", 2)
                .containsEntry("amount", BigDecimal.ZERO)
                .containsEntry("payment_status", "FREE")
                .containsEntry("status", "CONFIRMED");
    }

    @Test
    void paidActivityCalculatesPerPersonAmountAndRequiresPayment() {
        stubActivity(activity(new BigDecimal("150"), false));
        var result = join(3);

        assertThat(result)
                .containsEntry("amount", new BigDecimal("450"))
                .containsEntry("payment_status", "PENDING")
                .containsEntry("status", "PENDING_PAYMENT");
    }

    @Test
    void capacityAndDuplicateRegistrationAreEnforced() {
        var item = activity(BigDecimal.ZERO, false);
        item.setCapacity(3);
        stubActivity(item);
        var existing = participation("CONFIRMED", "FREE", BigDecimal.ZERO);
        existing.setParticipantCount(2);
        when(participations.findByActivityId(activityId)).thenReturn(List.of(existing));

        assertThatThrownBy(() -> join(2)).hasMessageContaining("Only 1 places remain");
        when(participations.findByActivityIdAndParticipantId(activityId, residentId))
                .thenReturn(Optional.of(existing));
        assertThatThrownBy(() -> join(1)).hasMessageContaining("already registered");
    }

    @Test
    void closedRegistrationWindowIsRejected() {
        var item = activity(BigDecimal.ZERO, false);
        item.setRegistrationClosesAt(OffsetDateTime.now().minusMinutes(1));
        stubActivity(item);

        assertThatThrownBy(() -> join(1)).hasMessageContaining("registration is not open");
    }

    @Test
    void razorpayTestCheckoutRequiresOwnerConfigurationAndValidMethod() {
        var item = activity(new BigDecimal("200"), false);
        stubActivity(item);
        var row = participation("PENDING_PAYMENT", "PENDING", new BigDecimal("200"));
        stubParticipation(row);
        when(activities.findById(activityId)).thenReturn(Optional.of(item));

        assertThatThrownBy(
                        () ->
                                service.checkout(
                                        societyId,
                                        UUID.randomUUID(),
                                        "RESIDENT",
                                        participationId,
                                        "UPI"))
                .isInstanceOf(AuthorizationException.class);
        assertThatThrownBy(
                        () ->
                                service.checkout(
                                        societyId, residentId, "RESIDENT", participationId, "CASH"))
                .hasMessageContaining("Invalid Razorpay");
        assertThatThrownBy(
                        () ->
                                service.checkout(
                                        societyId, residentId, "RESIDENT", participationId, "UPI"))
                .hasMessageContaining("not configured");

        var setting = new SocietyPaymentSetting();
        setting.setTestMode(true);
        when(paymentSettings.findBySocietyId(societyId)).thenReturn(Optional.of(setting));
        var result = service.checkout(societyId, residentId, "RESIDENT", participationId, "CARD");
        assertThat(result)
                .containsEntry("payment_status", "PAID")
                .containsEntry("payment_method", "RAZORPAY_CARD")
                .containsEntry("status", "CONFIRMED");
    }

    @Test
    void cashPaymentCanMoveRegistrationToApprovalQueue() {
        var item = activity(new BigDecimal("100"), true);
        stubActivity(item);
        var row = participation("PENDING_PAYMENT", "PENDING", new BigDecimal("100"));
        stubParticipation(row);
        when(activities.findById(activityId)).thenReturn(Optional.of(item));

        var result = service.recordCash(societyId, staffId, "SOCIETY_ADMIN", participationId);
        assertThat(result)
                .containsEntry("payment_status", "PAID")
                .containsEntry("payment_method", "CASH")
                .containsEntry("status", "PENDING_APPROVAL");
    }

    @Test
    void approvalRejectionAttendanceAndNoShowUseControlledStatuses() {
        var item = activity(BigDecimal.ZERO, true);
        item.setStartsAt(OffsetDateTime.now().minusHours(1));
        item.setEndsAt(OffsetDateTime.now().plusHours(1));
        stubActivity(item);
        var row = participation("PENDING_APPROVAL", "FREE", BigDecimal.ZERO);
        stubParticipation(row);

        assertThat(participationAction(row, "APPROVE", null)).containsEntry("status", "CONFIRMED");
        assertThat(participationAction(row, "CHECK_IN", null))
                .containsEntry("status", "CHECKED_IN");
        assertThat(participationAction(row, "ATTENDED", null)).containsEntry("status", "ATTENDED");

        row.setStatus("PENDING_APPROVAL");
        assertThatThrownBy(() -> participationAction(row, "REJECT", null))
                .hasMessageContaining("Rejection reason");
        assertThat(participationAction(row, "REJECT", "Age restriction"))
                .containsEntry("status", "REJECTED");

        row.setStatus("CONFIRMED");
        item.setEndsAt(OffsetDateTime.now().minusMinutes(1));
        assertThat(participationAction(row, "NO_SHOW", null)).containsEntry("status", "NO_SHOW");
    }

    @Test
    void paidCancellationCreatesRefundAndManagerCanRecordIt() {
        var item = activity(new BigDecimal("250"), false);
        item.setStartsAt(OffsetDateTime.now().plusDays(3));
        item.setCancellationHours(24);
        stubActivity(item);
        var row = participation("CONFIRMED", "PAID", new BigDecimal("250"));
        stubParticipation(row);

        assertThat(
                        service.participationAction(
                                societyId, residentId, "RESIDENT", participationId, "CANCEL", null))
                .containsEntry("payment_status", "REFUND_PENDING")
                .containsEntry("status", "CANCELLED");
        assertThat(participationAction(row, "REFUND", null))
                .containsEntry("payment_status", "REFUNDED");
    }

    @Test
    void activityCancellationCancelsPlacesAndQueuesPaidRefunds() {
        var item = activity(BigDecimal.ZERO, false);
        item.setStartsAt(OffsetDateTime.now().minusMinutes(5));
        stubActivity(item);
        var paid = participation("CONFIRMED", "PAID", new BigDecimal("100"));
        var free = participation("CONFIRMED", "FREE", BigDecimal.ZERO);
        when(participations.findByActivityId(activityId)).thenReturn(List.of(paid, free));

        var result =
                service.activityAction(
                        societyId,
                        staffId,
                        "SOCIETY_ADMIN",
                        activityId,
                        "CANCEL",
                        "Weather warning");
        assertThat(result).containsEntry("status", "CANCELLED");
        assertThat(paid.getPaymentStatus()).isEqualTo("REFUND_PENDING");
        assertThat(free.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void memberPhotoRequiresConfirmedParticipationAndIsModerated() {
        var item = activity(BigDecimal.ZERO, false);
        item.setStartsAt(OffsetDateTime.now().minusMinutes(5));
        stubActivity(item);
        var file = mock(UploadedFile.class);
        when(file.contentType()).thenReturn("image/jpeg");
        when(storage.storeImage(societyId, file)).thenReturn("gallery/test.jpg");

        assertThatThrownBy(
                        () ->
                                service.uploadPhoto(
                                        societyId,
                                        residentId,
                                        "RESIDENT",
                                        activityId,
                                        "Great event",
                                        file))
                .isInstanceOf(AuthorizationException.class);

        when(participations.findByActivityIdAndParticipantId(activityId, residentId))
                .thenReturn(Optional.of(participation("CONFIRMED", "FREE", BigDecimal.ZERO)));
        var result =
                service.uploadPhoto(
                        societyId, residentId, "RESIDENT", activityId, "Great event", file);
        assertThat(result)
                .containsEntry("moderation_status", "PENDING")
                .containsEntry("caption", "Great event");
    }

    @Test
    void adminCanApproveFeatureAndRemovePublishedPhoto() {
        var photo = photo("PENDING");
        when(photos.findByIdAndSocietyId(photoId, societyId)).thenReturn(Optional.of(photo));
        stubActivity(activity(BigDecimal.ZERO, false));

        assertThat(photoAction(photo, "APPROVE", null))
                .containsEntry("moderation_status", "PUBLISHED");
        assertThat(photoAction(photo, "FEATURE", null)).containsEntry("featured", true);
        assertThatThrownBy(() -> photoAction(photo, "REMOVE", null))
                .hasMessageContaining("Removal reason");
        assertThat(photoAction(photo, "REMOVE", "Duplicate photo"))
                .containsEntry("moderation_status", "REMOVED")
                .containsEntry("featured", false);
    }

    @Test
    void feeCannotChangeAfterActiveRegistrationsExist() {
        var item = activity(new BigDecimal("100"), false);
        stubActivity(item);
        when(participations.findByActivityId(activityId))
                .thenReturn(List.of(participation("CONFIRMED", "PAID", new BigDecimal("100"))));
        var now = OffsetDateTime.now();

        assertThatThrownBy(
                        () ->
                                service.update(
                                        societyId,
                                        staffId,
                                        "SOCIETY_ADMIN",
                                        activityId,
                                        "Updated event",
                                        "SOCIAL",
                                        "Description",
                                        "Hall",
                                        now.plusDays(5),
                                        now.plusDays(5).plusHours(2),
                                        now.minusMinutes(1),
                                        now.plusDays(4),
                                        100,
                                        new BigDecimal("200"),
                                        false,
                                        24,
                                        "ALL_MEMBERS",
                                        null,
                                        null,
                                        null))
                .hasMessageContaining("Fee cannot change");
    }

    @Test
    void timelineAndEntityReadsAreSocietyScoped() {
        stubActivity(activity(BigDecimal.ZERO, false));
        var event = new ActivityEvent();
        event.setEventType("PUBLISHED");
        event.setActorId(staffId);
        event.setActorRole("SOCIETY_ADMIN");
        event.setOccurredAt(OffsetDateTime.now());
        when(events.findByActivityIdOrderByOccurredAt(activityId)).thenReturn(List.of(event));

        assertThat(service.history(societyId, residentId, "RESIDENT", activityId))
                .singleElement()
                .satisfies(row -> assertThat(row).containsEntry("event_type", "PUBLISHED"));
        assertThatThrownBy(
                        () ->
                                service.history(
                                        UUID.randomUUID(), residentId, "RESIDENT", activityId))
                .hasMessageContaining("not found");
    }

    private Map<String, Object> create(BigDecimal fee, boolean publish, boolean approval) {
        var now = OffsetDateTime.now();
        return service.create(
                societyId,
                staffId,
                "SOCIETY_ADMIN",
                "Community celebration",
                "CULTURAL",
                "Society cultural evening",
                "Community Hall",
                now.plusDays(5),
                now.plusDays(5).plusHours(3),
                now.minusMinutes(1),
                now.plusDays(4),
                100,
                fee,
                approval,
                24,
                "ALL_MEMBERS",
                "Society Committee",
                "9000000000",
                "Bring registration confirmation",
                publish);
    }

    private Map<String, Object> join(int count) {
        return service.join(
                societyId,
                residentId,
                "RESIDENT",
                activityId,
                count,
                "Resident Family",
                "9000000000",
                null);
    }

    private Map<String, Object> participationAction(
            ActivityParticipation row, String action, String reason) {
        stubParticipation(row);
        return service.participationAction(
                societyId, staffId, "SOCIETY_ADMIN", participationId, action, reason);
    }

    private Map<String, Object> photoAction(
            ActivityGalleryPhoto photo, String action, String reason) {
        when(photos.findByIdAndSocietyId(photoId, societyId)).thenReturn(Optional.of(photo));
        return service.photoAction(societyId, staffId, "SOCIETY_ADMIN", photoId, action, reason);
    }

    private void stubActivity(SocietyActivity item) {
        when(activities.findByIdAndSocietyId(activityId, societyId)).thenReturn(Optional.of(item));
        when(activities.findById(activityId)).thenReturn(Optional.of(item));
    }

    private void stubParticipation(ActivityParticipation row) {
        when(participations.findByIdAndSocietyId(participationId, societyId))
                .thenReturn(Optional.of(row));
    }

    private void allowStaff() {
        when(permissions.existsByStaffUserIdAndPermissionCode(staffId, "ACTIVITIES_GALLERIES"))
                .thenReturn(true);
    }

    private SocietyActivity activity(BigDecimal fee, boolean approval) {
        var now = OffsetDateTime.now();
        var item = new SocietyActivity();
        item.setSocietyId(societyId);
        item.setActivityCode("ACT-TEST-1");
        item.setTitle("Test activity");
        item.setCategory("SOCIAL");
        item.setDescription("Test society activity");
        item.setVenue("Community Hall");
        item.setStartsAt(now.plusDays(3));
        item.setEndsAt(now.plusDays(3).plusHours(2));
        item.setRegistrationOpensAt(now.minusDays(1));
        item.setRegistrationClosesAt(now.plusDays(2));
        item.setCapacity(20);
        item.setFeePerPerson(fee);
        item.setApprovalRequired(approval);
        item.setCancellationHours(24);
        item.setAudience("ALL_MEMBERS");
        item.setStatus("PUBLISHED");
        item.setCreatedBy(staffId);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        return item;
    }

    private ActivityParticipation participation(
            String status, String paymentStatus, BigDecimal amount) {
        var row = new ActivityParticipation();
        row.setSocietyId(societyId);
        row.setActivityId(activityId);
        row.setParticipantId(residentId);
        row.setParticipantCount(1);
        row.setAmount(amount);
        row.setPaymentStatus(paymentStatus);
        row.setStatus(status);
        row.setCreatedAt(OffsetDateTime.now());
        row.setUpdatedAt(OffsetDateTime.now());
        return row;
    }

    private ActivityGalleryPhoto photo(String status) {
        var photo = new ActivityGalleryPhoto();
        photo.setSocietyId(societyId);
        photo.setActivityId(activityId);
        photo.setSubmittedBy(residentId);
        photo.setStorageKey("gallery/test.jpg");
        photo.setContentType("image/jpeg");
        photo.setModerationStatus(status);
        photo.setCreatedAt(OffsetDateTime.now());
        return photo;
    }

    /**
     * Keeps the historical behavior-focused assertions readable while exercising the typed port.
     */
    private static final class LegacyActivityFacade {
        private final ActivityService delegate;

        private LegacyActivityFacade(ActivityService delegate) {
            this.delegate = delegate;
        }

        Map<String, Object> workspace(UUID societyId, UUID actorId, String role) {
            var value = delegate.workspace(societyId, actor(actorId, role));
            var result = new LinkedHashMap<String, Object>();
            result.put("activities", value.activities());
            result.put("total_activities", value.totalActivities());
            result.put("my_registrations", value.myRegistrations());
            return result;
        }

        Map<String, Object> create(
                UUID societyId,
                UUID actorId,
                String role,
                String title,
                String category,
                String description,
                String venue,
                OffsetDateTime startsAt,
                OffsetDateTime endsAt,
                OffsetDateTime opensAt,
                OffsetDateTime closesAt,
                Integer capacity,
                BigDecimal fee,
                boolean approval,
                int cancellationHours,
                String audience,
                String organizer,
                String mobile,
                String instructions,
                boolean publish) {
            return activity(
                    delegate.create(
                            societyId,
                            actor(actorId, role),
                            command(
                                    title,
                                    category,
                                    description,
                                    venue,
                                    startsAt,
                                    endsAt,
                                    opensAt,
                                    closesAt,
                                    capacity,
                                    fee,
                                    approval,
                                    cancellationHours,
                                    audience,
                                    organizer,
                                    mobile,
                                    instructions,
                                    publish)));
        }

        Map<String, Object> update(
                UUID societyId,
                UUID actorId,
                String role,
                UUID activityId,
                String title,
                String category,
                String description,
                String venue,
                OffsetDateTime startsAt,
                OffsetDateTime endsAt,
                OffsetDateTime opensAt,
                OffsetDateTime closesAt,
                Integer capacity,
                BigDecimal fee,
                boolean approval,
                int cancellationHours,
                String audience,
                String organizer,
                String mobile,
                String instructions) {
            return activity(
                    delegate.update(
                            societyId,
                            actor(actorId, role),
                            activityId,
                            command(
                                    title,
                                    category,
                                    description,
                                    venue,
                                    startsAt,
                                    endsAt,
                                    opensAt,
                                    closesAt,
                                    capacity,
                                    fee,
                                    approval,
                                    cancellationHours,
                                    audience,
                                    organizer,
                                    mobile,
                                    instructions,
                                    false)));
        }

        Map<String, Object> activityAction(
                UUID societyId,
                UUID actorId,
                String role,
                UUID activityId,
                String action,
                String reason) {
            return activity(
                    delegate.activityAction(
                            societyId,
                            actor(actorId, role),
                            activityId,
                            new ActivityModels.ActionCommand(ActivityAction.from(action), reason)));
        }

        Map<String, Object> join(
                UUID societyId,
                UUID actorId,
                String role,
                UUID activityId,
                int count,
                String names,
                String mobile,
                String notes) {
            return participation(
                    delegate.join(
                            societyId,
                            actor(actorId, role),
                            activityId,
                            new ActivityModels.JoinCommand(count, names, mobile, notes)));
        }

        Map<String, Object> checkout(
                UUID societyId, UUID actorId, String role, UUID participationId, String method) {
            return participation(
                    delegate.checkout(
                            societyId,
                            actor(actorId, role),
                            participationId,
                            PaymentMethod.from(method)));
        }

        Map<String, Object> recordCash(
                UUID societyId, UUID actorId, String role, UUID participationId) {
            return participation(
                    delegate.recordCash(societyId, actor(actorId, role), participationId));
        }

        Map<String, Object> participationAction(
                UUID societyId,
                UUID actorId,
                String role,
                UUID participationId,
                String action,
                String reason) {
            return participation(
                    delegate.participationAction(
                            societyId,
                            actor(actorId, role),
                            participationId,
                            new ActivityModels.ParticipationActionCommand(
                                    ParticipationAction.from(action), reason)));
        }

        Map<String, Object> uploadPhoto(
                UUID societyId,
                UUID actorId,
                String role,
                UUID activityId,
                String caption,
                UploadedFile file) {
            return photo(
                    delegate.uploadPhoto(
                            societyId, actor(actorId, role), activityId, caption, file));
        }

        Map<String, Object> photoAction(
                UUID societyId,
                UUID actorId,
                String role,
                UUID photoId,
                String action,
                String reason) {
            return photo(
                    delegate.photoAction(
                            societyId,
                            actor(actorId, role),
                            photoId,
                            new ActivityModels.PhotoActionCommand(
                                    PhotoAction.from(action), reason)));
        }

        List<Map<String, Object>> history(
                UUID societyId, UUID actorId, String role, UUID activityId) {
            return delegate.history(societyId, actor(actorId, role), activityId).stream()
                    .map(
                            value -> {
                                var result = new LinkedHashMap<String, Object>();
                                result.put("event_type", value.eventType());
                                return (Map<String, Object>) result;
                            })
                    .toList();
        }

        private ActivityModels.Actor actor(UUID id, String role) {
            return new ActivityModels.Actor(id, UserRole.from(role));
        }

        private ActivityModels.ActivityCommand command(
                String title,
                String category,
                String description,
                String venue,
                OffsetDateTime startsAt,
                OffsetDateTime endsAt,
                OffsetDateTime opensAt,
                OffsetDateTime closesAt,
                Integer capacity,
                BigDecimal fee,
                boolean approval,
                int cancellationHours,
                String audience,
                String organizer,
                String mobile,
                String instructions,
                boolean publish) {
            return new ActivityModels.ActivityCommand(
                    title,
                    ActivityCategory.from(category),
                    description,
                    venue,
                    startsAt,
                    endsAt,
                    opensAt,
                    closesAt,
                    capacity,
                    fee,
                    approval,
                    cancellationHours,
                    ActivityAudience.from(audience),
                    organizer,
                    mobile,
                    instructions,
                    publish);
        }

        private Map<String, Object> activity(ActivityModels.ActivityView value) {
            var result = new LinkedHashMap<String, Object>();
            result.put("status", value.status().code());
            result.put("is_paid", value.paid());
            result.put("fee_per_person", value.feePerPerson());
            return result;
        }

        private Map<String, Object> participation(ActivityModels.ParticipationView value) {
            var result = new LinkedHashMap<String, Object>();
            result.put("participant_count", value.participantCount());
            result.put("amount", value.amount());
            result.put("payment_status", value.paymentStatus().code());
            result.put(
                    "payment_method",
                    value.paymentMethod() == null
                            ? null
                            : value.paymentReference() != null
                                            && value.paymentReference()
                                                    .startsWith("pay_activity_test_")
                                    ? value.paymentMethod().razorpayCode()
                                    : value.paymentMethod().code());
            result.put("status", value.status().code());
            return result;
        }

        private Map<String, Object> photo(ActivityModels.PhotoView value) {
            var result = new LinkedHashMap<String, Object>();
            result.put("moderation_status", value.moderationStatus().code());
            result.put("caption", value.caption());
            result.put("featured", value.featured());
            return result;
        }
    }
}
