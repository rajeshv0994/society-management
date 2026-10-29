package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.PlatformPaymentAttempt;
import java.util.*;

public interface PlatformPaymentAttemptRepository
        extends CrudRepositoryPort<PlatformPaymentAttempt, UUID> {
    Optional<PlatformPaymentAttempt> findByProviderOrderId(String providerOrderId);

    Optional<PlatformPaymentAttempt> findByProviderPaymentId(String providerPaymentId);

    List<PlatformPaymentAttempt> findBySubscriptionOrderIdOrderByCreatedAtDesc(UUID orderId);
}
