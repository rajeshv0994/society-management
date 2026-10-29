package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.AmenityBooking;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAmenityBookingRepository
        extends JpaRepository<AmenityBooking, UUID>,
                com.societyapp.application.port.out.persistence.AmenityBookingRepository {
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
