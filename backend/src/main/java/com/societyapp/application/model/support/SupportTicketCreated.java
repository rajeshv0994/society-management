package com.societyapp.application.model.support;

import com.societyapp.domain.type.SupportTicketStatus;
import java.util.UUID;

public record SupportTicketCreated(UUID ticketId, SupportTicketStatus status) {}
