package com.societyapp.domain.type;

public enum TaskEventType implements DomainCode {
    CREATED,
    REASSIGNED,
    EDITED,
    PROGRESS_NOTE,
    STATUS_CHANGED,
    COMMENT;

    public static TaskEventType from(String value) {
        return DomainCode.parse(TaskEventType.class, value, "task event type");
    }
}
