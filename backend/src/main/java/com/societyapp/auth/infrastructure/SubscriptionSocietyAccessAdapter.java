package com.societyapp.auth.infrastructure;

import com.societyapp.application.port.in.SubscriptionAccessUseCase;
import com.societyapp.application.port.out.persistence.SocietyRepository;
import com.societyapp.auth.application.port.out.SocietyAccessPolicy;
import com.societyapp.auth.domain.UserAccount;
import com.societyapp.domain.type.SocietyStatus;
import com.societyapp.domain.type.UserRole;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionSocietyAccessAdapter implements SocietyAccessPolicy {
    private final SocietyRepository societies;
    private final SubscriptionAccessUseCase subscriptions;

    public SubscriptionSocietyAccessAdapter(
            SocietyRepository societies, SubscriptionAccessUseCase subscriptions) {
        this.societies = societies;
        this.subscriptions = subscriptions;
    }

    @Override
    public Optional<String> denialReason(UserAccount account) {
        if (account.societyId() == null) return Optional.empty();
        var society = societies.findById(account.societyId()).orElse(null);
        if (society == null) return Optional.of("Society account was not found");
        if (!SocietyStatus.ACTIVE.matches(society.getStatus())) {
            return Optional.of("Society account is not active");
        }
        if (UserRole.SOCIETY_ADMIN.matches(account.role())
                && subscriptions.recoveryLoginAllowed(account.societyId())) {
            return Optional.empty();
        }
        var decision = subscriptions.evaluate(account.societyId());
        return decision.allowed() ? Optional.empty() : Optional.of(decision.reason());
    }
}
