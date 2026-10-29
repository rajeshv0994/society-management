package com.societyapp.application.model.resident;

import com.societyapp.domain.type.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class ResidentModels {
    private ResidentModels() {}

    public record RegistrationCommand(
            UUID societyId,
            UUID spaceId,
            ResidentType residentType,
            String fullName,
            String mobile,
            String email,
            LocalDate dateOfBirth,
            String gender,
            String emergencyContact,
            LocalDate moveInDate,
            LocalDate moveOutDate) {}

    public record AdminCreateCommand(
            RegistrationCommand registration, UUID reviewerId, String temporaryPassword) {}

    public record ReviewCommand(
            UUID reviewerId, ApprovalAction decision, String reason, String temporaryPassword) {}

    public record HouseholdCommand(
            String fullName, String relationship, String mobile, LocalDate dateOfBirth) {}

    public record RegistrationResult(UUID registrationId, RegistrationStatus status) {}

    public record ReviewResult(RegistrationStatus status, UUID userId) {}

    public record CreatedResident(UUID registrationId, RegistrationStatus status, UUID userId) {}

    public record EntityResult(UUID id, Boolean active) {}

    public record SpaceOption(
            UUID id,
            String block,
            String section,
            String spaceNumber,
            FlatOccupancyStatus occupancyStatus) {}

    public record RegistrationView(
            UUID id,
            UUID societyId,
            UUID spaceId,
            ResidentType residentType,
            String fullName,
            String mobile,
            String email,
            LocalDate dateOfBirth,
            String gender,
            String emergencyContact,
            LocalDate moveInDate,
            LocalDate moveOutDate,
            RegistrationStatus status,
            String rejectionReason,
            boolean invited,
            OffsetDateTime invitationExpiresAt,
            OffsetDateTime submittedAt,
            UUID reviewedBy,
            OffsetDateTime reviewedAt,
            String spaceNumber,
            String block) {}

    public record Summary(
            UUID id,
            String fullName,
            String email,
            String mobile,
            AccountStatus status,
            String spaceNumber,
            ResidentType residentType,
            String block) {}

    public record Space(
            UUID id,
            String spaceNumber,
            FlatOccupancyStatus occupancyStatus,
            BigDecimal areaSqft,
            String section,
            String block) {}

    public record Household(
            UUID id, String fullName, String relationship, String mobile, LocalDate dateOfBirth) {}

    public record Complaint(
            UUID id,
            String title,
            String description,
            WorkPriority priority,
            ComplaintStatus status,
            String resolution,
            OffsetDateTime createdAt) {}

    public record Invoice(
            UUID id,
            String invoiceNumber,
            LocalDate billingMonth,
            LocalDate dueDate,
            BigDecimal subtotal,
            BigDecimal lateFee,
            BigDecimal paidAmount,
            PaymentStatus status) {}

    public record Payment(
            UUID id,
            UUID invoiceId,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            String providerReference,
            PaymentStatus status,
            OffsetDateTime paidAt) {}

    public record Visitor(
            UUID id,
            String visitorName,
            String mobile,
            String purpose,
            String vehicleNumber,
            OffsetDateTime expectedAt,
            VisitorStatus status) {}

    public record Booking(
            UUID id,
            UUID amenityId,
            String amenityName,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            BigDecimal amount,
            AmenityBookingStatus status) {}

    public record Detail(
            Summary resident,
            RegistrationView registration,
            Space space,
            List<Household> household,
            List<Complaint> complaints,
            List<Invoice> invoices,
            List<Payment> payments,
            List<Visitor> visitors,
            List<Booking> amenityBookings) {}
}
