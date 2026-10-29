package com.societyapp.application.mapper;

import com.societyapp.application.model.task.TaskModels;
import com.societyapp.application.port.out.persistence.UserAccountJpaRepository;
import com.societyapp.domain.model.StaffTask;
import com.societyapp.domain.model.StaffTaskUpdate;
import com.societyapp.domain.model.UserAccount;
import com.societyapp.domain.type.TaskEventType;
import com.societyapp.domain.type.TaskStatus;
import com.societyapp.domain.type.WorkPriority;
import java.time.OffsetDateTime;

public final class TaskViewMapper {
    private final UserAccountJpaRepository users;

    public TaskViewMapper(UserAccountJpaRepository users) {
        this.users = users;
    }

    public TaskModels.View task(StaffTask value) {
        var status = TaskStatus.from(value.getStatus());
        return new TaskModels.View(
                value.getId(),
                "TSK-" + value.getId().toString().substring(0, 8).toUpperCase(),
                value.getTitle(),
                value.getDescription(),
                value.getAssignedTo(),
                value.getAssignedBy(),
                value.getDueAt(),
                WorkPriority.from(value.getPriority()),
                status,
                value.getCompletionNote(),
                value.getCreatedAt(),
                value.getUpdatedAt(),
                value.getStartedAt(),
                value.getCompletedAt(),
                value.getDueAt() != null
                        && value.getDueAt().isBefore(OffsetDateTime.now())
                        && !status.isTerminal(),
                users.findById(value.getAssignedTo()).map(UserAccount::getFullName).orElse(null),
                users.findById(value.getAssignedBy()).map(UserAccount::getFullName).orElse(null));
    }

    public TaskModels.TimelineEntry update(StaffTaskUpdate value) {
        return new TaskModels.TimelineEntry(
                value.getId(),
                TaskEventType.from(value.getEventType()),
                value.getFromStatus() == null ? null : TaskStatus.from(value.getFromStatus()),
                value.getToStatus() == null ? null : TaskStatus.from(value.getToStatus()),
                value.getNote(),
                value.getCreatedAt(),
                users.findById(value.getActorUserId())
                        .map(UserAccount::getFullName)
                        .orElse("User"));
    }
}
