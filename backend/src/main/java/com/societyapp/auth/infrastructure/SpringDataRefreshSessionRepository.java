package com.societyapp.auth.infrastructure;

import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
interface SpringDataRefreshSessionRepository extends JpaRepository<RefreshSessionJpaEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select session from RefreshSessionJpaEntity session where session.tokenHash = :hash")
    Optional<RefreshSessionJpaEntity> findByTokenHashForUpdate(@Param("hash") String hash);

    @Modifying
    @Query(
            """
            update RefreshSessionJpaEntity session
            set session.revokedAt = :now
            where session.familyId = :familyId and session.revokedAt is null
            """)
    void revokeFamily(@Param("familyId") UUID familyId, @Param("now") OffsetDateTime now);

    @Modifying
    @Query(
            """
            update RefreshSessionJpaEntity session
            set session.revokedAt = :now
            where session.userId = :userId and session.revokedAt is null
            """)
    void revokeAllForUser(@Param("userId") UUID userId, @Param("now") OffsetDateTime now);
}
