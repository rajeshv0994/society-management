package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.DeliveryEvent;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataDeliveryEventRepository
        extends JpaRepository<DeliveryEvent, UUID>,
                com.societyapp.application.port.out.persistence.DeliveryEventRepository {
    List<DeliveryEvent> findByDeliveryIdOrderByOccurredAt(UUID deliveryId);
}
