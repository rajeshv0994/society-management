package com.societyapp.application.service;

import com.societyapp.application.exception.AuthorizationException;
import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.mapper.TaskViewMapper;
import com.societyapp.application.model.task.TaskModels;
import com.societyapp.application.port.in.TaskUseCase;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.usecase.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.AccountStatus;
import com.societyapp.domain.type.StaffPermissionCode;
import com.societyapp.domain.type.TaskEventType;
import com.societyapp.domain.type.TaskStatus;
import com.societyapp.domain.type.UserRole;
import java.time.OffsetDateTime;
import java.util.*;

public class TaskService implements TaskUseCase {
    private final StaffTaskRepository tasks;
    private final StaffTaskUpdateRepository updates;
    private final UserAccountJpaRepository users;
    private final StaffPermissionRepository permissions;
    private final TaskViewMapper viewMapper;

    public TaskService(
            StaffTaskRepository tasks,
            StaffTaskUpdateRepository updates,
            UserAccountJpaRepository users,
            StaffPermissionRepository permissions) {
        this.tasks = tasks;
        this.updates = updates;
        this.users = users;
        this.permissions = permissions;
        this.viewMapper = new TaskViewMapper(users);
    }

    @Override
    public List<TaskModels.View> list(TaskModels.Actor actor) {
        if (actor.role() == UserRole.STAFF) requireStaffPermission(actor.userId());
        return tasks.findBySocietyIdOrderByCreatedAtDesc(actor.societyId()).stream()
                .filter(
                        task ->
                                actor.role() != UserRole.STAFF
                                        || task.getAssignedTo().equals(actor.userId()))
                .map(viewMapper::task)
                .toList();
    }

    @Override
    public TaskModels.Options options(TaskModels.Actor actor) {
        if (actor.role() == UserRole.STAFF) requireStaffPermission(actor.userId());
        var staff =
                users
                        .findBySocietyIdAndRoleOrderByFullName(
                                actor.societyId(), UserRole.STAFF.code())
                        .stream()
                        .filter(user -> AccountStatus.ACTIVE.matches(user.getStatus()))
                        .filter(
                                user ->
                                        permissions.existsByStaffUserIdAndPermissionCode(
                                                user.getId(), StaffPermissionCode.TASKS.code()))
                        .map(user -> new TaskModels.StaffOption(user.getId(), user.getFullName()))
                        .toList();
        return new TaskModels.Options(actor.userId(), staff);
    }

    @Override
    public TaskModels.Detail detail(TaskModels.Actor actor, UUID taskId) {
        var task = requireVisible(actor, taskId);
        return new TaskModels.Detail(
                viewMapper.task(task),
                updates.findByTaskIdOrderByCreatedAt(taskId).stream()
                        .map(viewMapper::update)
                        .toList());
    }

    @Override
    public TaskModels.Status create(TaskModels.Actor actor, TaskModels.CreateCommand command) {
        var staff = requireAssignableStaff(actor.societyId(), command.assignedTo());
        var now = OffsetDateTime.now();
        var task = new StaffTask();
        task.setSocietyId(actor.societyId());
        task.setTitle(command.title().trim());
        task.setDescription(command.description() == null ? null : command.description().trim());
        task.setAssignedTo(staff.getId());
        task.setAssignedBy(actor.userId());
        task.setDueAt(command.dueAt());
        task.setPriority(command.priority().code());
        task.setStatus(TaskStatus.OPEN.code());
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        task = tasks.save(task);
        addUpdate(
                task.getId(),
                actor.userId(),
                TaskEventType.CREATED,
                null,
                TaskStatus.OPEN.code(),
                "Task assigned to " + staff.getFullName());
        return new TaskModels.Status(task.getId(), TaskStatus.OPEN);
    }

    @Override
    public TaskModels.Status edit(
            TaskModels.Actor actor, UUID taskId, TaskModels.EditCommand command) {
        var task =
                tasks.findByIdAndSocietyId(taskId, actor.societyId())
                        .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        var staff = requireAssignableStaff(actor.societyId(), command.assignedTo());
        boolean reassigned = !task.getAssignedTo().equals(command.assignedTo());
        task.setTitle(command.title().trim());
        task.setDescription(command.description() == null ? null : command.description().trim());
        task.setAssignedTo(command.assignedTo());
        task.setDueAt(command.dueAt());
        task.setPriority(command.priority().code());
        task.setUpdatedAt(OffsetDateTime.now());
        tasks.save(task);
        addUpdate(
                taskId,
                actor.userId(),
                reassigned ? TaskEventType.REASSIGNED : TaskEventType.EDITED,
                task.getStatus(),
                task.getStatus(),
                reassigned ? "Task reassigned to " + staff.getFullName() : "Task details updated");
        return new TaskModels.Status(taskId, TaskStatus.from(task.getStatus()));
    }

