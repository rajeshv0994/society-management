package com.societyapp.application.command;

import com.societyapp.application.exception.*;
import com.societyapp.application.model.activity.ActivityModels;
import com.societyapp.application.port.out.UploadedFile;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.query.ActivityQueryCoordinator;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.math.*;
import java.time.*;
import java.util.*;

public final class ActivityGalleryCoordinator extends ActivityWorkflowSupport {
    public ActivityGalleryCoordinator(
            SocietyActivityRepository activities,
            ActivityParticipationRepository participations,
            ActivityGalleryPhotoRepository photos,
            ActivityEventRepository events,
            UserAccountJpaRepository users,
            StaffPermissionRepository permissions,
            SocietyPaymentSettingRepository paymentSettings,
            NotificationOutboxRepository notifications,
            com.societyapp.application.port.out.DocumentStorage storage,
            ActivityQueryCoordinator queries) {
        super(
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

    public ActivityModels.PhotoView uploadPhoto(
            UUID societyId,
            UUID actorId,
            String role,
            UUID activityId,
            String caption,
            UploadedFile file) {
        var activity = requireActivity(societyId, activityId);
        if (UserRole.RESIDENT.matches(role)) {
            if (OffsetDateTime.now().isBefore(activity.getStartsAt())) {
                throw new IllegalStateException(
                        "Gallery photos can be submitted after the activity starts");
            }
            participations
                    .findByActivityIdAndParticipantId(activityId, actorId)
                    .filter(
                            row ->
                                    Set.of(
                                                    ParticipationStatus.CONFIRMED.code(),
                                                    ParticipationStatus.CHECKED_IN.code(),
                                                    ParticipationStatus.ATTENDED.code())
                                            .contains(row.getStatus()))
                    .orElseThrow(
                            () ->
                                    new AuthorizationException(
                                            "Only confirmed participants can submit photos"));
        } else {
            requireManager(actorId, role);
        }
        var photo = new ActivityGalleryPhoto();
        photo.setSocietyId(societyId);
        photo.setActivityId(activityId);
        photo.setSubmittedBy(actorId);
        photo.setStorageKey(storage.storeImage(societyId, file));
        photo.setContentType(file.contentType());
        photo.setCaption(blankToNull(caption));
        photo.setModerationStatus(
                UserRole.SOCIETY_ADMIN.matches(role)
                        ? ModerationStatus.PUBLISHED.code()
                        : ModerationStatus.PENDING.code());
        photo.setFeatured(false);
        photo.setCreatedAt(OffsetDateTime.now());
        photo = photos.save(photo);
        storage.attach(photo.getStorageKey(), "ACTIVITY_PHOTO", photo.getId(), activity.getTitle());
        addEvent(activity, null, actorId, role, "PHOTO_SUBMITTED", caption);
        return queries.photoView(photo);
    }

    public ActivityModels.PhotoContent photo(
            UUID societyId, UUID actorId, String role, UUID photoId) {
        if (UserRole.STAFF.matches(role)) requireManager(actorId, role);
        var photo =
                photos.findByIdAndSocietyId(photoId, societyId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Activity photo not found"));
        if (UserRole.RESIDENT.matches(role)
                && !ModerationStatus.PUBLISHED.matches(photo.getModerationStatus())
                && !actorId.equals(photo.getSubmittedBy())) {
            throw new ResourceNotFoundException("Activity photo not found");
        }
        return new ActivityModels.PhotoContent(
                storage.load(photo.getStorageKey()), photo.getContentType());
    }

    public ActivityModels.PhotoView photoAction(
            UUID societyId, UUID actorId, String role, UUID photoId, String action, String reason) {
        requireAdmin(role);
        var photo =
                photos.findByIdAndSocietyId(photoId, societyId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Activity photo not found"));
        var requestedAction = PhotoAction.from(action);
        switch (requestedAction) {
            case APPROVE -> {
                if (!ModerationStatus.PENDING.matches(photo.getModerationStatus())) {
                    throw new IllegalStateException("Photo is not pending moderation");
                }
                photo.setModerationStatus(ModerationStatus.PUBLISHED.code());
                photo.setRejectionReason(null);
            }
            case REJECT -> {
                if (!ModerationStatus.PENDING.matches(photo.getModerationStatus())) {
                    throw new IllegalStateException("Photo is not pending moderation");
                }
                requireReason(reason, "Rejection reason is required");
                photo.setModerationStatus(ModerationStatus.REJECTED.code());
                photo.setRejectionReason(reason.trim());
            }
            case REMOVE -> {
                requireReason(reason, "Removal reason is required");
                photo.setModerationStatus(ModerationStatus.REMOVED.code());
                photo.setFeatured(false);
                photo.setRejectionReason(reason.trim());
            }
            case FEATURE -> {
                if (!ModerationStatus.PUBLISHED.matches(photo.getModerationStatus())) {
                    throw new IllegalStateException("Only a published photo can be featured");
                }
                photo.setFeatured(true);
            }
            case UNFEATURE -> photo.setFeatured(false);
        }
        photo.setReviewedBy(actorId);
        photo.setReviewedAt(OffsetDateTime.now());
        photo = photos.save(photo);
        addEvent(
                requireActivity(societyId, photo.getActivityId()),
                null,
                actorId,
                role,
                "PHOTO_" + requestedAction.code(),
                reason);
        return queries.photoView(photo);
    }
}
