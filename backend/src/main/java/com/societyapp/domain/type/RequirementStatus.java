package com.societyapp.domain.type;

public enum RequirementStatus implements DomainCode {
    NEW(1, false),
    CONTACTED(2, false),
    ANALYSING(3, false),
    ACCEPTED(3, true),
    APPROVED(3, false),
    REJECTED(3, true),
    CLOSED(3, true);

    private final int order;
    private final boolean closed;

    RequirementStatus(int order, boolean closed) {
        this.order = order;
        this.closed = closed;
    }

    public int order() {
        return order;
    }

    public boolean isClosed() {
        return closed;
    }

    public static RequirementStatus from(String value) {
        return DomainCode.parse(RequirementStatus.class, value, "requirement status");
    }
}
