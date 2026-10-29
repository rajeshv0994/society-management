package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.RazorpayWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataRazorpayWebhookEventRepository
        extends JpaRepository<RazorpayWebhookEvent, String>,
                com.societyapp.application.port.out.persistence.RazorpayWebhookEventRepository {
    @Modifying
    @Query(
            value =
                    "INSERT INTO razorpay_webhook_events(event_id,event_type,received_at) "
                            + "VALUES (:id,:type,now()) ON CONFLICT (event_id) DO NOTHING",
            nativeQuery = true)
    int claim(@Param("id") String eventId, @Param("type") String eventType);

    @Modifying
    @Query(
            value = "UPDATE razorpay_webhook_events SET processed_at=now() WHERE event_id=:id",
            nativeQuery = true)
    void markProcessed(@Param("id") String eventId);
}
