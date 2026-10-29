package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.SupportTicket;
import java.util.*;

public interface SupportTicketRepository extends CrudRepositoryPort<SupportTicket, UUID> {
    List<SupportTicket> findAllByOrderByCreatedAtDesc();

    List<SupportTicket> findBySocietyIdOrderByCreatedAtDesc(UUID societyId);

    Optional<SupportTicket> findByIdAndSocietyId(UUID id, UUID societyId);
}
