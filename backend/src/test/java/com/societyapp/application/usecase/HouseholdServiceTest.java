package com.societyapp.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.societyapp.application.exception.AuthorizationException;
import com.societyapp.application.model.household.HouseholdModels;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.application.service.HouseholdService;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HouseholdServiceTest {
    private final UserAccountJpaRepository users = mock(UserAccountJpaRepository.class);
    private final HouseholdMemberRepository members = mock(HouseholdMemberRepository.class);
    private final HouseholdVehicleRepository vehicles = mock(HouseholdVehicleRepository.class);
    private final HouseholdEmergencyContactRepository contacts =
            mock(HouseholdEmergencyContactRepository.class);
    private final FlatRepository flats = mock(FlatRepository.class);
    private final FloorRepository floors = mock(FloorRepository.class);
    private final BlockRepository blocks = mock(BlockRepository.class);
    private final StaffPermissionRepository permissions = mock(StaffPermissionRepository.class);
    private final HouseholdService service =
            new HouseholdService(
                    users, members, vehicles, contacts, flats, floors, blocks, permissions);

    private final UUID societyId = UUID.randomUUID();
    private final UUID residentId = UUID.randomUUID();
    private final UUID staffId = UUID.randomUUID();
    private final UUID recordId = UUID.randomUUID();

    @BeforeEach
    void defaults() {
        when(members.save(any(HouseholdMember.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(vehicles.save(any(HouseholdVehicle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(contacts.save(any(HouseholdEmergencyContact.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(flats.findAll()).thenReturn(List.of());
        when(members.findBySocietyIdAndPrimaryResidentIdAndActiveTrue(societyId, residentId))
                .thenReturn(List.of());
        when(vehicles.findBySocietyIdAndPrimaryResidentIdAndActiveTrueOrderByRegistrationNumber(
                        societyId, residentId))
                .thenReturn(List.of());
        when(contacts.findBySocietyIdAndPrimaryResidentIdAndActiveTrueOrderByPriority(
                        societyId, residentId))
                .thenReturn(List.of());
        when(users.findById(residentId)).thenReturn(Optional.of(resident()));
    }

    @Test
    void residentWorkspaceOnlyReturnsOwnHousehold() {
        var result = service.workspace(societyId, residentId, UserRole.RESIDENT);

        assertThat(result.households()).hasSize(1);
        assertThat(result.residentCount()).isEqualTo(1);
        assertThat(result.memberCount()).isZero();
        assertThat(result.vehicleCount()).isZero();
        assertThat(result.pendingVerification()).isZero();
    }

    @Test
    void staffRequiresExplicitHouseholdPermission() {
        assertThatThrownBy(() -> service.workspace(societyId, staffId, UserRole.STAFF))
                .isInstanceOf(AuthorizationException.class);

        when(permissions.existsByStaffUserIdAndPermissionCode(staffId, "HOUSEHOLDS"))
                .thenReturn(true);
        when(users.findBySocietyIdAndRoleOrderByFullName(societyId, "RESIDENT"))
                .thenReturn(List.of(resident()));
        assertThat(service.workspace(societyId, staffId, UserRole.STAFF).households()).hasSize(1);
    }

    @Test
    void residentCannotManageAnotherResidentsHousehold() {
        assertThatThrownBy(
                        () ->
                                service.addMember(
                                        actor(UserRole.RESIDENT, UUID.randomUUID()),
                                        memberCommand("Family Member", "SPOUSE")))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void newMemberReceivesCodePendingVerificationAndNormalizedIdentity() {
        var result =
                service.addMember(
                        actor(UserRole.RESIDENT, null),
                        new HouseholdModels.MemberCommand(
                                " Family Member ",
                                "spouse",
                                "9000012345",
                                "family@example.com",
                                LocalDate.of(1990, 1, 1),
                                Gender.FEMALE,
                                "B+",
                                "Designer",
                                IdentityType.AADHAAR,
                                "ab12",
                                "Emergency",
                                "9111111111",
                                true));

        assertThat(result.memberCode()).startsWith("HM-");
        assertThat(result.fullName()).isEqualTo("Family Member");
        assertThat(result.relationship()).isEqualTo("SPOUSE");
        assertThat(result.identityLast4()).isEqualTo("AB12");
        assertThat(result.accessEnabled()).isTrue();
        assertThat(result.verificationStatus()).isEqualTo(VerificationStatus.PENDING);
    }

    @Test
    void residentEditResetsPreviouslyVerifiedMemberToPending() {
        HouseholdMember member = member("VERIFIED");
        when(members.findByIdAndSocietyIdAndPrimaryResidentId(recordId, societyId, residentId))
                .thenReturn(Optional.of(member));

        var result =
                service.updateMember(
                        actor(UserRole.RESIDENT, null),
                        recordId,
                        memberCommand("Family Member", "SPOUSE"));

        assertThat(result.verificationStatus()).isEqualTo(VerificationStatus.PENDING);
        assertThat(member.getReviewedBy()).isNull();
        assertThat(member.getReviewedAt()).isNull();
    }

    @Test
    void managerVerificationRequiresRejectionNote() {
        HouseholdMember member = member("PENDING");
        when(members.findByIdAndSocietyIdAndPrimaryResidentId(recordId, societyId, residentId))
                .thenReturn(Optional.of(member));

        assertThatThrownBy(
                        () ->
                                service.reviewMember(
                                        actor(UserRole.SOCIETY_ADMIN, residentId),
                                        recordId,
                                        new HouseholdModels.ReviewCommand(
                                                VerificationStatus.REJECTED, " ")))
                .hasMessageContaining("Rejection note");

        var result =
                service.reviewMember(
                        actor(UserRole.SOCIETY_ADMIN, residentId),
                        recordId,
                        new HouseholdModels.ReviewCommand(VerificationStatus.VERIFIED, null));
        assertThat(result.verificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
        assertThat(member.getReviewedBy()).isEqualTo(staffId);
    }

    @Test
    void vehicleRegistrationIsNormalizedAndResidentEditResetsVerification() {
        var created =
                service.addVehicle(actor(UserRole.RESIDENT, null), vehicleCommand("mh 12 ab 1234"));
        assertThat(created.registrationNumber()).isEqualTo("MH12AB1234");
        assertThat(created.verificationStatus()).isEqualTo(VerificationStatus.PENDING);

        HouseholdVehicle vehicle = vehicle("VERIFIED");
        when(vehicles.findByIdAndSocietyIdAndPrimaryResidentId(recordId, societyId, residentId))
                .thenReturn(Optional.of(vehicle));
        var updated =
                service.updateVehicle(
                        actor(UserRole.RESIDENT, null), recordId, vehicleCommand("MH12AB1234"));
        assertThat(updated.verificationStatus()).isEqualTo(VerificationStatus.PENDING);
    }

    @Test
    void emergencyContactsAreLimitedToThreeAndRemovalIsSoft() {
        when(contacts.findBySocietyIdAndPrimaryResidentIdAndActiveTrueOrderByPriority(
                        societyId, residentId))
                .thenReturn(List.of(contact(), contact(), contact()));
        assertThatThrownBy(
                        () -> service.addContact(actor(UserRole.RESIDENT, null), contactCommand()))
                .hasMessageContaining("up to 3");

        HouseholdEmergencyContact contact = contact();
        when(contacts.findByIdAndSocietyIdAndPrimaryResidentId(recordId, societyId, residentId))
                .thenReturn(Optional.of(contact));
        assertThat(service.removeContact(actor(UserRole.RESIDENT, null), recordId).active())
                .isFalse();
        assertThat(contact.isActive()).isFalse();
    }

    @Test
    void deletingMemberDisablesSocietyAccess() {
        HouseholdMember member = member("VERIFIED");
        member.setAccessEnabled(true);
        when(members.findByIdAndSocietyIdAndPrimaryResidentId(recordId, societyId, residentId))
                .thenReturn(Optional.of(member));

        service.removeMember(actor(UserRole.RESIDENT, null), recordId);

        assertThat(member.isActive()).isFalse();
        assertThat(member.isAccessEnabled()).isFalse();
    }

    private UserAccount resident() {
        UserAccount user = new UserAccount();
        user.setSocietyId(societyId);
        user.setFullName("Resident");
        user.setEmail("resident@example.com");
        user.setMobile("9000000000");
        user.setRole("RESIDENT");
        user.setStatus("ACTIVE");
        return user;
    }

    private HouseholdModels.Actor actor(UserRole role, UUID targetResidentId) {
        return new HouseholdModels.Actor(
                societyId,
                role == UserRole.RESIDENT ? residentId : staffId,
                role,
                targetResidentId);
    }

    private HouseholdModels.MemberCommand memberCommand(String name, String relationship) {
        return new HouseholdModels.MemberCommand(
                name,
                relationship,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false);
    }

    private HouseholdModels.VehicleCommand vehicleCommand(String registration) {
        return new HouseholdModels.VehicleCommand(
                VehicleType.CAR, registration, "Tata", "Nexon", "Blue", "P-12", "ST-12");
    }

    private HouseholdModels.ContactCommand contactCommand() {
        return new HouseholdModels.ContactCommand("Emergency", "BROTHER", "9111111111", null, 1);
    }

    private HouseholdMember member(String status) {
        HouseholdMember member = new HouseholdMember();
        member.setSocietyId(societyId);
        member.setPrimaryResidentId(residentId);
        member.setMemberCode("HM-TEST00001");
        member.setFullName("Family Member");
        member.setRelationship("SPOUSE");
        member.setVerificationStatus(status);
        member.setActive(true);
        member.setCreatedAt(OffsetDateTime.now());
        member.setUpdatedAt(OffsetDateTime.now());
        return member;
    }

    private HouseholdVehicle vehicle(String status) {
        HouseholdVehicle vehicle = new HouseholdVehicle();
        vehicle.setSocietyId(societyId);
        vehicle.setPrimaryResidentId(residentId);
        vehicle.setVehicleType("CAR");
        vehicle.setRegistrationNumber("MH12AB1234");
        vehicle.setVerificationStatus(status);
        vehicle.setActive(true);
        vehicle.setCreatedAt(OffsetDateTime.now());
        vehicle.setUpdatedAt(OffsetDateTime.now());
        return vehicle;
    }

    private HouseholdEmergencyContact contact() {
        HouseholdEmergencyContact contact = new HouseholdEmergencyContact();
        contact.setSocietyId(societyId);
        contact.setPrimaryResidentId(residentId);
        contact.setFullName("Emergency");
        contact.setRelationship("BROTHER");
        contact.setMobile("9111111111");
        contact.setPriority(1);
        contact.setActive(true);
        contact.setCreatedAt(OffsetDateTime.now());
        contact.setUpdatedAt(OffsetDateTime.now());
        return contact;
    }
}
