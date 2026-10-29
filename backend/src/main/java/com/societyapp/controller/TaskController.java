package com.societyapp.controller;

import com.societyapp.application.model.task.TaskModels;
import com.societyapp.application.port.in.TaskUseCase;
import com.societyapp.controller.mapper.TaskHttpMapper;
import com.societyapp.controller.model.TaskResponse;
import com.societyapp.domain.type.TaskStatus;
import com.societyapp.domain.type.UserRole;
import com.societyapp.domain.type.WorkPriority;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskUseCase service;
    private final TaskHttpMapper mapper = new TaskHttpMapper();

    public TaskController(TaskUseCase service) {
        this.service = service;
    }

    public record Create(
            @NotBlank @Size(max = 180) String title,
            String description,
            @NotNull UUID assignedTo,
            OffsetDateTime dueAt,
            @NotNull @Pattern(regexp = "LOW|NORMAL|HIGH|URGENT") String priority) {}

    public record Edit(
            @NotBlank @Size(max = 180) String title,
            String description,
            @NotNull UUID assignedTo,
            OffsetDateTime dueAt,
            @NotNull @Pattern(regexp = "LOW|NORMAL|HIGH|URGENT") String priority) {}

    public record Progress(
            @NotNull @Pattern(regexp = "OPEN|IN_PROGRESS|BLOCKED|COMPLETED|CANCELLED")
                    String status,
            String note) {}

    public record Complete(@NotBlank String completionNote) {}

    public record Comment(@NotBlank String note) {}

    @GetMapping
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF')")
    List<TaskResponse.View> list() {
        return service.list(actor()).stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/options")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF')")
    TaskResponse.Options options() {
        return mapper.toResponse(service.options(actor()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF')")
    TaskResponse.Detail detail(@PathVariable UUID id) {
        return mapper.toResponse(service.detail(actor(), id));
    }

    @PostMapping
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    TaskResponse.Status create(@Valid @RequestBody Create request) {
        return mapper.toResponse(
                service.create(
                        actor(),
                        new TaskModels.CreateCommand(
                                request.title(),
                                request.description(),
                                request.assignedTo(),
                                request.dueAt(),
                                WorkPriority.from(request.priority()))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    TaskResponse.Status edit(@PathVariable UUID id, @Valid @RequestBody Edit request) {
        return mapper.toResponse(
                service.edit(
                        actor(),
                        id,
                        new TaskModels.EditCommand(
                                request.title(),
                                request.description(),
                                request.assignedTo(),
                                request.dueAt(),
                                WorkPriority.from(request.priority()))));
    }

    @PatchMapping("/{id}/progress")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF')")
    TaskResponse.Status progress(@PathVariable UUID id, @Valid @RequestBody Progress request) {
        return mapper.toResponse(
                service.progress(
                        actor(),
                        id,
                        new TaskModels.ProgressCommand(
                                TaskStatus.from(request.status()), request.note())));
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF')")
    TaskResponse.Commented comment(@PathVariable UUID id, @Valid @RequestBody Comment request) {
        return mapper.toResponse(service.comment(actor(), id, request.note()));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF')")
    TaskResponse.Status complete(@PathVariable UUID id, @Valid @RequestBody Complete request) {
        return mapper.toResponse(service.complete(actor(), id, request.completionNote()));
    }

    private TaskModels.Actor actor() {
        return new TaskModels.Actor(
                CurrentUser.societyId(), CurrentUser.id(), UserRole.from(CurrentUser.role()));
    }
}
