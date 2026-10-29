package com.societyapp.auth.application.port.in;

import java.util.UUID;

public interface UserSessions {
    Session start(UserLogin.Result login);

    Session refresh(String refreshToken);

    void logout(String refreshToken);

    void logoutAll(UUID userId);

    Profile current(UUID userId);

    record Session(
            String accessToken,
            String refreshToken,
            long expiresIn,
            UUID userId,
            String fullName,
            String role,
            UUID societyId) {}

    record Profile(UUID userId, String fullName, String email, String role, UUID societyId) {}
}
