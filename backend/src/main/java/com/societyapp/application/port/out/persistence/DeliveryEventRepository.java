package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.DeliveryEvent;
import java.util.*;

public interface DeliveryEventRepository extends CrudRepositoryPort<DeliveryEvent, UUID> {
    List<DeliveryEvent> findByDeliveryIdOrderByOccurredAt(UUID deliveryId);
}
