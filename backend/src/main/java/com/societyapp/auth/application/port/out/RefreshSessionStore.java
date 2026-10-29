package com.societyapp.auth.application.port.out;

import com.societyapp.auth.domain.RefreshSession;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshSessionStore {
    void create(RefreshSession session);

    Optional<RefreshSession> findForUpdate(String tokenHash);

    void replace(UUID currentId, RefreshSession replacement, OffsetDateTime usedAt);

    void revoke(UUID sessionId, OffsetDateTime revokedAt);

    void revokeFamily(UUID familyId, OffsetDateTime revokedAt);

    void revokeAllForUser(UUID userId, OffsetDateTime revokedAt);
}
