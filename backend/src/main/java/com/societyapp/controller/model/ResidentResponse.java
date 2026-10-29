package com.societyapp.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class ResidentResponse {
    private ResidentResponse() {}

    public record RegistrationResult(UUID registrationId, String status) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ReviewResult(String status, UUID userId, UUID registrationId) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record EntityResult(UUID id, Boolean active) {}

    public record SpaceOption(
            UUID id,
            String block,
            String section,
            String floor,
            @JsonProperty("space_number") String spaceNumber,
            @JsonProperty("unit_number") String unitNumber,
            @JsonProperty("flat_number") String flatNumber,
            @JsonProperty("occupancy_status") String occupancyStatus) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Registration(
            UUID id,
            @JsonProperty("society_id") UUID societyId,
            @JsonProperty("flat_id") UUID flatId,
            @JsonProperty("resident_type") String residentType,
            @JsonProperty("full_name") String fullName,
            String mobile,
            String email,
            LocalDate dob,
            String gender,
            @JsonProperty("emergency_contact") String emergencyContact,
            @JsonProperty("move_in_date") LocalDate moveInDate,
            @JsonProperty("move_out_date") LocalDate moveOutDate,
            String status,
            @JsonProperty("rejection_reason") String rejectionReason,
            boolean invited,
            @JsonProperty("invitation_expires_at") OffsetDateTime invitationExpiresAt,
            @JsonProperty("submitted_at") OffsetDateTime submittedAt,
            @JsonProperty("reviewed_by") UUID reviewedBy,
            @JsonProperty("reviewed_at") OffsetDateTime reviewedAt,
            @JsonProperty("space_number") String spaceNumber,
            @JsonProperty("unit_number") String unitNumber,
            @JsonProperty("flat_number") String flatNumber,
            String block) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Summary(
            UUID id,
            @JsonProperty("full_name") String fullName,
            String email,
            String mobile,
            String status,
            @JsonProperty("space_number") String spaceNumber,
            @JsonProperty("unit_number") String unitNumber,
            @JsonProperty("flat_number") String flatNumber,
            @JsonProperty("resident_type") String residentType,
            String block) {}

    public record Space(
            UUID id,
            @JsonProperty("space_number") String spaceNumber,
            @JsonProperty("occupancy_status") String occupancyStatus,
            @JsonProperty("area_sqft") BigDecimal areaSqft,
            String section,
            String block) {}

    public record Household(
            UUID id,
            @JsonProperty("full_name") String fullName,
            String relationship,
            String mobile,
            @JsonProperty("date_of_birth") LocalDate dateOfBirth) {}

    public record Complaint(
            UUID id,
            String title,
            String description,
            String priority,
            String status,
            String resolution,
            @JsonProperty("created_at") OffsetDateTime createdAt) {}

    public record Invoice(
            UUID id,
            @JsonProperty("invoice_number") String invoiceNumber,
            @JsonProperty("billing_month") LocalDate billingMonth,
            @JsonProperty("due_date") LocalDate dueDate,
            BigDecimal subtotal,
            @JsonProperty("late_fee") BigDecimal lateFee,
            @JsonProperty("paid_amount") BigDecimal paidAmount,
            String status) {}

    public record Payment(
            UUID id,
            @JsonProperty("invoice_id") UUID invoiceId,
            BigDecimal amount,
            @JsonProperty("payment_method") String paymentMethod,
            @JsonProperty("provider_reference") String providerReference,
            String status,
            @JsonProperty("paid_at") OffsetDateTime paidAt) {}

    public record Visitor(
            UUID id,
            @JsonProperty("visitor_name") String visitorName,
            String mobile,
            String purpose,
            @JsonProperty("vehicle_number") String vehicleNumber,
            @JsonProperty("expected_at") OffsetDateTime expectedAt,
            String status) {}

    public record Booking(
            UUID id,
            @JsonProperty("amenity_id") UUID amenityId,
            @JsonProperty("amenity_name") String amenityName,
            @JsonProperty("starts_at") OffsetDateTime startsAt,
            @JsonProperty("ends_at") OffsetDateTime endsAt,
            BigDecimal amount,
            String status) {}

    public record Detail(
            Summary resident,
            Registration registration,
            Space space,
            List<Household> household,
            List<Complaint> complaints,
            List<Invoice> invoices,
            List<Payment> payments,
            List<Visitor> visitors,
            List<Booking> amenityBookings) {}
}
