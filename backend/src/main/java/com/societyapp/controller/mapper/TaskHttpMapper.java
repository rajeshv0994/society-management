package com.societyapp.controller.mapper;

import com.societyapp.application.model.task.TaskModels;
import com.societyapp.controller.model.TaskResponse;

public final class TaskHttpMapper {
    public TaskResponse.View toResponse(TaskModels.View value) {
        return new TaskResponse.View(
                value.id(),
                value.reference(),
                value.title(),
                value.description(),
                value.assignedTo(),
                value.assignedBy(),
                value.dueAt(),
                value.priority().code(),
                value.status().code(),
                value.completionNote(),
                value.createdAt(),
                value.updatedAt(),
                value.startedAt(),
                value.completedAt(),
                value.overdue(),
                value.assignedToName(),
                value.assignedByName());
    }

    public TaskResponse.Detail toResponse(TaskModels.Detail value) {
        var task = value.task();
        return new TaskResponse.Detail(
                task.id(),
                task.reference(),
                task.title(),
                task.description(),
                task.assignedTo(),
                task.assignedBy(),
                task.dueAt(),
                task.priority().code(),
                task.status().code(),
                task.completionNote(),
                task.createdAt(),
                task.updatedAt(),
                task.startedAt(),
                task.completedAt(),
                task.overdue(),
                task.assignedToName(),
                task.assignedByName(),
                value.timeline().stream().map(this::timeline).toList());
    }

    public TaskResponse.Options toResponse(TaskModels.Options value) {
        return new TaskResponse.Options(
                value.currentUserId(),
                value.staff().stream()
                        .map(item -> new TaskResponse.StaffOption(item.id(), item.fullName()))
                        .toList());
    }

    public TaskResponse.Status toResponse(TaskModels.Status value) {
        return new TaskResponse.Status(value.id(), value.status().code());
    }

    public TaskResponse.Commented toResponse(TaskModels.Commented value) {
        return new TaskResponse.Commented(value.id(), value.commented());
    }

    private TaskResponse.TimelineEntry timeline(TaskModels.TimelineEntry value) {
        return new TaskResponse.TimelineEntry(
                value.id(),
                value.eventType().code(),
                value.fromStatus() == null ? null : value.fromStatus().code(),
                value.toStatus() == null ? null : value.toStatus().code(),
                value.note(),
                value.createdAt(),
                value.actorName());
    }
}
