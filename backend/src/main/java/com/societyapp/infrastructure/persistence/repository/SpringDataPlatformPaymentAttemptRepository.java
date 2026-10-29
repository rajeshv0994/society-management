package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.PlatformPaymentAttempt;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPlatformPaymentAttemptRepository
        extends JpaRepository<PlatformPaymentAttempt, UUID>,
                com.societyapp.application.port.out.persistence.PlatformPaymentAttemptRepository {
    Optional<PlatformPaymentAttempt> findByProviderOrderId(String providerOrderId);

    Optional<PlatformPaymentAttempt> findByProviderPaymentId(String providerPaymentId);

    List<PlatformPaymentAttempt> findBySubscriptionOrderIdOrderByCreatedAtDesc(UUID orderId);
}
