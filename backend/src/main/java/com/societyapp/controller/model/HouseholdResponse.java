package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class HouseholdResponse {
    private HouseholdResponse() {}

    public record User(
            UUID id,
            @JsonProperty("full_name") String fullName,
            String email,
            String mobile,
            @JsonProperty("email_verified") boolean emailVerified,
            @JsonProperty("mobile_verified") boolean mobileVerified,
            String status) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Space(
            UUID id,
            @JsonProperty("space_number") String spaceNumber,
            @JsonProperty("unit_number") String unitNumber,
            @JsonProperty("flat_number") String flatNumber,
            @JsonProperty("occupancy_status") String occupancyStatus,
            @JsonProperty("area_sqft") BigDecimal areaSqft,
            @JsonProperty("resident_type") String residentType,
            String section,
            String floor,
            String block) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Member(
            UUID id,
            @JsonProperty("member_code") String memberCode,
            @JsonProperty("resident_id") UUID residentId,
            @JsonProperty("full_name") String fullName,
            String relationship,
            String mobile,
            String email,
            @JsonProperty("date_of_birth") LocalDate dateOfBirth,
            String gender,
            @JsonProperty("blood_group") String bloodGroup,
            String occupation,
            @JsonProperty("identity_type") String identityType,
            @JsonProperty("identity_last4") String identityLast4,
            @JsonProperty("emergency_contact_name") String emergencyContactName,
            @JsonProperty("emergency_contact_mobile") String emergencyContactMobile,
            @JsonProperty("access_enabled") boolean accessEnabled,
            @JsonProperty("verification_status") String verificationStatus,
            @JsonProperty("verification_note") String verificationNote,
            boolean active) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Vehicle(
            UUID id,
            @JsonProperty("resident_id") UUID residentId,
            @JsonProperty("vehicle_type") String vehicleType,
            @JsonProperty("registration_number") String registrationNumber,
            String make,
            String model,
            String color,
            @JsonProperty("parking_slot") String parkingSlot,
            @JsonProperty("sticker_number") String stickerNumber,
            @JsonProperty("verification_status") String verificationStatus,
            @JsonProperty("verification_note") String verificationNote,
            boolean active) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Contact(
            UUID id,
            @JsonProperty("resident_id") UUID residentId,
            @JsonProperty("full_name") String fullName,
            String relationship,
            String mobile,
            @JsonProperty("alternate_mobile") String alternateMobile,
            int priority,
            boolean active) {}

    public record Household(
            User resident,
            User user,
            List<Space> spaces,
            List<Space> units,
            List<Space> flats,
            List<Member> household,
            List<Vehicle> vehicles,
            @JsonProperty("emergency_contacts") List<Contact> emergencyContacts) {}

    public record Workspace(
            List<Household> households,
            @JsonProperty("resident_count") int residentCount,
            @JsonProperty("member_count") int memberCount,
            @JsonProperty("vehicle_count") int vehicleCount,
            @JsonProperty("pending_verification") long pendingVerification) {}

    public record EntityResult(UUID id, boolean active) {}
}
