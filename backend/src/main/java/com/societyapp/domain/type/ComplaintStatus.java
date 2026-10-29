package com.societyapp.domain.type;

import java.util.Set;

public enum ComplaintStatus implements DomainCode {
    OPEN,
    ASSIGNED,
    IN_PROGRESS,
    RESOLVED,
    CLOSED;

    public boolean isTerminal() {
        return this == RESOLVED || this == CLOSED;
    }

    public Set<ComplaintStatus> allowedTransitions() {
        return switch (this) {
            case OPEN -> Set.of(ASSIGNED, IN_PROGRESS, CLOSED);
            case ASSIGNED -> Set.of(IN_PROGRESS, RESOLVED, CLOSED);
            case IN_PROGRESS -> Set.of(RESOLVED, CLOSED);
            case RESOLVED -> Set.of(IN_PROGRESS, CLOSED);
            case CLOSED -> Set.of();
        };
    }

    public static ComplaintStatus from(String value) {
        return DomainCode.parse(ComplaintStatus.class, value, "complaint status");
    }
}
