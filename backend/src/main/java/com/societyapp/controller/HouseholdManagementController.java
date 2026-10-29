package com.societyapp.controller;

import com.societyapp.application.model.household.HouseholdModels;
import com.societyapp.application.port.in.HouseholdUseCase;
import com.societyapp.controller.mapper.HouseholdHttpMapper;
import com.societyapp.controller.model.HouseholdResponse;
import com.societyapp.domain.type.*;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/households")
@PreAuthorize("hasAnyRole('SOCIETY_ADMIN','STAFF','RESIDENT')")
public class HouseholdManagementController {
    private final HouseholdUseCase service;
    private final HouseholdHttpMapper mapper = new HouseholdHttpMapper();

    public HouseholdManagementController(HouseholdUseCase service) {
        this.service = service;
    }

    public record MemberRequest(
            UUID residentId,
            @NotBlank @Size(max = 150) String fullName,
            @NotBlank @Size(max = 40) String relationship,
            @Pattern(regexp = "^$|[0-9+() -]{7,20}$") String mobile,
            @Email String email,
            LocalDate dateOfBirth,
            @Pattern(regexp = "MALE|FEMALE|OTHER|PREFER_NOT_TO_SAY") String gender,
            @Pattern(regexp = "A\\+|A-|B\\+|B-|AB\\+|AB-|O\\+|O-") String bloodGroup,
            @Size(max = 120) String occupation,
            @Pattern(regexp = "AADHAAR|PASSPORT|DRIVING_LICENCE|VOTER_ID|OTHER")
                    String identityType,
            @Pattern(regexp = "[A-Za-z0-9]{4}") String identityLast4,
            @Size(max = 150) String emergencyContactName,
            @Pattern(regexp = "^$|[0-9+() -]{7,20}$") String emergencyContactMobile,
            Boolean accessEnabled) {}

    public record VehicleRequest(
            UUID residentId,
            @NotBlank @Pattern(regexp = "CAR|MOTORCYCLE|SCOOTER|BICYCLE|OTHER") String vehicleType,
            @NotBlank @Pattern(regexp = "[A-Za-z0-9 -]{4,20}") String registrationNumber,
            @Size(max = 80) String make,
            @Size(max = 80) String model,
            @Size(max = 40) String color,
            @Size(max = 40) String parkingSlot,
            @Size(max = 40) String stickerNumber) {}

    public record ContactRequest(
            UUID residentId,
            @NotBlank @Size(max = 150) String fullName,
            @NotBlank @Size(max = 40) String relationship,
            @NotBlank @Pattern(regexp = "[0-9+() -]{7,20}") String mobile,
            @Pattern(regexp = "^$|[0-9+() -]{7,20}$") String alternateMobile,
            @Min(1) @Max(3) int priority) {}

    public record ReviewRequest(
            @NotNull UUID residentId,
            @NotBlank @Pattern(regexp = "PENDING|VERIFIED|REJECTED") String status,
            @Size(max = 300) String note) {}

    @GetMapping
    HouseholdResponse.Workspace workspace() {
        return mapper.toResponse(
                service.workspace(
                        CurrentUser.societyId(),
                        CurrentUser.id(),
                        UserRole.from(CurrentUser.role())));
    }

    @PostMapping("/members")
    HouseholdResponse.Member addMember(@Valid @RequestBody MemberRequest request) {
        return mapper.toResponse(service.addMember(actor(request.residentId()), member(request)));
    }

    @PutMapping("/members/{id}")
    HouseholdResponse.Member updateMember(
            @PathVariable UUID id, @Valid @RequestBody MemberRequest request) {
        return mapper.toResponse(
                service.updateMember(actor(request.residentId()), id, member(request)));
    }

    @PatchMapping("/members/{id}/verification")
    HouseholdResponse.Member reviewMember(
            @PathVariable UUID id, @Valid @RequestBody ReviewRequest request) {
        return mapper.toResponse(
                service.reviewMember(actor(request.residentId()), id, review(request)));
    }

    @DeleteMapping("/members/{id}")
    HouseholdResponse.EntityResult removeMember(
            @PathVariable UUID id, @RequestParam(required = false) UUID residentId) {
        return mapper.toResponse(service.removeMember(actor(residentId), id));
    }

    @PostMapping("/vehicles")
    HouseholdResponse.Vehicle addVehicle(@Valid @RequestBody VehicleRequest request) {
        return mapper.toResponse(service.addVehicle(actor(request.residentId()), vehicle(request)));
    }

    @PutMapping("/vehicles/{id}")
    HouseholdResponse.Vehicle updateVehicle(
            @PathVariable UUID id, @Valid @RequestBody VehicleRequest request) {
        return mapper.toResponse(
                service.updateVehicle(actor(request.residentId()), id, vehicle(request)));
    }

    @PatchMapping("/vehicles/{id}/verification")
    HouseholdResponse.Vehicle reviewVehicle(
            @PathVariable UUID id, @Valid @RequestBody ReviewRequest request) {
        return mapper.toResponse(
                service.reviewVehicle(actor(request.residentId()), id, review(request)));
    }

    @DeleteMapping("/vehicles/{id}")
    HouseholdResponse.EntityResult removeVehicle(
            @PathVariable UUID id, @RequestParam(required = false) UUID residentId) {
        return mapper.toResponse(service.removeVehicle(actor(residentId), id));
    }

    @PostMapping("/emergency-contacts")
    HouseholdResponse.Contact addContact(@Valid @RequestBody ContactRequest request) {
        return mapper.toResponse(service.addContact(actor(request.residentId()), contact(request)));
    }

    @PutMapping("/emergency-contacts/{id}")
    HouseholdResponse.Contact updateContact(
            @PathVariable UUID id, @Valid @RequestBody ContactRequest request) {
        return mapper.toResponse(
                service.updateContact(actor(request.residentId()), id, contact(request)));
    }

    @DeleteMapping("/emergency-contacts/{id}")
    HouseholdResponse.EntityResult removeContact(
            @PathVariable UUID id, @RequestParam(required = false) UUID residentId) {
        return mapper.toResponse(service.removeContact(actor(residentId), id));
    }

    private HouseholdModels.Actor actor(UUID residentId) {
        return new HouseholdModels.Actor(
                CurrentUser.societyId(),
                CurrentUser.id(),
                UserRole.from(CurrentUser.role()),
                residentId);
    }

    private HouseholdModels.MemberCommand member(MemberRequest request) {
        return new HouseholdModels.MemberCommand(
                request.fullName(),
                request.relationship(),
                request.mobile(),
                request.email(),
                request.dateOfBirth(),
                request.gender() == null ? null : Gender.from(request.gender()),
                request.bloodGroup(),
                request.occupation(),
                IdentityType.from(request.identityType()),
                request.identityLast4(),
                request.emergencyContactName(),
                request.emergencyContactMobile(),
                request.accessEnabled());
    }

    private HouseholdModels.VehicleCommand vehicle(VehicleRequest request) {
        return new HouseholdModels.VehicleCommand(
                VehicleType.from(request.vehicleType()),
                request.registrationNumber(),
                request.make(),
                request.model(),
                request.color(),
                request.parkingSlot(),
                request.stickerNumber());
    }

    private HouseholdModels.ContactCommand contact(ContactRequest request) {
        return new HouseholdModels.ContactCommand(
                request.fullName(),
                request.relationship(),
                request.mobile(),
                request.alternateMobile(),
                request.priority());
    }

    private HouseholdModels.ReviewCommand review(ReviewRequest request) {
        return new HouseholdModels.ReviewCommand(
                VerificationStatus.from(request.status()), request.note());
    }
}
