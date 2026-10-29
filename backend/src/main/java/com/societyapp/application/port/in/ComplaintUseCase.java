package com.societyapp.application.port.in;

import com.societyapp.application.model.complaint.ComplaintModels;
import java.util.List;
import java.util.UUID;

public interface ComplaintUseCase {
    List<ComplaintModels.View> list(ComplaintModels.Actor actor);

    ComplaintModels.Options options(ComplaintModels.Actor actor);

    ComplaintModels.Detail detail(ComplaintModels.Actor actor, UUID complaintId);

    ComplaintModels.Status create(
            ComplaintModels.Actor actor, ComplaintModels.CreateCommand command);

    ComplaintModels.Status update(
            ComplaintModels.Actor actor, UUID complaintId, ComplaintModels.UpdateCommand command);

    ComplaintModels.Commented comment(ComplaintModels.Actor actor, UUID complaintId, String note);
}
