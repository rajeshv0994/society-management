package com.societyapp.controller;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.societyapp.application.model.visitor.VisitorModels;
import com.societyapp.application.port.in.VisitorUseCase;
import com.societyapp.controller.mapper.VisitorHttpMapper;
import com.societyapp.controller.model.VisitorResponse;
import com.societyapp.domain.type.*;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/visitors")
public class VisitorController {
    private final VisitorUseCase service;
    private final VisitorHttpMapper mapper = new VisitorHttpMapper();

    public VisitorController(VisitorUseCase service) {
        this.service = service;
    }

    public record Create(
            @JsonAlias({"spaceId", "unitId"}) UUID flatId,
            UUID hostResidentId,
            @Pattern(regexp = "RESIDENT|STAFF|OTHER") String targetType,
            UUID targetUserId,
            String targetName,
            @NotBlank String visitorName,
            String mobile,
            @NotBlank String purpose,
            String vehicleNumber,
            OffsetDateTime expectedAt,
            @Min(0) int accompanyingMembers) {}

    public record Approval(
            @NotNull @Pattern(regexp = "APPROVE|DENY") String decision,
            @Size(max = 250) String note) {}

    public record Cancellation(@Size(max = 250) String note) {}

    public record StaffApproval(@NotBlank @Size(max = 250) String note) {}

    public record Status(
            @NotNull @Pattern(regexp = "CHECKED_IN|CHECKED_OUT|CANCELLED|DENIED") String status) {}

    @GetMapping
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    List<VisitorResponse.Visitor> list() {
        return service.list(CurrentUser.societyId(), actor()).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF')")
    VisitorResponse.Permission permissions() {
        return mapper.toResponse(service.permissions(actor()));
    }

    @GetMapping("/hosts")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF')")
    List<VisitorResponse.Host> hosts() {
        service.requireGate(actor());
        return service.hosts(CurrentUser.societyId()).stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/targets")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF')")
    List<VisitorResponse.Target> targets() {
        service.requireGate(actor());
        return service.targets(CurrentUser.societyId()).stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/member-options")
    @PreAuthorize("hasRole('RESIDENT')")
    VisitorResponse.MemberOptions memberOptions() {
        return mapper.toResponse(service.memberOptions(CurrentUser.societyId(), CurrentUser.id()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    VisitorResponse.Mutation create(@Valid @RequestBody Create r) {
        return mapper.toResponse(
                service.create(
                        CurrentUser.societyId(),
                        actor(),
                        new VisitorModels.CreateCommand(
                                r.flatId(),
                                r.hostResidentId(),
                                r.targetType() == null
                                        ? null
                                        : VisitorTargetType.from(r.targetType()),
                                r.targetUserId(),
                                r.targetName(),
                                r.visitorName(),
                                r.mobile(),
                                r.purpose(),
                                r.vehicleNumber(),
                                r.expectedAt(),
                                r.accompanyingMembers())));
    }

    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF')")
    VisitorResponse.Photo photo(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
        return mapper.toResponse(
                service.uploadPhoto(
                        CurrentUser.societyId(), actor(), id, new SpringUploadedFile(file)));
    }

    @GetMapping("/photos/{id}")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
    ResponseEntity<Resource> photo(@PathVariable UUID id) {
        var photo = service.photo(CurrentUser.societyId(), actor(), id);
        var contentType = photo.contentType() == null ? "image/jpeg" : photo.contentType();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(photo.content().length())
                .body(
                        new org.springframework.core.io.InputStreamResource(
                                photo.content().stream()));
    }

    @PatchMapping("/{id}/approval")
    @PreAuthorize("hasRole('RESIDENT')")
    VisitorResponse.Mutation approval(@PathVariable UUID id, @Valid @RequestBody Approval r) {
        return mapper.toResponse(
                service.residentDecision(
                        CurrentUser.societyId(),
                        CurrentUser.id(),
                        id,
                        new VisitorModels.DecisionCommand(
                                ApprovalAction.from(r.decision()), r.note())));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('RESIDENT')")
    VisitorResponse.Mutation cancel(@PathVariable UUID id, @Valid @RequestBody Cancellation r) {
        return mapper.toResponse(
                service.residentCancel(CurrentUser.societyId(), CurrentUser.id(), id, r.note()));
    }

    @PatchMapping("/{id}/staff-approval")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF')")
    VisitorResponse.Mutation staffApproval(
            @PathVariable UUID id, @Valid @RequestBody StaffApproval r) {
        return mapper.toResponse(
                service.staffApproval(CurrentUser.societyId(), actor(), id, r.note()));
    }

    @PatchMapping("/{id}/staff-decision")
    @PreAuthorize("hasRole('STAFF')")
    VisitorResponse.Mutation staffDecision(@PathVariable UUID id, @Valid @RequestBody Approval r) {
        return mapper.toResponse(
                service.targetStaffDecision(
                        CurrentUser.societyId(),
                        CurrentUser.id(),
                        id,
                        new VisitorModels.DecisionCommand(
                                ApprovalAction.from(r.decision()), r.note())));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF')")
    VisitorResponse.Mutation status(@PathVariable UUID id, @Valid @RequestBody Status r) {
        return mapper.toResponse(
                service.updateStatus(
                        CurrentUser.societyId(), actor(), id, VisitorStatus.from(r.status())));
    }

    private VisitorModels.Actor actor() {
        return new VisitorModels.Actor(CurrentUser.id(), UserRole.from(CurrentUser.role()));
    }
}
