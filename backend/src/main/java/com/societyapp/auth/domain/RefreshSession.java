package com.societyapp.auth.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RefreshSession(
        UUID id,
        UUID userId,
        UUID familyId,
        String tokenHash,
        OffsetDateTime expiresAt,
        OffsetDateTime createdAt,
        OffsetDateTime revokedAt,
        UUID replacedById) {

    public boolean isExpired(OffsetDateTime now) {
        return !expiresAt.isAfter(now);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }
}
