package com.societyapp.application.model.visitor;

import com.societyapp.application.model.BinaryContent;
import com.societyapp.domain.type.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class VisitorModels {
    private VisitorModels() {}

    public record Actor(UUID userId, UserRole role) {}

    public record CreateCommand(
            UUID spaceId,
            UUID hostResidentId,
            VisitorTargetType targetType,
            UUID targetUserId,
            String targetName,
            String visitorName,
            String mobile,
            String purpose,
            String vehicleNumber,
            OffsetDateTime expectedAt,
            int accompanyingMembers) {}

    public record DecisionCommand(ApprovalAction decision, String note) {}

    public record VisitorView(
            UUID id,
            UUID societyId,
            UUID spaceId,
            String visitorName,
            String mobile,
            String purpose,
            String vehicleNumber,
            OffsetDateTime expectedAt,
            UUID approvedBy,
            UUID hostResidentId,
            String hostResidentName,
            VisitorTargetType targetType,
            UUID targetUserId,
            String targetName,
            String targetMobile,
            int accompanyingMembers,
            int totalPeople,
            List<String> photoUrls,
            OffsetDateTime residentDecisionAt,
            UUID staffApprovedBy,
            VisitorApprovalMethod approvalMethod,
            String approvalNote,
            UUID checkedInBy,
            OffsetDateTime checkedInAt,
            OffsetDateTime checkedOutAt,
            VisitorStatus status,
            OffsetDateTime createdAt,
            String spaceNumber,
            String block) {}

    public record PermissionView(UUID userId, boolean canManageGate) {}

    public record SpaceOption(UUID spaceId, String spaceNumber, String block) {}

    public record MemberOptions(UUID residentId, List<SpaceOption> spaces) {}

    public record HostOption(
            UUID residentId,
            String fullName,
            String mobile,
            UUID spaceId,
            String spaceNumber,
            String block) {}

    public record TargetOption(UUID userId, String fullName, UserRole role, String mobile) {}

    public record Mutation(UUID id, VisitorStatus status) {}

    public record PhotoView(UUID id, UUID visitorId, String photoUrl) {}

    public record PhotoContent(BinaryContent content, String contentType) {}
}
