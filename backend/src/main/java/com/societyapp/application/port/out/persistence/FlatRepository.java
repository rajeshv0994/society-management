package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.Flat;
import java.util.List;
import java.util.UUID;

public interface FlatRepository extends CrudRepositoryPort<Flat, UUID> {
    List<Flat> findByFloorIdOrderByFlatNumber(UUID floorId);
}
