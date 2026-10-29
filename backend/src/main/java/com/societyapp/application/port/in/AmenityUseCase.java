package com.societyapp.application.port.in;

import com.societyapp.application.model.amenity.AmenityModels;
import com.societyapp.domain.type.PaymentMethod;
import com.societyapp.domain.type.UserRole;
import java.util.List;
import java.util.UUID;

public interface AmenityUseCase {
    List<AmenityModels.AmenityView> list(UUID societyId, UserRole role);

    List<AmenityModels.BookingView> bookings(UUID societyId, AmenityModels.Actor actor);

    AmenityModels.AmenityView create(UUID societyId, AmenityModels.AmenityCommand command);

    AmenityModels.AmenityView update(
            UUID societyId, UUID amenityId, AmenityModels.AmenityCommand command);

    AmenityModels.BookingView book(
            UUID societyId, UUID userId, UUID amenityId, AmenityModels.BookingCommand command);

    AmenityModels.BookingView checkout(
            UUID societyId, AmenityModels.Actor actor, UUID bookingId, PaymentMethod method);

    AmenityModels.BookingView recordCash(UUID societyId, AmenityModels.Actor actor, UUID bookingId);

    AmenityModels.BookingView action(
            UUID societyId,
            AmenityModels.Actor actor,
            UUID bookingId,
            AmenityModels.ActionCommand command);
}