    @Override
    public TaskModels.Status progress(
            TaskModels.Actor actor, UUID taskId, TaskModels.ProgressCommand command) {
        var task = requireVisible(actor, taskId);
        var previous = TaskStatus.from(task.getStatus());
        var next = command.status();
        String note = command.note();
        validateTransition(previous, next, actor.role());
        if ((next == TaskStatus.BLOCKED || next == TaskStatus.COMPLETED)
                && (note == null || note.isBlank())) {
            throw new IllegalArgumentException(
                    "A note is required when blocking or completing a task");
        }
        if (previous == next && (note == null || note.isBlank())) {
            throw new IllegalArgumentException("Add a progress note or change status");
        }
        var now = OffsetDateTime.now();
        task.setStatus(next.code());
        task.setUpdatedAt(now);
        if (next == TaskStatus.IN_PROGRESS && task.getStartedAt() == null) {
            task.setStartedAt(now);
        }
        if (next == TaskStatus.COMPLETED) {
            task.setCompletionNote(note.trim());
            task.setCompletedAt(now);
        } else {
            task.setCompletedAt(null);
            if (previous == TaskStatus.COMPLETED) {
                task.setCompletionNote(null);
            }
        }
        tasks.save(task);
        addUpdate(
                taskId,
                actor.userId(),
                previous == next ? TaskEventType.PROGRESS_NOTE : TaskEventType.STATUS_CHANGED,
                previous.code(),
                next.code(),
                note);
        return new TaskModels.Status(taskId, next);
    }

    @Override
    public TaskModels.Commented comment(TaskModels.Actor actor, UUID taskId, String note) {
        var task = requireVisible(actor, taskId);
        task.setUpdatedAt(OffsetDateTime.now());
        tasks.save(task);
        addUpdate(taskId, actor.userId(), TaskEventType.COMMENT, null, null, note);
        return new TaskModels.Commented(taskId, true);
    }

    @Override
    public TaskModels.Status complete(TaskModels.Actor actor, UUID taskId, String note) {
        return progress(actor, taskId, new TaskModels.ProgressCommand(TaskStatus.COMPLETED, note));
    }

    private StaffTask requireVisible(TaskModels.Actor actor, UUID taskId) {
        if (actor.role() == UserRole.STAFF) requireStaffPermission(actor.userId());
        return tasks.findByIdAndSocietyId(taskId, actor.societyId())
                .filter(
                        task ->
                                actor.role() != UserRole.STAFF
                                        || task.getAssignedTo().equals(actor.userId()))
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private UserAccount requireAssignableStaff(UUID societyId, UUID staffId) {
        return users.findById(staffId)
                .filter(user -> societyId.equals(user.getSocietyId()))
                .filter(user -> UserRole.STAFF.matches(user.getRole()))
                .filter(user -> AccountStatus.ACTIVE.matches(user.getStatus()))
                .filter(
                        user ->
                                permissions.existsByStaffUserIdAndPermissionCode(
                                        user.getId(), StaffPermissionCode.TASKS.code()))
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Active staff with Tasks permission not found"));
    }

    private void requireStaffPermission(UUID staffId) {
        if (!permissions.existsByStaffUserIdAndPermissionCode(
                staffId, StaffPermissionCode.TASKS.code())) {
            throw new AuthorizationException("Tasks permission is required");
        }
    }

    private void validateTransition(TaskStatus from, TaskStatus to, UserRole role) {
        if (from == to) return;
        if (!from.allowedTransitions().contains(to)) {
            throw new IllegalStateException("Invalid task status transition");
        }
        if (role == UserRole.STAFF
                && ((from == TaskStatus.COMPLETED && to == TaskStatus.IN_PROGRESS)
                        || (from == TaskStatus.CANCELLED && to == TaskStatus.OPEN)
                        || to == TaskStatus.CANCELLED)) {
            throw new AuthorizationException("Only Society Admin can cancel or reopen tasks");
        }
    }

    private void addUpdate(
            UUID taskId, UUID actorId, TaskEventType event, String from, String to, String note) {
        var update = new StaffTaskUpdate();
        update.setTaskId(taskId);
        update.setActorUserId(actorId);
        update.setEventType(event.code());
        update.setFromStatus(from);
        update.setToStatus(to);
        update.setNote(note);
        update.setCreatedAt(OffsetDateTime.now());
        updates.save(update);
    }
}
