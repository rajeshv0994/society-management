package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.SupportTicketMessage;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSupportTicketMessageRepository
        extends JpaRepository<SupportTicketMessage, UUID>,
                com.societyapp.application.port.out.persistence.SupportTicketMessageRepository {
    List<SupportTicketMessage> findByTicketIdOrderByCreatedAt(UUID ticketId);
}
