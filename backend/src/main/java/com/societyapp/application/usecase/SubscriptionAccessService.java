package com.societyapp.application.usecase;

import com.societyapp.application.port.in.SubscriptionAccessUseCase;
import com.societyapp.application.port.out.persistence.SocietyRepository;
import com.societyapp.application.port.out.persistence.SubscriptionOrderRepository;
import com.societyapp.domain.type.LifecycleStatus;
import com.societyapp.domain.type.PaymentStatus;
import com.societyapp.domain.type.SubscriptionStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public class SubscriptionAccessService implements SubscriptionAccessUseCase {
    private static final Set<String> UNPAID =
            Set.of(
                    PaymentStatus.DUE.code(),
                    PaymentStatus.PAYMENT_PENDING.code(),
                    PaymentStatus.REJECTED.code());
    private static final Set<String> CLOSED =
            Set.of(SubscriptionStatus.SUSPENDED.code(), SubscriptionStatus.CANCELLED.code());
    private final SocietyRepository societies;
    private final SubscriptionOrderRepository orders;

    public SubscriptionAccessService(
            SocietyRepository societies, SubscriptionOrderRepository orders) {
        this.societies = societies;
        this.orders = orders;
    }

    public Decision evaluate(UUID societyId) {
        var society = societies.findById(societyId).orElse(null);
        if (society == null) return Decision.deny("Society account was not found");
        if (!LifecycleStatus.ACTIVE.matches(society.getStatus())) {
            return Decision.deny("Society account is not active");
        }
        if (CLOSED.contains(society.getSubscriptionStatus())) {
            return Decision.deny("Platform membership is suspended");
        }
        if (Set.of(
                        SubscriptionStatus.PENDING.code(),
                        SubscriptionStatus.PENDING_PAYMENT.code(),
                        SubscriptionStatus.EXPIRED.code())
                .contains(society.getSubscriptionStatus())) {
            return Decision.deny("Platform membership payment is required");
        }
        if (society.getSubscriptionExpiresAt() != null
                && !society.getSubscriptionExpiresAt().isAfter(OffsetDateTime.now())) {
            return Decision.deny(
                    "Platform membership has expired. Pay the outstanding invoice to continue");
        }
        if (orders.existsBySocietyIdAndDueDateLessThanEqualAndStatusIn(
                societyId, LocalDate.now(), UNPAID)) {
            return Decision.deny(
                    "Platform membership invoice is due or overdue. Payment is required to continue");
        }
        return Decision.permit();
    }

    public boolean recoveryLoginAllowed(UUID societyId) {
        return societies
                .findById(societyId)
                .filter(society -> LifecycleStatus.ACTIVE.matches(society.getStatus()))
                .filter(society -> !CLOSED.contains(society.getSubscriptionStatus()))
                .isPresent();
    }
}
