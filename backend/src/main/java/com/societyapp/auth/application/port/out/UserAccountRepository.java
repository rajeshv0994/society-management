package com.societyapp.auth.application.port.out;

import com.societyapp.auth.domain.UserAccount;
import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository {
    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findById(UUID id);
}
