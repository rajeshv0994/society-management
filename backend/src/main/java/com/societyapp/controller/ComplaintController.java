package com.societyapp.controller;

import com.societyapp.application.model.complaint.ComplaintModels;
import com.societyapp.application.port.in.ComplaintUseCase;
import com.societyapp.controller.mapper.ComplaintHttpMapper;
import com.societyapp.controller.model.ComplaintResponse;
import com.societyapp.domain.type.ComplaintStatus;
import com.societyapp.domain.type.UserRole;
import com.societyapp.domain.type.WorkPriority;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {
    private final ComplaintUseCase service;
    private final ComplaintHttpMapper mapper = new ComplaintHttpMapper();

    public ComplaintController(ComplaintUseCase service) {
        this.service = service;
    }

    public record Create(
            UUID residentId,
            UUID flatId,
            UUID categoryId,
            UUID assignedTo,
            @NotBlank String title,
            @NotBlank String description,
            @Pattern(regexp = "LOW|NORMAL|HIGH|URGENT") String priority) {}

    public record Update(
            @Pattern(regexp = "OPEN|ASSIGNED|IN_PROGRESS|RESOLVED|CLOSED") String status,
            UUID assignedTo,
            String note,
            String resolution) {}

    public record Comment(@NotBlank String note) {}

    @GetMapping
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    List<ComplaintResponse.View> list() {
        return service.list(actor()).stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/options")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    ComplaintResponse.Options options() {
        return mapper.toResponse(service.options(actor()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    ComplaintResponse.Detail detail(@PathVariable UUID id) {
        return mapper.toResponse(service.detail(actor(), id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    ComplaintResponse.Status create(@Valid @RequestBody Create r) {
        return mapper.toResponse(
                service.create(
                        actor(),
                        new ComplaintModels.CreateCommand(
                                r.residentId(),
                                r.flatId(),
                                r.categoryId(),
                                r.assignedTo(),
                                r.title(),
                                r.description(),
                                WorkPriority.from(
                                        r.priority() == null
                                                ? WorkPriority.NORMAL.code()
                                                : r.priority()))));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF')")
    ComplaintResponse.Status update(@PathVariable UUID id, @Valid @RequestBody Update r) {
        return mapper.toResponse(
                service.update(
                        actor(),
                        id,
                        new ComplaintModels.UpdateCommand(
                                r.status() == null ? null : ComplaintStatus.from(r.status()),
                                r.assignedTo(),
                                r.note(),
                                r.resolution())));
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    ComplaintResponse.Commented comment(@PathVariable UUID id, @Valid @RequestBody Comment r) {
        return mapper.toResponse(service.comment(actor(), id, r.note()));
    }

    private ComplaintModels.Actor actor() {
        return new ComplaintModels.Actor(
                CurrentUser.societyId(), CurrentUser.id(), UserRole.from(CurrentUser.role()));
    }
}
