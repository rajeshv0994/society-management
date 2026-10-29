package com.societyapp.application.model.household;

import com.societyapp.domain.type.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class HouseholdModels {
    private HouseholdModels() {}

    public record Actor(UUID societyId, UUID actorId, UserRole role, UUID residentId) {}

    public record MemberCommand(
            String fullName,
            String relationship,
            String mobile,
            String email,
            LocalDate dateOfBirth,
            Gender gender,
            String bloodGroup,
            String occupation,
            IdentityType identityType,
            String identityLast4,
            String emergencyContactName,
            String emergencyContactMobile,
            Boolean accessEnabled) {}

    public record VehicleCommand(
            VehicleType vehicleType,
            String registrationNumber,
            String make,
            String model,
            String color,
            String parkingSlot,
            String stickerNumber) {}

    public record ContactCommand(
            String fullName,
            String relationship,
            String mobile,
            String alternateMobile,
            int priority) {}

    public record ReviewCommand(VerificationStatus status, String note) {}

    public record User(
            UUID id,
            String fullName,
            String email,
            String mobile,
            boolean emailVerified,
            boolean mobileVerified,
            AccountStatus status) {}

    public record Space(
            UUID id,
            String spaceNumber,
            String occupancyStatus,
            BigDecimal areaSqft,
            ResidentType residentType,
            String section,
            String block) {}

    public record Member(
            UUID id,
            String memberCode,
            UUID residentId,
            String fullName,
            String relationship,
            String mobile,
            String email,
            LocalDate dateOfBirth,
            Gender gender,
            String bloodGroup,
            String occupation,
            IdentityType identityType,
            String identityLast4,
            String emergencyContactName,
            String emergencyContactMobile,
            boolean accessEnabled,
            VerificationStatus verificationStatus,
            String verificationNote,
            boolean active) {}

    public record Vehicle(
            UUID id,
            UUID residentId,
            VehicleType vehicleType,
            String registrationNumber,
            String make,
            String model,
            String color,
            String parkingSlot,
            String stickerNumber,
            VerificationStatus verificationStatus,
            String verificationNote,
            boolean active) {}

    public record Contact(
            UUID id,
            UUID residentId,
            String fullName,
            String relationship,
            String mobile,
            String alternateMobile,
            int priority,
            boolean active) {}

    public record Household(
            User resident,
            List<Space> spaces,
            List<Member> household,
            List<Vehicle> vehicles,
            List<Contact> emergencyContacts) {}

    public record Workspace(
            List<Household> households,
            int residentCount,
            int memberCount,
            int vehicleCount,
            long pendingVerification) {}

    public record EntityResult(UUID id, boolean active) {}
}
