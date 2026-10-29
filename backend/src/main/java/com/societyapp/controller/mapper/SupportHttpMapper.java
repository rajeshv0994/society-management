package com.societyapp.controller.mapper;

import com.societyapp.application.model.support.*;
import com.societyapp.controller.model.SupportResponse;
import org.springframework.stereotype.Component;

@Component
public class SupportHttpMapper {
    public SupportResponse.Ticket toResponse(SupportTicketView view) {
        return new SupportResponse.Ticket(
                view.id(),
                view.societyId(),
                view.createdBy(),
                view.subject(),
                view.description(),
                view.priority().code(),
                view.status().code(),
                view.assignedTo(),
                view.resolution(),
                view.createdAt(),
                view.updatedAt(),
                view.closedAt(),
                view.createdByName(),
                view.messageCount());
    }

    public SupportResponse.Message toResponse(SupportMessageView view) {
        return new SupportResponse.Message(
                view.id(),
                view.message(),
                view.messageType().code(),
                view.attachmentKey(),
                view.createdAt(),
                view.readAt(),
                view.senderId(),
                view.senderName(),
                view.senderRole() == null ? null : view.senderRole().code());
    }

    public SupportResponse.Created toResponse(SupportTicketCreated view) {
        return new SupportResponse.Created(view.ticketId(), view.status().code());
    }

    public SupportResponse.Sent toResponse(SupportMessageSent view) {
        return new SupportResponse.Sent(view.messageId(), view.created());
    }
}
