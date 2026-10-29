package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.SupportTicket;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSupportTicketRepository
        extends JpaRepository<SupportTicket, UUID>,
                com.societyapp.application.port.out.persistence.SupportTicketRepository {
    List<SupportTicket> findAllByOrderByCreatedAtDesc();

    List<SupportTicket> findBySocietyIdOrderByCreatedAtDesc(UUID societyId);

    Optional<SupportTicket> findByIdAndSocietyId(UUID id, UUID societyId);
}
