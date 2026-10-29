package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.ResidentRegistration;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataResidentRegistrationRepository
        extends JpaRepository<ResidentRegistration, UUID>,
                com.societyapp.application.port.out.persistence.ResidentRegistrationRepository {
    List<ResidentRegistration> findBySocietyIdOrderBySubmittedAtDesc(UUID societyId);

    List<ResidentRegistration> findBySocietyIdAndStatusOrderBySubmittedAtDesc(
            UUID societyId, String status);

    Optional<ResidentRegistration> findByIdAndSocietyId(UUID id, UUID societyId);
}
