package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.Society;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSocietyRepository
        extends JpaRepository<Society, UUID>,
                com.societyapp.application.port.out.persistence.SocietyRepository {
    Optional<Society> findByRegistrationNumberIgnoreCase(String registrationNumber);

    boolean existsByRegistrationNumberIgnoreCase(String registrationNumber);

    Optional<Society> findBySocietyCodeIgnoreCase(String societyCode);

    boolean existsBySocietyCodeIgnoreCase(String societyCode);

    List<Society> findByStatusOrderByCreatedAtDesc(String status);
}
