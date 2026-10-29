package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.AmenityBooking;
import java.time.OffsetDateTime;
import java.util.*;

public interface AmenityBookingRepository extends CrudRepositoryPort<AmenityBooking, UUID> {
    List<AmenityBooking> findBySocietyIdAndBookedByOrderByStartsAtDesc(
            UUID societyId, UUID bookedBy);

    List<AmenityBooking> findBySocietyIdOrderByStartsAtDesc(UUID societyId);

    Optional<AmenityBooking> findByIdAndSocietyId(UUID id, UUID societyId);

    boolean existsByAmenityIdAndStatusAndStartsAtLessThanAndEndsAtGreaterThan(
            UUID amenityId, String status, OffsetDateTime endsAt, OffsetDateTime startsAt);

    boolean existsByAmenityIdAndStatusInAndStartsAtLessThanAndEndsAtGreaterThan(
            UUID amenityId,
            Collection<String> statuses,
            OffsetDateTime endsAt,
            OffsetDateTime startsAt);
}
