package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.Block;
import java.util.List;
import java.util.UUID;

public interface BlockRepository extends CrudRepositoryPort<Block, UUID> {
    List<Block> findBySocietyIdOrderByName(UUID societyId);

    boolean existsByIdAndSocietyId(UUID id, UUID societyId);
}
