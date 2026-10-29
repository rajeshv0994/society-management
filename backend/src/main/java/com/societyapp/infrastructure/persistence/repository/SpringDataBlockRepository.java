package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.Block;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataBlockRepository
        extends JpaRepository<Block, UUID>,
                com.societyapp.application.port.out.persistence.BlockRepository {
    List<Block> findBySocietyIdOrderByName(UUID societyId);

    boolean existsByIdAndSocietyId(UUID id, UUID societyId);
}
