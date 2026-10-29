package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.Amenity;
import java.util.*;

public interface AmenityRepository extends CrudRepositoryPort<Amenity, UUID> {
    List<Amenity> findBySocietyIdOrderByName(UUID societyId);

    Optional<Amenity> findByIdAndSocietyId(UUID id, UUID societyId);
}
