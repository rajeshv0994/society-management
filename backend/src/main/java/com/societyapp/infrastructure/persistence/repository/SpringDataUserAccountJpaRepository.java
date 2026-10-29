package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.UserAccount;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataUserAccountJpaRepository
        extends JpaRepository<UserAccount, UUID>,
                com.societyapp.application.port.out.persistence.UserAccountJpaRepository {
    Optional<UserAccount> findByEmailIgnoreCase(String email);

    List<UserAccount> findBySocietyIdAndRoleOrderByFullName(UUID societyId, String role);
}
