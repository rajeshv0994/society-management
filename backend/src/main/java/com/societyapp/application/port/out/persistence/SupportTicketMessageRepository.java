package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.SupportTicketMessage;
import java.util.*;

public interface SupportTicketMessageRepository
        extends CrudRepositoryPort<SupportTicketMessage, UUID> {
    List<SupportTicketMessage> findByTicketIdOrderByCreatedAt(UUID ticketId);
}
