package com.societyapp.domain.type;

public enum SupportTicketStatus implements DomainCode {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED;

    public static SupportTicketStatus from(String value) {
        return DomainCode.parse(SupportTicketStatus.class, value, "support ticket status");
    }
}
