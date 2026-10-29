package com.societyapp.application.port.in;

import com.societyapp.application.model.visitor.VisitorModels;
import com.societyapp.application.port.out.UploadedFile;
import com.societyapp.domain.type.VisitorStatus;
import java.util.List;
import java.util.UUID;

public interface VisitorUseCase {
    List<VisitorModels.VisitorView> list(UUID societyId, VisitorModels.Actor actor);

    VisitorModels.PermissionView permissions(VisitorModels.Actor actor);

    VisitorModels.MemberOptions memberOptions(UUID societyId, UUID residentId);

    void requireGate(VisitorModels.Actor actor);

    List<VisitorModels.HostOption> hosts(UUID societyId);

    List<VisitorModels.TargetOption> targets(UUID societyId);

    VisitorModels.Mutation create(
            UUID societyId, VisitorModels.Actor actor, VisitorModels.CreateCommand command);

    VisitorModels.PhotoView uploadPhoto(
            UUID societyId, VisitorModels.Actor actor, UUID visitorId, UploadedFile file);

    VisitorModels.PhotoContent photo(UUID societyId, VisitorModels.Actor actor, UUID photoId);

    VisitorModels.Mutation residentDecision(
            UUID societyId, UUID residentId, UUID visitorId, VisitorModels.DecisionCommand command);

    VisitorModels.Mutation residentCancel(
            UUID societyId, UUID residentId, UUID visitorId, String note);

    VisitorModels.Mutation staffApproval(
            UUID societyId, VisitorModels.Actor actor, UUID visitorId, String note);

    VisitorModels.Mutation targetStaffDecision(
            UUID societyId, UUID staffId, UUID visitorId, VisitorModels.DecisionCommand command);

    VisitorModels.Mutation updateStatus(
            UUID societyId, VisitorModels.Actor actor, UUID visitorId, VisitorStatus status);
}
