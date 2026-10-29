package com.societyapp.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.societyapp.application.port.out.persistence.SocietyRepository;
import com.societyapp.application.port.out.persistence.SubscriptionOrderRepository;
import com.societyapp.application.usecase.SubscriptionAccessService;
import com.societyapp.domain.model.Society;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SubscriptionAccessServiceTest {
    private final UUID societyId = UUID.randomUUID();
    private SocietyRepository societies;
    private SubscriptionOrderRepository orders;
    private SubscriptionAccessService service;
    private Society society;

    @BeforeEach
    void setUp() {
        societies = mock(SocietyRepository.class);
        orders = mock(SubscriptionOrderRepository.class);
        service = new SubscriptionAccessService(societies, orders);
        society = new Society();
        ReflectionTestUtils.setField(society, "id", societyId);
        society.setStatus("ACTIVE");
        society.setSubscriptionStatus("ACTIVE");
        when(societies.findById(societyId)).thenReturn(Optional.of(society));
    }

    @Test
    void activeTrialBeforeExpiryCanUseServices() {
        society.setSubscriptionStatus("TRIAL");
        society.setSubscriptionExpiresAt(OffsetDateTime.now().plusDays(1));

        assertThat(service.evaluate(societyId).allowed()).isTrue();
    }

    @Test
    void expiredTrialCannotUseServices() {
        society.setSubscriptionStatus("TRIAL");
        society.setSubscriptionExpiresAt(OffsetDateTime.now().minusSeconds(1));

        assertThat(service.evaluate(societyId).allowed()).isFalse();
        assertThat(service.evaluate(societyId).reason()).contains("expired");
    }

    @Test
    void dueOrOverdueUnpaidInvoiceCannotUseServices() {
        when(orders.existsBySocietyIdAndDueDateLessThanEqualAndStatusIn(
                        eq(societyId), any(), anyCollection()))
                .thenReturn(true);

        assertThat(service.evaluate(societyId).allowed()).isFalse();
        assertThat(service.evaluate(societyId).reason()).contains("due or overdue");
    }

    @Test
    void pendingInitialPaymentCannotUseServices() {
        society.setSubscriptionStatus("PENDING_PAYMENT");

        assertThat(service.evaluate(societyId).allowed()).isFalse();
        assertThat(service.recoveryLoginAllowed(society.getId())).isTrue();
    }
}
