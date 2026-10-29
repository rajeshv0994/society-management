package com.societyapp.controller;

import com.societyapp.application.model.activity.ActivityModels;
import com.societyapp.application.port.in.ActivityUseCase;
import com.societyapp.controller.mapper.ActivityHttpMapper;
import com.societyapp.controller.model.ActivityResponse;
import com.societyapp.domain.type.*;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/activities")
@PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
public class ActivityController {
    private final ActivityUseCase service;
    private final ActivityHttpMapper mapper = new ActivityHttpMapper();

    public ActivityController(ActivityUseCase service) {
        this.service = service;
    }

    public record ActivityRequest(
            @NotBlank @Size(max = 180) String title,
            @NotBlank
                    @Pattern(
                            regexp =
                                    "SPORTS|CULTURAL|EDUCATIONAL|HEALTH|SOCIAL|RELIGIOUS|CHILDREN|SENIOR_CITIZEN|VOLUNTEERING|OTHER")
                    String category,
            @NotBlank @Size(max = 2000) String description,
            @NotBlank @Size(max = 300) String venue,
            @NotNull OffsetDateTime startsAt,
            @NotNull OffsetDateTime endsAt,
            @NotNull OffsetDateTime registrationOpensAt,
            @NotNull OffsetDateTime registrationClosesAt,
            @Min(1) Integer capacity,
            @NotNull @DecimalMin("0") BigDecimal feePerPerson,
            boolean approvalRequired,
            @Min(0) @Max(720) int cancellationHours,
            @NotBlank
                    @Pattern(regexp = "ALL_MEMBERS|ADULTS|CHILDREN|SENIOR_CITIZENS|WOMEN|MEN|OTHER")
                    String audience,
            @Size(max = 150) String organizerName,
            @Pattern(regexp = "^$|[0-9+() -]{7,20}$") String organizerMobile,
            @Size(max = 1000) String instructions,
            boolean publishNow) {}

    public record ActivityAction(
            @NotBlank @Pattern(regexp = "PUBLISH|CANCEL|COMPLETE|ARCHIVE") String action,
            @Size(max = 500) String reason) {}

    public record Join(
            @Min(1) @Max(50) int participantCount,
            @Size(max = 1000) String participantNames,
            @Pattern(regexp = "^$|[0-9+() -]{7,20}$") String contactMobile,
            @Size(max = 500) String notes) {}

    public record Checkout(
            @NotBlank @Pattern(regexp = "UPI|CARD|NET_BANKING") String paymentMethod) {}

    public record ParticipationAction(
            @NotBlank @Pattern(regexp = "APPROVE|REJECT|CHECK_IN|ATTENDED|NO_SHOW|CANCEL|REFUND")
                    String action,
            @Size(max = 500) String reason) {}

    public record PhotoAction(
            @NotBlank @Pattern(regexp = "APPROVE|REJECT|REMOVE|FEATURE|UNFEATURE") String action,
            @Size(max = 500) String reason) {}

    @GetMapping
    ActivityResponse.Workspace workspace() {
        return mapper.toResponse(service.workspace(CurrentUser.societyId(), actor()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ActivityResponse.Activity create(@Valid @RequestBody ActivityRequest request) {
        return mapper.toResponse(
                service.create(CurrentUser.societyId(), actor(), command(request)));
    }

    @PutMapping("/{id}")
    ActivityResponse.Activity update(
            @PathVariable UUID id, @Valid @RequestBody ActivityRequest request) {
        return mapper.toResponse(
                service.update(CurrentUser.societyId(), actor(), id, command(request)));
    }

    @PatchMapping("/{id}")
    ActivityResponse.Activity activityAction(
            @PathVariable UUID id, @Valid @RequestBody ActivityAction request) {
        return mapper.toResponse(
                service.activityAction(
                        CurrentUser.societyId(),
                        actor(),
                        id,
                        new ActivityModels.ActionCommand(
                                com.societyapp.domain.type.ActivityAction.from(request.action()),
                                request.reason())));
    }

    @PostMapping("/{id}/join")
    @ResponseStatus(HttpStatus.CREATED)
    ActivityResponse.Participation join(@PathVariable UUID id, @Valid @RequestBody Join request) {
        return mapper.toResponse(
                service.join(
                        CurrentUser.societyId(),
                        actor(),
                        id,
                        new ActivityModels.JoinCommand(
                                request.participantCount(), request.participantNames(),
                                request.contactMobile(), request.notes())));
    }

    @PostMapping("/participations/{id}/checkout")
    ActivityResponse.Participation checkout(
            @PathVariable UUID id, @Valid @RequestBody Checkout request) {
        return mapper.toResponse(
                service.checkout(
                        CurrentUser.societyId(),
                        actor(),
                        id,
                        PaymentMethod.from(request.paymentMethod())));
    }

    @PostMapping("/participations/{id}/cash")
    ActivityResponse.Participation cash(@PathVariable UUID id) {
        return mapper.toResponse(service.recordCash(CurrentUser.societyId(), actor(), id));
    }

    @PatchMapping("/participations/{id}")
    ActivityResponse.Participation participationAction(
            @PathVariable UUID id, @Valid @RequestBody ParticipationAction request) {
        return mapper.toResponse(
                service.participationAction(
                        CurrentUser.societyId(),
                        actor(),
                        id,
                        new ActivityModels.ParticipationActionCommand(
                                com.societyapp.domain.type.ParticipationAction.from(
                                        request.action()),
                                request.reason())));
    }

    @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    ActivityResponse.Photo uploadPhoto(
            @PathVariable UUID id,
            @RequestParam(required = false) @Size(max = 500) String caption,
            @RequestPart("file") MultipartFile file) {
        return mapper.toResponse(
                service.uploadPhoto(
                        CurrentUser.societyId(),
                        actor(),
                        id,
                        caption,
                        new SpringUploadedFile(file)));
    }

    @GetMapping("/photos/{id}")
    ResponseEntity<Resource> photo(@PathVariable UUID id) {
        var photo = service.photo(CurrentUser.societyId(), actor(), id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.contentType()))
                .contentLength(photo.content().length())
                .body(
                        new org.springframework.core.io.InputStreamResource(
                                photo.content().stream()));
    }

    @PatchMapping("/photos/{id}")
    ActivityResponse.Photo photoAction(
            @PathVariable UUID id, @Valid @RequestBody PhotoAction request) {
        return mapper.toResponse(
                service.photoAction(
                        CurrentUser.societyId(),
                        actor(),
                        id,
                        new ActivityModels.PhotoActionCommand(
                                com.societyapp.domain.type.PhotoAction.from(request.action()),
                                request.reason())));
    }

    @GetMapping("/{id}/history")
    List<ActivityResponse.Event> history(@PathVariable UUID id) {
        return service.history(CurrentUser.societyId(), actor(), id).stream()
                .map(mapper::toResponse)
                .toList();
    }

    private ActivityModels.Actor actor() {
        return new ActivityModels.Actor(CurrentUser.id(), UserRole.from(CurrentUser.role()));
    }

    private ActivityModels.ActivityCommand command(ActivityRequest request) {
        return new ActivityModels.ActivityCommand(
                request.title(),
                ActivityCategory.from(request.category()),
                request.description(),
                request.venue(),
                request.startsAt(),
                request.endsAt(),
                request.registrationOpensAt(),
                request.registrationClosesAt(),
                request.capacity(),
                request.feePerPerson(),
                request.approvalRequired(),
                request.cancellationHours(),
                ActivityAudience.from(request.audience()),
                request.organizerName(),
                request.organizerMobile(),
                request.instructions(),
                request.publishNow());
    }
}
