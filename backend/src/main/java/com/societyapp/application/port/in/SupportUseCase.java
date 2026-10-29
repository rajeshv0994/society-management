package com.societyapp.application.port.in;

import com.societyapp.application.model.support.*;
import com.societyapp.domain.type.SupportPriority;
import com.societyapp.domain.type.UserRole;
import java.util.List;
import java.util.UUID;

public interface SupportUseCase {
    List<SupportTicketView> tickets(UUID societyId);

    SupportTicketCreated create(
            UUID societyId,
            UUID userId,
            String subject,
            String description,
            SupportPriority priority);

    List<SupportMessageView> messages(UUID ticketId, UUID societyId, UUID userId, UserRole role);

    SupportMessageSent send(UUID ticketId, UUID societyId, UUID userId, UserRole role, String text);
}
