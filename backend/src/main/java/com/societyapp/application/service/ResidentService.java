package com.societyapp.application.service;

import com.societyapp.application.mapper.ResidentViewMapper;
import com.societyapp.application.model.resident.ResidentModels;
import com.societyapp.application.port.in.ResidentUseCase;
import com.societyapp.application.port.in.StructureAccessUseCase;
import com.societyapp.application.port.out.PasswordCodec;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.usecase.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.AccountStatus;
import com.societyapp.domain.type.ApprovalAction;
import com.societyapp.domain.type.FlatOccupancyStatus;
import com.societyapp.domain.type.RegistrationStatus;
import com.societyapp.domain.type.ResidentType;
import com.societyapp.domain.type.UserRole;
import com.societyapp.domain.type.VerificationStatus;
import java.time.*;
import java.util.*;

public class ResidentService implements ResidentUseCase {
    private final ResidentRegistrationRepository registrations;
    private final UserAccountJpaRepository users;
    private final FlatRepository flats;
    private final ComplaintRepository complaints;
    private final MaintenanceInvoiceRepository invoices;
    private final PaymentRepository payments;
    private final VisitorRepository visitors;
    private final HouseholdMemberRepository householdMembers;
    private final AmenityBookingRepository amenityBookings;
    private final StructureAccessUseCase access;
    private final PasswordCodec passwords;
    private final ResidentViewMapper viewMapper;

    public ResidentService(
            ResidentRegistrationRepository registrations,
            UserAccountJpaRepository users,
            FlatRepository flats,
            FloorRepository floors,
            BlockRepository blocks,
            ComplaintRepository complaints,
            MaintenanceInvoiceRepository invoices,
            PaymentRepository payments,
            VisitorRepository visitors,
            HouseholdMemberRepository householdMembers,
            AmenityBookingRepository amenityBookings,
            AmenityRepository amenities,
            StructureAccessUseCase access,
            PasswordCodec passwords) {
        this.registrations = registrations;
        this.users = users;
        this.flats = flats;
        this.complaints = complaints;
        this.invoices = invoices;
        this.payments = payments;
        this.visitors = visitors;
        this.householdMembers = householdMembers;
        this.amenityBookings = amenityBookings;
        this.access = access;
        this.passwords = passwords;
        this.viewMapper = new ResidentViewMapper(flats, floors, blocks, amenities);
    }

    @Override
    public List<ResidentModels.SpaceOption> spaces(UUID societyId) {
        return viewMapper.availableSpaces(societyId);
    }

    @Override
    public ResidentModels.RegistrationResult register(ResidentModels.RegistrationCommand command) {
        if (!access.belongsToSociety(command.spaceId(), command.societyId())) {
            throw new IllegalArgumentException("Space does not belong to society");
        }
        if (command.moveInDate() != null
                && command.moveOutDate() != null
                && !command.moveOutDate().isAfter(command.moveInDate())) {
            throw new IllegalArgumentException("Move-out must be after move-in");
        }
        var registration = new ResidentRegistration();
        registration.setSocietyId(command.societyId());
        registration.setFlatId(command.spaceId());
        registration.setResidentType(command.residentType().code());
        registration.setFullName(command.fullName());
        registration.setMobile(command.mobile());
        registration.setEmail(command.email());
        registration.setDob(command.dateOfBirth());
        registration.setGender(command.gender());
        registration.setEmergencyContact(command.emergencyContact());
        registration.setMoveInDate(command.moveInDate());
        registration.setMoveOutDate(command.moveOutDate());
        registration.setStatus(RegistrationStatus.PENDING_APPROVAL.code());
        registration.setInvited(false);
        registration.setSubmittedAt(OffsetDateTime.now());
        registration = registrations.save(registration);
        return new ResidentModels.RegistrationResult(
                registration.getId(), RegistrationStatus.PENDING_APPROVAL);
    }

    @Override
    public List<ResidentModels.RegistrationView> pending(UUID societyId) {
        return registrations
                .findBySocietyIdAndStatusOrderBySubmittedAtDesc(
                        societyId, RegistrationStatus.PENDING_APPROVAL.code())
                .stream()
                .map(viewMapper::registration)
                .toList();
    }

