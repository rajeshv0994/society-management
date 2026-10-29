package com.societyapp.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.societyapp.application.port.in.SubscriptionAccessUseCase;
import com.societyapp.application.port.out.persistence.SocietyRepository;
import com.societyapp.auth.domain.UserAccount;
import com.societyapp.domain.model.Society;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SubscriptionSocietyAccessAdapterTest {
    private final UUID societyId = UUID.randomUUID();
    private final SocietyRepository societies = mock(SocietyRepository.class);
    private final SubscriptionAccessUseCase subscriptions = mock(SubscriptionAccessUseCase.class);
    private final SubscriptionSocietyAccessAdapter adapter =
            new SubscriptionSocietyAccessAdapter(societies, subscriptions);
    private Society society;

    @BeforeEach
    void setUp() {
        society = new Society();
        society.setStatus("ACTIVE");
        society.setSubscriptionStatus("ACTIVE");
        when(societies.findById(societyId)).thenReturn(Optional.of(society));
    }

    @Test
    void reportsInactiveSocietyToAnOtherwiseValidAccount() {
        society.setStatus("PENDING");

        assertThat(adapter.denialReason(account("RESIDENT")))
                .contains("Society account is not active");
    }

    @Test
    void societyAdminCanLoginForPaymentRecovery() {
        society.setSubscriptionStatus("PENDING_PAYMENT");
        when(subscriptions.recoveryLoginAllowed(societyId)).thenReturn(true);

        assertThat(adapter.denialReason(account("SOCIETY_ADMIN"))).isEmpty();
    }

    @Test
    void memberReceivesMembershipDenialReason() {
        var decision =
                new SubscriptionAccessUseCase.Decision(
                        false, "Platform membership payment is required");
        when(subscriptions.evaluate(societyId)).thenReturn(decision);

        assertThat(adapter.denialReason(account("RESIDENT")))
                .contains("Platform membership payment is required");
    }

    private UserAccount account(String role) {
        return new UserAccount(
                UUID.randomUUID(),
                "Test User",
                "user@example.com",
                "encoded",
                role,
                societyId,
                "GREEN",
                UserAccount.Status.ACTIVE);
    }
}
