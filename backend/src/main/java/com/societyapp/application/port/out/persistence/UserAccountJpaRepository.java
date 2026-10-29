package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.UserAccount;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAccountJpaRepository extends CrudRepositoryPort<UserAccount, UUID> {
    Optional<UserAccount> findByEmailIgnoreCase(String email);

    List<UserAccount> findBySocietyIdAndRoleOrderByFullName(UUID societyId, String role);
}
