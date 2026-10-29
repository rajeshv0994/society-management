package com.societyapp.controller;

import com.societyapp.application.model.emergency.EmergencyModels;
import com.societyapp.application.port.in.EmergencyIncidentUseCase;
import com.societyapp.controller.mapper.EmergencyHttpMapper;
import com.societyapp.controller.model.EmergencyResponse;
import com.societyapp.domain.type.*;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emergencies")
@PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
public class EmergencyIncidentController {
    private final EmergencyIncidentUseCase service;
    private final EmergencyHttpMapper mapper = new EmergencyHttpMapper();

    public EmergencyIncidentController(EmergencyIncidentUseCase service) {
        this.service = service;
    }

    public record IncidentRequest(
            UUID residentId,
            UUID flatId,
            @NotBlank
                    @Pattern(
                            regexp =
                                    "MEDICAL|ACCIDENT|FIRE|SECURITY|GAS_LEAK|ELECTRICAL|ELEVATOR|NATURAL_DISASTER|OTHER")
                    String incidentType,
            @NotBlank @Pattern(regexp = "LOW|MEDIUM|HIGH|CRITICAL") String severity,
            @NotBlank @Size(max = 180) String title,
            @NotBlank @Size(max = 1500) String description,
            @NotBlank @Size(max = 300) String locationDetails,
            @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
            @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude,
            @Min(0) @Max(999) int peopleAffected,
            @Size(max = 150) String injuredPersonName,
            @Pattern(regexp = "^$|[0-9+() -]{7,20}$") String injuredPersonMobile,
            @Size(max = 800) String injuryDetails) {}

    public record ActionRequest(
            @NotBlank
                    @Pattern(
                            regexp =
                                    "ACKNOWLEDGE|ASSIGN|START_RESPONSE|ARRIVE|REQUEST_AMBULANCE|REQUEST_POLICE|REQUEST_FIRE|NOTIFY_CONTACT|ADD_UPDATE|ESCALATE|STABILIZE|RESOLVE|CLOSE|CANCEL|FALSE_ALARM|REOPEN")
                    String action,
            @Size(max = 1000) String note,
            UUID assignedStaffId,
            @Size(max = 180) String hospitalName,
            @Size(max = 120) String externalReference,
            @Pattern(regexp = "MEDIUM|HIGH|CRITICAL") String severity) {}

    public record DirectoryRequest(
            @NotBlank
                    @Pattern(
                            regexp =
                                    "AMBULANCE|HOSPITAL|POLICE|FIRE|SOCIETY_SECURITY|DISASTER_CONTROL|OTHER")
                    String contactType,
            @NotBlank @Size(max = 150) String name,
            @NotBlank @Pattern(regexp = "[0-9+() -]{7,20}") String phone,
            @Pattern(regexp = "^$|[0-9+() -]{7,20}$") String alternatePhone,
            @Size(max = 300) String address,
            boolean available24x7,
            @Min(1) @Max(99) int priority) {}

    @GetMapping
    EmergencyResponse.Workspace workspace() {
        return mapper.toResponse(service.workspace(actor()));
    }

    @GetMapping("/{id}/history")
    List<EmergencyResponse.HistoryEntry> history(@PathVariable UUID id) {
        return service.history(actor(), id).stream().map(mapper::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    EmergencyResponse.Incident create(@Valid @RequestBody IncidentRequest request) {
        return mapper.toResponse(
                service.create(
                        actor(),
                        new EmergencyModels.CreateCommand(
                                request.residentId(),
                                request.flatId(),
                                EmergencyIncidentType.from(request.incidentType()),
                                IncidentSeverity.from(request.severity()),
                                request.title(),
                                request.description(),
                                request.locationDetails(),
                                request.latitude(),
                                request.longitude(),
                                request.peopleAffected(),
                                request.injuredPersonName(),
                                request.injuredPersonMobile(),
                                request.injuryDetails())));
    }

    @PatchMapping("/{id}")
    EmergencyResponse.Incident action(
            @PathVariable UUID id, @Valid @RequestBody ActionRequest request) {
        return mapper.toResponse(
                service.action(
                        actor(),
                        id,
                        new EmergencyModels.ActionCommand(
                                EmergencyAction.from(request.action()),
                                request.note(),
                                request.assignedStaffId(),
                                request.hospitalName(),
                                request.externalReference(),
                                request.severity() == null
                                        ? null
                                        : IncidentSeverity.from(request.severity()))));
    }

    @PostMapping("/directory")
    @ResponseStatus(HttpStatus.CREATED)
    EmergencyResponse.DirectoryContact createDirectory(
            @Valid @RequestBody DirectoryRequest request) {
        return saveDirectory(null, request);
    }

    @PutMapping("/directory/{id}")
    EmergencyResponse.DirectoryContact updateDirectory(
            @PathVariable UUID id, @Valid @RequestBody DirectoryRequest request) {
        return saveDirectory(id, request);
    }

    @DeleteMapping("/directory/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void removeDirectory(@PathVariable UUID id) {
        service.removeDirectory(actor(), id);
    }

    private EmergencyResponse.DirectoryContact saveDirectory(UUID id, DirectoryRequest request) {
        return mapper.toResponse(
                service.saveDirectory(
                        actor(),
                        id,
                        new EmergencyModels.DirectoryCommand(
                                EmergencyContactType.from(request.contactType()),
                                request.name(),
                                request.phone(),
                                request.alternatePhone(),
                                request.address(),
                                request.available24x7(),
                                request.priority())));
    }

    private EmergencyModels.Actor actor() {
        return new EmergencyModels.Actor(
                CurrentUser.societyId(), CurrentUser.id(), UserRole.from(CurrentUser.role()));
    }
}
