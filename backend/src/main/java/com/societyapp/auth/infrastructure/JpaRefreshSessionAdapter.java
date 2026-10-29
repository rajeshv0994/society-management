package com.societyapp.auth.infrastructure;

import com.societyapp.auth.application.port.out.RefreshSessionStore;
import com.societyapp.auth.domain.RefreshSession;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaRefreshSessionAdapter implements RefreshSessionStore {
    private final SpringDataRefreshSessionRepository repository;

    public JpaRefreshSessionAdapter(SpringDataRefreshSessionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void create(RefreshSession session) {
        repository.save(toEntity(session));
    }

    @Override
    public Optional<RefreshSession> findForUpdate(String tokenHash) {
        return repository.findByTokenHashForUpdate(tokenHash).map(this::toDomain);
    }

    @Override
    public void replace(UUID currentId, RefreshSession replacement, OffsetDateTime usedAt) {
        repository.save(toEntity(replacement));
        var current =
                repository
                        .findById(currentId)
                        .orElseThrow(() -> new IllegalStateException("Refresh session not found"));
        current.setLastUsedAt(usedAt);
        current.setRevokedAt(usedAt);
        current.setReplacedById(replacement.id());
        repository.save(current);
    }

    @Override
    public void revoke(UUID sessionId, OffsetDateTime revokedAt) {
        repository
                .findById(sessionId)
                .ifPresent(
                        session -> {
                            if (session.getRevokedAt() == null) {
                                session.setRevokedAt(revokedAt);
                                repository.save(session);
                            }
                        });
    }

    @Override
    public void revokeFamily(UUID familyId, OffsetDateTime revokedAt) {
        repository.revokeFamily(familyId, revokedAt);
    }

    @Override
    public void revokeAllForUser(UUID userId, OffsetDateTime revokedAt) {
        repository.revokeAllForUser(userId, revokedAt);
    }

    private RefreshSessionJpaEntity toEntity(RefreshSession session) {
        var entity = new RefreshSessionJpaEntity();
        entity.setId(session.id());
        entity.setUserId(session.userId());
        entity.setFamilyId(session.familyId());
        entity.setTokenHash(session.tokenHash());
        entity.setExpiresAt(session.expiresAt());
        entity.setCreatedAt(session.createdAt());
        entity.setRevokedAt(session.revokedAt());
        entity.setReplacedById(session.replacedById());
        return entity;
    }

    private RefreshSession toDomain(RefreshSessionJpaEntity entity) {
        return new RefreshSession(
                entity.getId(),
                entity.getUserId(),
                entity.getFamilyId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getRevokedAt(),
                entity.getReplacedById());
    }
}
