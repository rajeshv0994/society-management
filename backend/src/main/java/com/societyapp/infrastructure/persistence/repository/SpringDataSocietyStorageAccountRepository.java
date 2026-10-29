package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.SocietyStorageAccount;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface SpringDataSocietyStorageAccountRepository
        extends JpaRepository<SocietyStorageAccount, UUID>,
                com.societyapp.application.port.out.persistence.SocietyStorageAccountRepository {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from SocietyStorageAccount a where a.societyId=:societyId")
    Optional<SocietyStorageAccount> findForUpdate(@Param("societyId") UUID societyId);
}
