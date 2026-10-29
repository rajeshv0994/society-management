package com.societyapp.application.port.in;

import com.societyapp.application.model.activity.ActivityModels;
import com.societyapp.application.port.out.UploadedFile;
import com.societyapp.domain.type.PaymentMethod;
import java.util.List;
import java.util.UUID;

public interface ActivityUseCase {
    ActivityModels.Workspace workspace(UUID societyId, ActivityModels.Actor actor);

    ActivityModels.ActivityView create(
            UUID societyId, ActivityModels.Actor actor, ActivityModels.ActivityCommand command);

    ActivityModels.ActivityView update(
            UUID societyId,
            ActivityModels.Actor actor,
            UUID activityId,
            ActivityModels.ActivityCommand command);

    ActivityModels.ActivityView activityAction(
            UUID societyId,
            ActivityModels.Actor actor,
            UUID activityId,
            ActivityModels.ActionCommand command);

    ActivityModels.ParticipationView join(
            UUID societyId,
            ActivityModels.Actor actor,
            UUID activityId,
            ActivityModels.JoinCommand command);

    ActivityModels.ParticipationView checkout(
            UUID societyId, ActivityModels.Actor actor, UUID participationId, PaymentMethod method);

    ActivityModels.ParticipationView recordCash(
            UUID societyId, ActivityModels.Actor actor, UUID participationId);

    ActivityModels.ParticipationView participationAction(
            UUID societyId,
            ActivityModels.Actor actor,
            UUID participationId,
            ActivityModels.ParticipationActionCommand command);

    ActivityModels.PhotoView uploadPhoto(
            UUID societyId,
            ActivityModels.Actor actor,
            UUID activityId,
            String caption,
            UploadedFile file);

    ActivityModels.PhotoContent photo(UUID societyId, ActivityModels.Actor actor, UUID photoId);

    ActivityModels.PhotoView photoAction(
            UUID societyId,
            ActivityModels.Actor actor,
            UUID photoId,
            ActivityModels.PhotoActionCommand command);

    List<ActivityModels.EventView> history(
            UUID societyId, ActivityModels.Actor actor, UUID activityId);
}
