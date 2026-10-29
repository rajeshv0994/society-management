package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.SocietyStorageAccount;
import java.util.Optional;
import java.util.UUID;

public interface SocietyStorageAccountRepository
        extends CrudRepositoryPort<SocietyStorageAccount, UUID> {
    Optional<SocietyStorageAccount> findForUpdate(UUID societyId);
}
