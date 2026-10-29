package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.Floor;
import java.util.List;
import java.util.UUID;

public interface FloorRepository extends CrudRepositoryPort<Floor, UUID> {
    List<Floor> findByBlockIdOrderByFloorNumber(UUID blockId);
}
