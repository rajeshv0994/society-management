package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.SocietyActivity;
import java.util.*;

public interface SocietyActivityRepository extends CrudRepositoryPort<SocietyActivity, UUID> {
    List<SocietyActivity> findBySocietyIdOrderByStartsAtDesc(UUID societyId);

    Optional<SocietyActivity> findByIdAndSocietyId(UUID id, UUID societyId);

    long countBySocietyId(UUID societyId);

    long countBySocietyIdAndStatus(UUID societyId, String status);
}
