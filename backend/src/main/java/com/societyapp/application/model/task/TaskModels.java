package com.societyapp.application.model.task;

import com.societyapp.domain.type.TaskEventType;
import com.societyapp.domain.type.TaskStatus;
import com.societyapp.domain.type.UserRole;
import com.societyapp.domain.type.WorkPriority;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class TaskModels {
    private TaskModels() {}

    public record Actor(UUID societyId, UUID userId, UserRole role) {}

    public record CreateCommand(
            String title,
            String description,
            UUID assignedTo,
            OffsetDateTime dueAt,
            WorkPriority priority) {}

    public record EditCommand(
            String title,
            String description,
            UUID assignedTo,
            OffsetDateTime dueAt,
            WorkPriority priority) {}

    public record ProgressCommand(TaskStatus status, String note) {}

    public record StaffOption(UUID id, String fullName) {}

    public record Options(UUID currentUserId, List<StaffOption> staff) {}

    public record View(
            UUID id,
            String reference,
            String title,
            String description,
            UUID assignedTo,
            UUID assignedBy,
            OffsetDateTime dueAt,
            WorkPriority priority,
            TaskStatus status,
            String completionNote,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            OffsetDateTime startedAt,
            OffsetDateTime completedAt,
            boolean overdue,
            String assignedToName,
            String assignedByName) {}

    public record TimelineEntry(
            UUID id,
            TaskEventType eventType,
            TaskStatus fromStatus,
            TaskStatus toStatus,
            String note,
            OffsetDateTime createdAt,
            String actorName) {}

    public record Detail(View task, List<TimelineEntry> timeline) {}

    public record Status(UUID id, TaskStatus status) {}

    public record Commented(UUID id, boolean commented) {}
}
