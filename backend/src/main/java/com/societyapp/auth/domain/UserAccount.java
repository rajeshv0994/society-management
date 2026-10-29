package com.societyapp.auth.domain;

import java.util.UUID;

public record UserAccount(
        UUID id,
        String fullName,
        String email,
        String passwordHash,
        String role,
        UUID societyId,
        String societyCode,
        Status status) {

    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    public enum Status {
        ACTIVE,
        INACTIVE,
        SUSPENDED
    }
}
