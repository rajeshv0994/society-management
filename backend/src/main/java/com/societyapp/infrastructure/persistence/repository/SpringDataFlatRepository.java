package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.Flat;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataFlatRepository
        extends JpaRepository<Flat, UUID>,
                com.societyapp.application.port.out.persistence.FlatRepository {
    List<Flat> findByFloorIdOrderByFlatNumber(UUID floorId);
}
