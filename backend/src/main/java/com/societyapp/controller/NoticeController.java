package com.societyapp.controller;

import com.societyapp.application.model.notice.NoticeModels;
import com.societyapp.application.port.in.NoticeUseCase;
import com.societyapp.controller.mapper.NoticeHttpMapper;
import com.societyapp.controller.model.NoticeResponse;
import com.societyapp.domain.type.*;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {
    private final NoticeUseCase service;
    private final NoticeHttpMapper mapper;

    public NoticeController(NoticeUseCase service, NoticeHttpMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    public record Save(
            @NotBlank @Size(max = 180) String title,
            @NotBlank String body,
            @NotNull @Pattern(regexp = "ALL|RESIDENTS|STAFF") String audience,
            @NotNull @Pattern(regexp = "NORMAL|IMPORTANT|URGENT") String priority,
            boolean pinned,
            boolean requiresAcknowledgement,
            @NotNull @Pattern(regexp = "DRAFT|PUBLISHED|ARCHIVED") String status,
            OffsetDateTime expiresAt) {}

    @GetMapping
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    List<NoticeResponse.Notice> list() {
        return service
                .list(CurrentUser.societyId(), CurrentUser.id(), UserRole.from(CurrentUser.role()))
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/options")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    NoticeResponse.Options options() {
        return mapper.toResponse(
                service.options(CurrentUser.id(), UserRole.from(CurrentUser.role())));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF')")
    NoticeResponse.Status create(@Valid @RequestBody Save request) {
        return mapper.toResponse(
                service.create(
                        CurrentUser.societyId(),
                        CurrentUser.id(),
                        UserRole.from(CurrentUser.role()),
                        command(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF')")
    NoticeResponse.Status update(@PathVariable UUID id, @Valid @RequestBody Save request) {
        return mapper.toResponse(
                service.update(
                        CurrentUser.societyId(),
                        CurrentUser.id(),
                        UserRole.from(CurrentUser.role()),
                        id,
                        command(request)));
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    NoticeResponse.Read read(@PathVariable UUID id) {
        return mapper.toResponse(
                service.markRead(
                        CurrentUser.societyId(),
                        CurrentUser.id(),
                        UserRole.from(CurrentUser.role()),
                        id));
    }

    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    NoticeResponse.Acknowledgement acknowledge(@PathVariable UUID id) {
        return mapper.toResponse(
                service.acknowledge(
                        CurrentUser.societyId(),
                        CurrentUser.id(),
                        UserRole.from(CurrentUser.role()),
                        id));
    }

    private NoticeModels.SaveCommand command(Save request) {
        return new NoticeModels.SaveCommand(
                request.title(),
                request.body(),
                NoticeAudience.from(request.audience()),
                NoticePriority.from(request.priority()),
                request.pinned(),
                request.requiresAcknowledgement(),
                NoticeStatus.from(request.status()),
                request.expiresAt());
    }
}
