package com.societyapp.application.command;

import com.societyapp.application.exception.AuthorizationException;
import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.mapper.HouseholdViewMapper;
import com.societyapp.application.model.household.HouseholdModels;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.time.*;
import java.util.*;

public final class HouseholdMemberCoordinator {
    private final UserAccountJpaRepository users;
    private final HouseholdMemberRepository members;
    private final StaffPermissionRepository permissions;
    private final HouseholdViewMapper views;

    public HouseholdMemberCoordinator(
            UserAccountJpaRepository users,
            HouseholdMemberRepository members,
            StaffPermissionRepository permissions,
            HouseholdViewMapper views) {
        this.users = users;
        this.members = members;
        this.permissions = permissions;
        this.views = views;
    }

    public HouseholdModels.Member add(
            HouseholdModels.Actor actor, HouseholdModels.MemberCommand command) {
        UUID residentId = targetResident(actor);
        var item = new HouseholdMember();
        item.setSocietyId(actor.societyId());
        item.setPrimaryResidentId(residentId);
        item.setMemberCode(memberCode());
        item.setCreatedAt(OffsetDateTime.now());
        apply(item, command);
        item.setVerificationStatus(VerificationStatus.PENDING.code());
        item.setActive(true);
        return views.member(members.save(item));
    }

    public HouseholdModels.Member update(
            HouseholdModels.Actor actor, UUID memberId, HouseholdModels.MemberCommand command) {
        UUID residentId = targetResident(actor);
        var item = requireMember(actor.societyId(), residentId, memberId);
        apply(item, command);
        if (actor.role() == UserRole.RESIDENT) {
            item.setVerificationStatus(VerificationStatus.PENDING.code());
            item.setVerificationNote(null);
            item.setReviewedBy(null);
            item.setReviewedAt(null);
        }
        return views.member(members.save(item));
    }

    public HouseholdModels.Member review(
            HouseholdModels.Actor actor, UUID memberId, HouseholdModels.ReviewCommand command) {
        requireManager(actor.actorId(), actor.role());
        requireResident(actor.societyId(), actor.residentId());
        var item = requireMember(actor.societyId(), actor.residentId(), memberId);
        var requestedStatus = reviewStatus(command.status());
        requireReviewNote(requestedStatus, command.note());
        item.setVerificationStatus(requestedStatus.code());
        item.setVerificationNote(blank(command.note()));
        item.setReviewedBy(actor.actorId());
        item.setReviewedAt(OffsetDateTime.now());
        item.setUpdatedAt(OffsetDateTime.now());
        return views.member(members.save(item));
    }

    public HouseholdModels.EntityResult remove(HouseholdModels.Actor actor, UUID memberId) {
        UUID residentId = targetResident(actor);
        var item = requireMember(actor.societyId(), residentId, memberId);
        item.setActive(false);
        item.setAccessEnabled(false);
        item.setUpdatedAt(OffsetDateTime.now());
        members.save(item);
        return new HouseholdModels.EntityResult(memberId, false);
    }

    private void apply(HouseholdMember item, HouseholdModels.MemberCommand command) {
        item.setFullName(command.fullName().trim());
        item.setRelationship(command.relationship().trim().toUpperCase(Locale.ROOT));
        item.setMobile(blank(command.mobile()));
        item.setEmail(blank(command.email()));
        item.setDateOfBirth(command.dateOfBirth());
        item.setGender(command.gender() == null ? null : command.gender().code());
        item.setBloodGroup(blank(command.bloodGroup()));
        item.setOccupation(blank(command.occupation()));
        item.setIdentityType(command.identityType() == null ? null : command.identityType().code());
        item.setIdentityLast4(
                command.identityLast4() == null || command.identityLast4().isBlank()
                        ? null
                        : command.identityLast4().trim().toUpperCase(Locale.ROOT));
        item.setEmergencyContactName(blank(command.emergencyContactName()));
        item.setEmergencyContactMobile(blank(command.emergencyContactMobile()));
        item.setAccessEnabled(Boolean.TRUE.equals(command.accessEnabled()));
        item.setUpdatedAt(OffsetDateTime.now());
    }

    private UUID targetResident(HouseholdModels.Actor actor) {
        if (actor.role() == UserRole.RESIDENT) {
            if (actor.residentId() != null && !actor.actorId().equals(actor.residentId()))
                throw new AuthorizationException("You can only manage your own household");
            requireResident(actor.societyId(), actor.actorId());
            return actor.actorId();
        }
        requireManager(actor.actorId(), actor.role());
        if (actor.residentId() == null) throw new IllegalArgumentException("Resident is required");
        requireResident(actor.societyId(), actor.residentId());
        return actor.residentId();
    }

    private void requireManager(UUID actorId, UserRole role) {
        if (role == UserRole.SOCIETY_ADMIN) return;
        if (role == UserRole.STAFF
                && permissions.existsByStaffUserIdAndPermissionCode(
                        actorId, StaffPermissionCode.HOUSEHOLDS.code())) return;
        throw new AuthorizationException("Household management permission is required");
    }

    private UserAccount requireResident(UUID societyId, UUID residentId) {
        return users.findById(residentId)
                .filter(user -> societyId.equals(user.getSocietyId()))
                .filter(user -> UserRole.RESIDENT.matches(user.getRole()))
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found"));
    }

    private HouseholdMember requireMember(UUID societyId, UUID residentId, UUID memberId) {
        return members.findByIdAndSocietyIdAndPrimaryResidentId(memberId, societyId, residentId)
                .filter(HouseholdMember::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Household member not found"));
    }

    private VerificationStatus reviewStatus(VerificationStatus requested) {
        if (requested == VerificationStatus.PENDING)
            throw new IllegalArgumentException("Review status must be VERIFIED or REJECTED");
        return requested;
    }

    private void requireReviewNote(VerificationStatus status, String note) {
        if (status == VerificationStatus.REJECTED && (note == null || note.isBlank()))
            throw new IllegalArgumentException("Rejection note is required");
    }

    private String memberCode() {
        return "HM-"
                + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 10)
                        .toUpperCase(Locale.ROOT);
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
