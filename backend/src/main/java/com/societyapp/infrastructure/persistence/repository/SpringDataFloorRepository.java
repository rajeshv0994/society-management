package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.Floor;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataFloorRepository
        extends JpaRepository<Floor, UUID>,
                com.societyapp.application.port.out.persistence.FloorRepository {
    List<Floor> findByBlockIdOrderByFloorNumber(UUID blockId);
}
