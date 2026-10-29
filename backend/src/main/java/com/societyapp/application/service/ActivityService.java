package com.societyapp.application.service;

import com.societyapp.application.command.*;
import com.societyapp.application.model.activity.ActivityModels;
import com.societyapp.application.port.in.ActivityUseCase;
import com.societyapp.application.port.out.*;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.query.ActivityQueryCoordinator;
import com.societyapp.domain.type.PaymentMethod;
import java.util.List;
import java.util.UUID;

public class ActivityService implements ActivityUseCase {
    private final ActivityQueryCoordinator queries;
    private final ActivityLifecycleCoordinator lifecycle;
    private final ActivityParticipationCoordinator participation;
    private final ActivityGalleryCoordinator gallery;

    public ActivityService(
            SocietyActivityRepository activities,
            ActivityParticipationRepository participations,
            ActivityGalleryPhotoRepository photos,
            ActivityEventRepository events,
            UserAccountJpaRepository users,
            StaffPermissionRepository permissions,
            SocietyPaymentSettingRepository paymentSettings,
            NotificationOutboxRepository notifications,
            DocumentStorage storage) {
        queries =
                new ActivityQueryCoordinator(
                        activities, participations, photos, events, users, permissions);
        lifecycle =
                new ActivityLifecycleCoordinator(
                        activities,
                        participations,
                        photos,
                        events,
                        users,
                        permissions,
                        paymentSettings,
                        notifications,
                        storage,
                        queries);
        participation =
                new ActivityParticipationCoordinator(
                        activities,
                        participations,
                        photos,
                        events,
                        users,
                        permissions,
                        paymentSettings,
                        notifications,
                        storage,
                        queries);
        gallery =
                new ActivityGalleryCoordinator(
                        activities,
                        participations,
                        photos,
                        events,
                        users,
                        permissions,
                        paymentSettings,
                        notifications,
                        storage,
                        queries);
    }

    @Override
    public ActivityModels.Workspace workspace(UUID societyId, ActivityModels.Actor actor) {
        return queries.workspace(societyId, actor);
    }

    @Override
    public ActivityModels.ActivityView create(
            UUID societyId, ActivityModels.Actor actor, ActivityModels.ActivityCommand command) {
        return lifecycle.create(
                societyId,
                actor.userId(),
                actor.role().code(),
                command.title(),
                command.category().code(),
                command.description(),
                command.venue(),
                command.startsAt(),
                command.endsAt(),
                command.registrationOpensAt(),
                command.registrationClosesAt(),
                command.capacity(),
                command.feePerPerson(),
                command.approvalRequired(),
                command.cancellationHours(),
                command.audience().code(),
                command.organizerName(),
                command.organizerMobile(),
                command.instructions(),
                command.publishNow());
    }

    @Override
    public ActivityModels.ActivityView update(
            UUID societyId,
            ActivityModels.Actor actor,
            UUID activityId,
            ActivityModels.ActivityCommand command) {
        return lifecycle.update(
                societyId,
                actor.userId(),
                actor.role().code(),
                activityId,
                command.title(),
                command.category().code(),
                command.description(),
                command.venue(),
                command.startsAt(),
                command.endsAt(),
                command.registrationOpensAt(),
                command.registrationClosesAt(),
                command.capacity(),
                command.feePerPerson(),
                command.approvalRequired(),
                command.cancellationHours(),
                command.audience().code(),
                command.organizerName(),
                command.organizerMobile(),
                command.instructions());
    }

    @Override
    public ActivityModels.ActivityView activityAction(
            UUID societyId,
            ActivityModels.Actor actor,
            UUID activityId,
            ActivityModels.ActionCommand command) {
        return lifecycle.activityAction(
                societyId,
                actor.userId(),
                actor.role().code(),
                activityId,
                command.action().code(),
                command.reason());
    }

    @Override
    public ActivityModels.ParticipationView join(
            UUID societyId,
            ActivityModels.Actor actor,
            UUID activityId,
            ActivityModels.JoinCommand command) {
        return participation.join(
                societyId,
                actor.userId(),
                actor.role().code(),
                activityId,
                command.participantCount(),
                command.participantNames(),
                command.contactMobile(),
                command.notes());
    }

    @Override
    public ActivityModels.ParticipationView checkout(
            UUID societyId,
            ActivityModels.Actor actor,
            UUID participationId,
            PaymentMethod method) {
        return participation.checkout(
                societyId, actor.userId(), actor.role().code(), participationId, method.code());
    }

    @Override
    public ActivityModels.ParticipationView recordCash(
            UUID societyId, ActivityModels.Actor actor, UUID participationId) {
        return participation.recordCash(
                societyId, actor.userId(), actor.role().code(), participationId);
    }

    @Override
    public ActivityModels.ParticipationView participationAction(
            UUID societyId,
            ActivityModels.Actor actor,
            UUID participationId,
            ActivityModels.ParticipationActionCommand command) {
        return participation.participationAction(
                societyId,
                actor.userId(),
                actor.role().code(),
                participationId,
                command.action().code(),
                command.reason());
    }

    @Override
    public ActivityModels.PhotoView uploadPhoto(
            UUID societyId,
            ActivityModels.Actor actor,
            UUID activityId,
            String caption,
            UploadedFile file) {
        return gallery.uploadPhoto(
                societyId, actor.userId(), actor.role().code(), activityId, caption, file);
    }

    @Override
    public ActivityModels.PhotoContent photo(
            UUID societyId, ActivityModels.Actor actor, UUID photoId) {
        return gallery.photo(societyId, actor.userId(), actor.role().code(), photoId);
    }

    @Override
    public ActivityModels.PhotoView photoAction(
            UUID societyId,
            ActivityModels.Actor actor,
            UUID photoId,
            ActivityModels.PhotoActionCommand command) {
        return gallery.photoAction(
                societyId,
                actor.userId(),
                actor.role().code(),
                photoId,
                command.action().code(),
                command.reason());
    }

    @Override
    public List<ActivityModels.EventView> history(
            UUID societyId, ActivityModels.Actor actor, UUID activityId) {
        return queries.history(societyId, actor, activityId);
    }
}