    @Override
    public List<ResidentModels.Summary> residents(UUID societyId) {
        return users
                .findBySocietyIdAndRoleOrderByFullName(societyId, UserRole.RESIDENT.code())
                .stream()
                .map(viewMapper::resident)
                .toList();
    }

    @Override
    public ResidentModels.CreatedResident createResident(
            ResidentModels.AdminCreateCommand command) {
        var registrationCommand = command.registration();
        if (users.findByEmailIgnoreCase(registrationCommand.email().trim()).isPresent()) {
            throw new IllegalStateException("A user with this email already exists");
        }
        var normalized =
                new ResidentModels.RegistrationCommand(
                        registrationCommand.societyId(),
                        registrationCommand.spaceId(),
                        registrationCommand.residentType(),
                        registrationCommand.fullName().trim(),
                        registrationCommand.mobile().trim(),
                        registrationCommand.email().trim().toLowerCase(Locale.ROOT),
                        registrationCommand.dateOfBirth(),
                        registrationCommand.gender(),
                        registrationCommand.emergencyContact(),
                        registrationCommand.moveInDate(),
                        registrationCommand.moveOutDate());
        var registration = register(normalized);
        var result =
                review(
                        normalized.societyId(),
                        registration.registrationId(),
                        new ResidentModels.ReviewCommand(
                                command.reviewerId(),
                                ApprovalAction.APPROVE,
                                null,
                                command.temporaryPassword()));
        return new ResidentModels.CreatedResident(
                registration.registrationId(), result.status(), result.userId());
    }

    @Override
    public ResidentModels.Detail resident(UUID societyId, UUID residentId) {
        var user = requireResident(societyId, residentId);
        var flat =
                flats.findAll().stream()
                        .filter(
                                item ->
                                        residentId.equals(item.getActiveOwnerId())
                                                || residentId.equals(item.getActiveTenantId()))
                        .findFirst()
                        .orElseThrow(
                                () -> new IllegalStateException("Resident has no active space"));
        var invoiceList =
                invoices.findBySocietyIdAndFlatIdOrderByBillingMonthDesc(societyId, flat.getId());
        var invoiceIds = invoiceList.stream().map(MaintenanceInvoice::getId).toList();
        var registration =
                registrations.findBySocietyIdOrderBySubmittedAtDesc(societyId).stream()
                        .filter(item -> flat.getId().equals(item.getFlatId()))
                        .filter(item -> user.getEmail().equalsIgnoreCase(item.getEmail()))
                        .findFirst()
                        .map(viewMapper::registration)
                        .orElse(null);
        return new ResidentModels.Detail(
                viewMapper.resident(user),
                registration,
                viewMapper.space(flat),
                householdMembers
                        .findBySocietyIdAndPrimaryResidentIdAndActiveTrue(societyId, residentId)
                        .stream()
                        .map(viewMapper::household)
                        .toList(),
                complaints
                        .findBySocietyIdAndRaisedByOrderByCreatedAtDesc(societyId, residentId)
                        .stream()
                        .map(viewMapper::complaint)
                        .toList(),
                invoiceList.stream().map(viewMapper::invoice).toList(),
                invoiceIds.isEmpty()
                        ? List.of()
                        : payments
                                .findBySocietyIdAndInvoiceIdInOrderByPaidAtDesc(
                                        societyId, invoiceIds)
                                .stream()
                                .map(viewMapper::payment)
                                .toList(),
                visitors
                        .findBySocietyIdAndFlatIdOrderByCreatedAtDesc(societyId, flat.getId())
                        .stream()
                        .map(viewMapper::visitor)
                        .toList(),
                amenityBookings
                        .findBySocietyIdAndBookedByOrderByStartsAtDesc(societyId, residentId)
                        .stream()
                        .map(viewMapper::booking)
                        .toList());
    }

