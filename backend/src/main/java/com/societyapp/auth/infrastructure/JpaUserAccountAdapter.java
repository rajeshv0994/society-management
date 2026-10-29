package com.societyapp.auth.infrastructure;

import com.societyapp.application.port.in.SubscriptionAccessUseCase;
import com.societyapp.application.port.out.persistence.SocietyRepository;
import com.societyapp.application.port.out.persistence.UserAccountJpaRepository;
import com.societyapp.auth.application.port.out.UserAccountRepository;
import com.societyapp.auth.domain.UserAccount;
import com.societyapp.domain.model.Society;
import com.societyapp.domain.type.UserRole;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaUserAccountAdapter implements UserAccountRepository {
    private final UserAccountJpaRepository accounts;
    private final SocietyRepository societies;
    private final SubscriptionAccessUseCase subscriptions;

    public JpaUserAccountAdapter(
            UserAccountJpaRepository accounts,
            SocietyRepository societies,
            SubscriptionAccessUseCase subscriptions) {
        this.accounts = accounts;
        this.societies = societies;
        this.subscriptions = subscriptions;
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        // Login validates the password and society code before the access policy is
        // evaluated, so an inactive-society reason is never disclosed anonymously.
        return accounts.findByEmailIgnoreCase(email).map(this::toDomain);
    }

    @Override
    public Optional<UserAccount> findById(UUID id) {
        return accounts.findById(id).filter(this::hasAccess).map(this::toDomain);
    }

    private boolean hasAccess(com.societyapp.domain.model.UserAccount account) {
        if (account.getSocietyId() == null) {
            return true;
        }
        return societies
                .findById(account.getSocietyId())
                .filter(
                        society ->
                                UserRole.SOCIETY_ADMIN.matches(account.getRole())
                                        ? subscriptions.recoveryLoginAllowed(account.getSocietyId())
                                        : subscriptions.evaluate(society.getId()).allowed())
                .isPresent();
    }

    private UserAccount toDomain(com.societyapp.domain.model.UserAccount entity) {
        return new UserAccount(
                entity.getId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.getSocietyId(),
                entity.getSocietyId() == null
                        ? null
                        : societies
                                .findById(entity.getSocietyId())
                                .map(Society::getSocietyCode)
                                .orElse(null),
                UserAccount.Status.valueOf(entity.getStatus()));
    }
}
