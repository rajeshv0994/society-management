package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.Society;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SocietyRepository extends CrudRepositoryPort<Society, UUID> {
    Optional<Society> findByRegistrationNumberIgnoreCase(String registrationNumber);

    boolean existsByRegistrationNumberIgnoreCase(String registrationNumber);

    Optional<Society> findBySocietyCodeIgnoreCase(String societyCode);

    boolean existsBySocietyCodeIgnoreCase(String societyCode);

    List<Society> findByStatusOrderByCreatedAtDesc(String status);
}
