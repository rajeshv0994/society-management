package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.ResidentRegistration;
import java.util.*;

public interface ResidentRegistrationRepository
        extends CrudRepositoryPort<ResidentRegistration, UUID> {
    List<ResidentRegistration> findBySocietyIdOrderBySubmittedAtDesc(UUID societyId);

    List<ResidentRegistration> findBySocietyIdAndStatusOrderBySubmittedAtDesc(
            UUID societyId, String status);

    Optional<ResidentRegistration> findByIdAndSocietyId(UUID id, UUID societyId);
}
