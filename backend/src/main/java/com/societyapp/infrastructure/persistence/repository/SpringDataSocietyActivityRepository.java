package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.SocietyActivity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSocietyActivityRepository
        extends JpaRepository<SocietyActivity, UUID>,
                com.societyapp.application.port.out.persistence.SocietyActivityRepository {
    List<SocietyActivity> findBySocietyIdOrderByStartsAtDesc(UUID societyId);

    Optional<SocietyActivity> findByIdAndSocietyId(UUID id, UUID societyId);

    long countBySocietyId(UUID societyId);

    long countBySocietyIdAndStatus(UUID societyId, String status);
}
