package com.societyapp.controller.mapper;

import com.societyapp.application.model.visitor.VisitorModels;
import com.societyapp.controller.model.VisitorResponse;

public final class VisitorHttpMapper {
    public VisitorResponse.Visitor toResponse(VisitorModels.VisitorView value) {
        var firstPhoto = value.photoUrls().isEmpty() ? null : value.photoUrls().getFirst();
        return new VisitorResponse.Visitor(
                value.id(),
                value.societyId(),
                value.spaceId(),
                value.spaceId(),
                value.spaceId(),
                value.visitorName(),
                value.mobile(),
                value.purpose(),
                value.vehicleNumber(),
                value.expectedAt(),
                value.approvedBy(),
                value.hostResidentId(),
                value.hostResidentName(),
                value.targetType().code(),
                value.targetUserId(),
                value.targetName(),
                value.targetMobile(),
                value.accompanyingMembers(),
                value.totalPeople(),
                value.photoUrls(),
                firstPhoto,
                value.photoUrls().size(),
                value.residentDecisionAt(),
                value.staffApprovedBy(),
                value.approvalMethod() == null ? null : value.approvalMethod().code(),
                value.approvalNote(),
                value.checkedInBy(),
                value.checkedInAt(),
                value.checkedOutAt(),
                value.status().code(),
                value.createdAt(),
                value.spaceNumber(),
                value.spaceNumber(),
                value.spaceNumber(),
                value.block());
    }

    public VisitorResponse.Permission toResponse(VisitorModels.PermissionView value) {
        return new VisitorResponse.Permission(value.userId(), value.canManageGate());
    }

    public VisitorResponse.MemberOptions toResponse(VisitorModels.MemberOptions value) {
        return new VisitorResponse.MemberOptions(
                value.residentId(),
                value.spaces().stream()
                        .map(
                                space ->
                                        new VisitorResponse.Space(
                                                space.spaceId(),
                                                space.spaceNumber(),
                                                space.block()))
                        .toList());
    }

    public VisitorResponse.Host toResponse(VisitorModels.HostOption value) {
        return new VisitorResponse.Host(
                value.residentId(),
                value.fullName(),
                value.mobile(),
                value.spaceId(),
                value.spaceNumber(),
                value.block());
    }

    public VisitorResponse.Target toResponse(VisitorModels.TargetOption value) {
        return new VisitorResponse.Target(
                value.userId(), value.fullName(), value.role().code(), value.mobile());
    }

    public VisitorResponse.Mutation toResponse(VisitorModels.Mutation value) {
        return new VisitorResponse.Mutation(value.id(), value.status().code());
    }

    public VisitorResponse.Photo toResponse(VisitorModels.PhotoView value) {
        return new VisitorResponse.Photo(value.id(), value.visitorId(), value.photoUrl());
    }
}