    @Override
    public ResidentModels.EntityResult addHouseholdMember(
            UUID societyId, UUID residentId, ResidentModels.HouseholdCommand command) {
        requireResident(societyId, residentId);
        var member = new HouseholdMember();
        member.setSocietyId(societyId);
        member.setPrimaryResidentId(residentId);
        member.setMemberCode(
                "HM-"
                        + UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 10)
                                .toUpperCase(Locale.ROOT));
        member.setFullName(command.fullName());
        member.setRelationship(command.relationship());
        member.setMobile(command.mobile());
        member.setDateOfBirth(command.dateOfBirth());
        member.setAccessEnabled(false);
        member.setVerificationStatus(VerificationStatus.PENDING.code());
        member.setActive(true);
        member.setCreatedAt(OffsetDateTime.now());
        member.setUpdatedAt(OffsetDateTime.now());
        return new ResidentModels.EntityResult(householdMembers.save(member).getId(), null);
    }

    @Override
    public ResidentModels.EntityResult removeHouseholdMember(
            UUID societyId, UUID residentId, UUID memberId) {
        requireResident(societyId, residentId);
        var member =
                householdMembers
                        .findByIdAndSocietyIdAndPrimaryResidentId(memberId, societyId, residentId)
                        .orElseThrow(
                                () -> new IllegalArgumentException("Household member not found"));
        member.setActive(false);
        householdMembers.save(member);
        return new ResidentModels.EntityResult(memberId, false);
    }

    @Override
    public ResidentModels.ReviewResult review(
            UUID societyId, UUID registrationId, ResidentModels.ReviewCommand command) {
        var registration =
                registrations
                        .findByIdAndSocietyId(registrationId, societyId)
                        .filter(
                                item ->
                                        RegistrationStatus.PENDING_APPROVAL.matches(
                                                item.getStatus()))
                        .orElseThrow(
                                () -> new IllegalStateException("Registration is not pending"));
        if (command.decision() == ApprovalAction.REJECT) {
            if (command.reason() == null || command.reason().isBlank()) {
                throw new IllegalArgumentException("Rejection reason is required");
            }
            registration.setStatus(RegistrationStatus.REJECTED.code());
            registration.setRejectionReason(command.reason());
            registration.setReviewedBy(command.reviewerId());
            registration.setReviewedAt(OffsetDateTime.now());
            registrations.save(registration);
            return new ResidentModels.ReviewResult(RegistrationStatus.REJECTED, null);
        }
        if (command.temporaryPassword() == null) {
            throw new IllegalArgumentException("Temporary password is required for approval");
        }
        var flat = flats.findById(registration.getFlatId()).orElseThrow();
        boolean owner = ResidentType.OWNER.matches(registration.getResidentType());
        if ((owner && flat.getActiveOwnerId() != null)
                || (!owner && flat.getActiveTenantId() != null)) {
            throw new IllegalStateException(
                    "Space already has an active " + registration.getResidentType().toLowerCase());
        }
        var user = new UserAccount();
        user.setSocietyId(societyId);
        user.setFullName(registration.getFullName());
        user.setEmail(registration.getEmail());
        user.setMobile(registration.getMobile());
        user.setPasswordHash(passwords.encode(command.temporaryPassword()));
        user.setRole(UserRole.RESIDENT.code());
        user.setStatus(AccountStatus.ACTIVE.code());
        user.setCreatedAt(OffsetDateTime.now());
        user = users.save(user);
        if (owner) {
            flat.setActiveOwnerId(user.getId());
            flat.setOccupancyStatus(FlatOccupancyStatus.OWNER_OCCUPIED.code());
        } else {
            flat.setActiveTenantId(user.getId());
            flat.setOccupancyStatus(FlatOccupancyStatus.TENANT_OCCUPIED.code());
        }
        flats.save(flat);
        registration.setStatus(RegistrationStatus.ACTIVE.code());
        registration.setReviewedBy(command.reviewerId());
        registration.setReviewedAt(OffsetDateTime.now());
        registrations.save(registration);
        return new ResidentModels.ReviewResult(RegistrationStatus.ACTIVE, user.getId());
    }

    private UserAccount requireResident(UUID societyId, UUID residentId) {
        return users.findById(residentId)
                .filter(user -> societyId.equals(user.getSocietyId()))
                .filter(user -> UserRole.RESIDENT.matches(user.getRole()))
                .orElseThrow(() -> new IllegalArgumentException("Resident not found"));
    }
}
