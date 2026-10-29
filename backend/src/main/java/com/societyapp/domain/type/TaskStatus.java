package com.societyapp.domain.type;

import java.util.Set;

public enum TaskStatus implements DomainCode {
    OPEN,
    IN_PROGRESS,
    BLOCKED,
    COMPLETED,
    CANCELLED;

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }

    public Set<TaskStatus> allowedTransitions() {
        return switch (this) {
            case OPEN -> Set.of(IN_PROGRESS, BLOCKED, COMPLETED, CANCELLED);
            case IN_PROGRESS -> Set.of(BLOCKED, COMPLETED, CANCELLED);
            case BLOCKED -> Set.of(IN_PROGRESS, CANCELLED);
            case COMPLETED -> Set.of(IN_PROGRESS);
            case CANCELLED -> Set.of(OPEN);
        };
    }

    public static TaskStatus from(String value) {
        return DomainCode.parse(TaskStatus.class, value, "task status");
    }
}
