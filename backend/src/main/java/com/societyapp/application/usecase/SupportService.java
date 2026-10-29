package com.societyapp.application.usecase;

import com.societyapp.application.exception.ResourceNotFoundException;
import com.societyapp.application.model.support.*;
import com.societyapp.application.port.in.SupportUseCase;
import com.societyapp.application.port.out.persistence.*;
import com.societyapp.domain.model.*;
import com.societyapp.domain.type.MessageType;
import com.societyapp.domain.type.SupportPriority;
import com.societyapp.domain.type.SupportTicketStatus;
import com.societyapp.domain.type.UserRole;
import java.time.OffsetDateTime;
import java.util.*;

public class SupportService implements SupportUseCase {
    private final SupportTicketRepository tickets;
    private final SupportTicketMessageRepository messages;
    private final UserAccountJpaRepository users;

    public SupportService(
            SupportTicketRepository tickets,
            SupportTicketMessageRepository messages,
            UserAccountJpaRepository users) {
        this.tickets = tickets;
        this.messages = messages;
        this.users = users;
    }

    public List<SupportTicketView> tickets(UUID societyId) {
        if (societyId == null) throw new IllegalArgumentException("A society account is required");
        return tickets.findBySocietyIdOrderByCreatedAtDesc(societyId).stream()
                .map(this::ticketView)
                .toList();
    }

    public SupportTicketCreated create(
            UUID societyId,
            UUID userId,
            String subject,
            String description,
            SupportPriority priority) {
        if (societyId == null) throw new IllegalArgumentException("A society account is required");
        var now = OffsetDateTime.now();
        var ticket = new SupportTicket();
        ticket.setSocietyId(societyId);
        ticket.setCreatedBy(userId);
        ticket.setSubject(subject);
        ticket.setDescription(description);
        ticket.setPriority(priority.code());
        ticket.setStatus(SupportTicketStatus.OPEN.code());
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        ticket = tickets.save(ticket);
        saveMessage(ticket.getId(), userId, description);
        return new SupportTicketCreated(ticket.getId(), SupportTicketStatus.OPEN);
    }

    public List<SupportMessageView> messages(
            UUID ticketId, UUID societyId, UUID userId, UserRole role) {
        authorize(ticketId, societyId, role);
        var result = messages.findByTicketIdOrderByCreatedAt(ticketId);
        result.stream()
                .filter(message -> !message.getSenderId().equals(userId))
                .filter(message -> message.getReadAt() == null)
                .forEach(message -> message.setReadAt(OffsetDateTime.now()));
        messages.saveAll(result);
        return result.stream().map(this::messageView).toList();
    }

    public SupportMessageSent send(
            UUID ticketId, UUID societyId, UUID userId, UserRole role, String text) {
        var ticket = authorize(ticketId, societyId, role);
        var message = saveMessage(ticketId, userId, text);
        if (SupportTicketStatus.OPEN.matches(ticket.getStatus())) {
            ticket.setStatus(SupportTicketStatus.IN_PROGRESS.code());
        }
        if (role == UserRole.SUPER_ADMIN) ticket.setAssignedTo(userId);
        ticket.setUpdatedAt(OffsetDateTime.now());
        tickets.save(ticket);
        return new SupportMessageSent(message.getId(), true);
    }

    private SupportTicket authorize(UUID ticketId, UUID societyId, UserRole role) {
        return tickets.findById(ticketId)
                .filter(
                        ticket ->
                                role == UserRole.SUPER_ADMIN
                                        || Objects.equals(ticket.getSocietyId(), societyId))
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found"));
    }

    private SupportTicketMessage saveMessage(UUID ticketId, UUID senderId, String text) {
        var message = new SupportTicketMessage();
        message.setTicketId(ticketId);
        message.setSenderId(senderId);
        message.setMessage(text);
        message.setMessageType(MessageType.TEXT.code());
        message.setCreatedAt(OffsetDateTime.now());
        return messages.save(message);
    }

    private SupportTicketView ticketView(SupportTicket ticket) {
        return new SupportTicketView(
                ticket.getId(),
                ticket.getSocietyId(),
                ticket.getCreatedBy(),
                ticket.getSubject(),
                ticket.getDescription(),
                SupportPriority.from(ticket.getPriority()),
                SupportTicketStatus.from(ticket.getStatus()),
                ticket.getAssignedTo(),
                ticket.getResolution(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getClosedAt(),
                ticket.getCreatedBy() == null
                        ? null
                        : users.findById(ticket.getCreatedBy())
                                .map(user -> user.getFullName())
                                .orElse(null),
                messages.findByTicketIdOrderByCreatedAt(ticket.getId()).size());
    }

    private SupportMessageView messageView(SupportTicketMessage message) {
        var sender = users.findById(message.getSenderId()).orElse(null);
        return new SupportMessageView(
                message.getId(),
                message.getMessage(),
                MessageType.from(message.getMessageType()),
                message.getAttachmentKey(),
                message.getCreatedAt(),
                message.getReadAt(),
                message.getSenderId(),
                sender == null ? null : sender.getFullName(),
                sender == null ? null : UserRole.from(sender.getRole()));
    }
}
