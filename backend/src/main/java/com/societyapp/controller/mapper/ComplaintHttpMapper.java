package com.societyapp.controller.mapper;

import com.societyapp.application.model.complaint.ComplaintModels;
import com.societyapp.controller.model.ComplaintResponse;

public final class ComplaintHttpMapper {
    public ComplaintResponse.View toResponse(ComplaintModels.View view) {
        return new ComplaintResponse.View(
                view.id(),
                view.reference(),
                view.residentId(),
                view.spaceId(),
                view.categoryId(),
                view.title(),
                view.description(),
                view.priority().code(),
                view.status().code(),
                view.assignedTo(),
                view.resolution(),
                view.createdAt(),
                view.updatedAt(),
                view.dueAt(),
                view.closedAt(),
                view.overdue(),
                view.residentName(),
                view.createdByName(),
                view.assignedToName(),
                view.categoryName(),
                view.spaceNumber(),
                view.block());
    }

    public ComplaintResponse.Detail toResponse(ComplaintModels.Detail detail) {
        var view = detail.complaint();
        return new ComplaintResponse.Detail(
                view.id(),
                view.reference(),
                view.residentId(),
                view.spaceId(),
                view.categoryId(),
                view.title(),
                view.description(),
                view.priority().code(),
                view.status().code(),
                view.assignedTo(),
                view.resolution(),
                view.createdAt(),
                view.updatedAt(),
                view.dueAt(),
                view.closedAt(),
                view.overdue(),
                view.residentName(),
                view.createdByName(),
                view.assignedToName(),
                view.categoryName(),
                view.spaceNumber(),
                view.block(),
                detail.timeline().stream().map(this::timeline).toList());
    }

    public ComplaintResponse.Options toResponse(ComplaintModels.Options options) {
        return new ComplaintResponse.Options(
                options.currentUserId(),
                options.categories().stream()
                        .map(x -> new ComplaintResponse.Category(x.id(), x.name(), x.slaHours()))
                        .toList(),
                options.residents().stream()
                        .map(
                                x ->
                                        new ComplaintResponse.Resident(
                                                x.residentId(),
                                                x.fullName(),
                                                x.mobile(),
                                                x.spaceId(),
                                                x.spaceNumber(),
                                                x.block()))
                        .toList(),
                options.staff().stream()
                        .map(x -> new ComplaintResponse.Staff(x.id(), x.fullName(), x.mobile()))
                        .toList(),
                options.spaces().stream()
                        .map(
                                x ->
                                        new ComplaintResponse.Space(
                                                x.spaceId(), x.spaceNumber(), x.block()))
                        .toList());
    }

    public ComplaintResponse.Status toResponse(ComplaintModels.Status status) {
        return new ComplaintResponse.Status(status.id(), status.status().code());
    }

    public ComplaintResponse.Commented toResponse(ComplaintModels.Commented result) {
        return new ComplaintResponse.Commented(result.id(), result.commented());
    }

    private ComplaintResponse.TimelineEntry timeline(ComplaintModels.TimelineEntry entry) {
        return new ComplaintResponse.TimelineEntry(
                entry.id(),
                entry.eventType(),
                entry.fromStatus() == null ? null : entry.fromStatus().code(),
                entry.toStatus() == null ? null : entry.toStatus().code(),
                entry.note(),
                entry.createdAt(),
                entry.actorName());
    }
}
