package com.societyapp.auth.application.port.in;

import java.util.UUID;

public interface UserLogin {
    Result authenticate(Command command);

    record Command(String email, String password, String societyCode) {}

    record Result(String token, UUID userId, String fullName, String role, UUID societyId) {}
}
