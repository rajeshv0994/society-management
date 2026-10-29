package com.societyapp.controller.mapper;

import com.societyapp.application.model.activity.ActivityModels;
import com.societyapp.controller.model.ActivityResponse;

public final class ActivityHttpMapper {
    public ActivityResponse.Workspace toResponse(ActivityModels.Workspace value) {
        return new ActivityResponse.Workspace(
                value.activities().stream().map(this::toResponse).toList(),
                value.participations().stream().map(this::toResponse).toList(),
                value.photos().stream().map(this::toResponse).toList(),
                value.totalActivities(),
                value.upcoming(),
                value.myRegistrations(),
                value.pendingPayments(),
                value.pendingApprovals(),
                value.pendingPhotos());
    }

    public ActivityResponse.Activity toResponse(ActivityModels.ActivityView value) {
        return new ActivityResponse.Activity(
                value.id(),
                value.activityCode(),
                value.title(),
                value.category().code(),
                value.description(),
                value.venue(),
                value.startsAt(),
                value.endsAt(),
                value.registrationOpensAt(),
                value.registrationClosesAt(),
                value.capacity(),
                value.registeredCount(),
                value.availablePlaces(),
                value.feePerPerson(),
                value.paid(),
                value.approvalRequired(),
                value.cancellationHours(),
                value.audience().code(),
                value.organizerName(),
                value.organizerMobile(),
                value.instructions(),
                value.status().code(),
                value.cancellationReason(),
                value.createdAt(),
                value.myParticipation() == null ? null : toResponse(value.myParticipation()));
    }

    public ActivityResponse.Participation toResponse(ActivityModels.ParticipationView value) {
        return new ActivityResponse.Participation(
                value.id(),
                value.activityId(),
                value.activityTitle(),
                value.activityCode(),
                value.startsAt(),
                value.participantId(),
                value.participantName(),
                value.participantEmail(),
                value.participantCount(),
                value.participantNames(),
                value.contactMobile(),
                value.notes(),
                value.amount(),
                value.paymentStatus().code(),
                paymentMethod(value),
                value.paymentReference(),
                value.status().code(),
                value.rejectionReason(),
                value.checkedInAt(),
                value.attendedAt(),
                value.createdAt());
    }

    public ActivityResponse.Photo toResponse(ActivityModels.PhotoView value) {
        return new ActivityResponse.Photo(
                value.id(),
                value.activityId(),
                value.activityTitle(),
                value.submittedBy(),
                value.submittedByName(),
                value.caption(),
                value.moderationStatus().code(),
                value.featured(),
                value.rejectionReason(),
                value.createdAt(),
                value.photoUrl());
    }

    public ActivityResponse.Event toResponse(ActivityModels.EventView value) {
        return new ActivityResponse.Event(
                value.id(),
                value.eventType(),
                value.note(),
                value.actorRole() == null ? null : value.actorRole().code(),
                value.occurredAt(),
                value.actorName());
    }

    private String paymentMethod(ActivityModels.ParticipationView value) {
        if (value.paymentMethod() == null) return null;
        return value.paymentReference() != null
                        && value.paymentReference().startsWith("pay_activity_test_")
                ? value.paymentMethod().razorpayCode()
                : value.paymentMethod().code();
    }
}
