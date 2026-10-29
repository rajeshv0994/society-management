package com.societyapp.controller.mapper;

import com.societyapp.application.model.resident.ResidentModels;
import com.societyapp.controller.model.ResidentResponse;

public final class ResidentHttpMapper {
    public ResidentResponse.RegistrationResult toResponse(ResidentModels.RegistrationResult value) {
        return new ResidentResponse.RegistrationResult(
                value.registrationId(), value.status().code());
    }

    public ResidentResponse.ReviewResult toResponse(ResidentModels.ReviewResult value) {
        return new ResidentResponse.ReviewResult(value.status().code(), value.userId(), null);
    }

    public ResidentResponse.ReviewResult toResponse(ResidentModels.CreatedResident value) {
        return new ResidentResponse.ReviewResult(
                value.status().code(), value.userId(), value.registrationId());
    }

    public ResidentResponse.EntityResult toResponse(ResidentModels.EntityResult value) {
        return new ResidentResponse.EntityResult(value.id(), value.active());
    }

    public ResidentResponse.SpaceOption toResponse(ResidentModels.SpaceOption value) {
        return new ResidentResponse.SpaceOption(
                value.id(),
                value.block(),
                value.section(),
                value.section(),
                value.spaceNumber(),
                value.spaceNumber(),
                value.spaceNumber(),
                value.occupancyStatus().code());
    }

    public ResidentResponse.Registration toResponse(ResidentModels.RegistrationView value) {
        return new ResidentResponse.Registration(
                value.id(),
                value.societyId(),
                value.spaceId(),
                value.residentType().code(),
                value.fullName(),
                value.mobile(),
                value.email(),
                value.dateOfBirth(),
                value.gender(),
                value.emergencyContact(),
                value.moveInDate(),
                value.moveOutDate(),
                value.status().code(),
                value.rejectionReason(),
                value.invited(),
                value.invitationExpiresAt(),
                value.submittedAt(),
                value.reviewedBy(),
                value.reviewedAt(),
                value.spaceNumber(),
                value.spaceNumber(),
                value.spaceNumber(),
                value.block());
    }

    public ResidentResponse.Summary toResponse(ResidentModels.Summary value) {
        return new ResidentResponse.Summary(
                value.id(),
                value.fullName(),
                value.email(),
                value.mobile(),
                value.status().code(),
                value.spaceNumber(),
                value.spaceNumber(),
                value.spaceNumber(),
                value.residentType() == null ? null : value.residentType().code(),
                value.block());
    }

    public ResidentResponse.Detail toResponse(ResidentModels.Detail value) {
        return new ResidentResponse.Detail(
                toResponse(value.resident()),
                value.registration() == null ? null : toResponse(value.registration()),
                space(value.space()),
                value.household().stream().map(this::household).toList(),
                value.complaints().stream().map(this::complaint).toList(),
                value.invoices().stream().map(this::invoice).toList(),
                value.payments().stream().map(this::payment).toList(),
                value.visitors().stream().map(this::visitor).toList(),
                value.amenityBookings().stream().map(this::booking).toList());
    }

    private ResidentResponse.Space space(ResidentModels.Space value) {
        return new ResidentResponse.Space(
                value.id(),
                value.spaceNumber(),
                value.occupancyStatus().code(),
                value.areaSqft(),
                value.section(),
                value.block());
    }

    private ResidentResponse.Household household(ResidentModels.Household value) {
        return new ResidentResponse.Household(
                value.id(),
                value.fullName(),
                value.relationship(),
                value.mobile(),
                value.dateOfBirth());
    }

    private ResidentResponse.Complaint complaint(ResidentModels.Complaint value) {
        return new ResidentResponse.Complaint(
                value.id(),
                value.title(),
                value.description(),
                value.priority().code(),
                value.status().code(),
                value.resolution(),
                value.createdAt());
    }

    private ResidentResponse.Invoice invoice(ResidentModels.Invoice value) {
        return new ResidentResponse.Invoice(
                value.id(),
                value.invoiceNumber(),
                value.billingMonth(),
                value.dueDate(),
                value.subtotal(),
                value.lateFee(),
                value.paidAmount(),
                value.status().code());
    }

    private ResidentResponse.Payment payment(ResidentModels.Payment value) {
        return new ResidentResponse.Payment(
                value.id(),
                value.invoiceId(),
                value.amount(),
                value.paymentMethod().code(),
                value.providerReference(),
                value.status().code(),
                value.paidAt());
    }

    private ResidentResponse.Visitor visitor(ResidentModels.Visitor value) {
        return new ResidentResponse.Visitor(
                value.id(),
                value.visitorName(),
                value.mobile(),
                value.purpose(),
                value.vehicleNumber(),
                value.expectedAt(),
                value.status().code());
    }

    private ResidentResponse.Booking booking(ResidentModels.Booking value) {
        return new ResidentResponse.Booking(
                value.id(),
                value.amenityId(),
                value.amenityName(),
                value.startsAt(),
                value.endsAt(),
                value.amount(),
                value.status().code());
    }
}
