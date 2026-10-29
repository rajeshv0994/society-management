package com.societyapp.auth.application.port.out;

import com.societyapp.auth.domain.UserAccount;
import java.util.Optional;

public interface SocietyAccessPolicy {
    Optional<String> denialReason(UserAccount account);
}
