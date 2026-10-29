package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.Amenity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAmenityRepository
        extends JpaRepository<Amenity, UUID>,
                com.societyapp.application.port.out.persistence.AmenityRepository {
    List<Amenity> findBySocietyIdOrderByName(UUID societyId);

    Optional<Amenity> findByIdAndSocietyId(UUID id, UUID societyId);
}
