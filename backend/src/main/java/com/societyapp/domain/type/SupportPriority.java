package com.societyapp.domain.type;

public enum SupportPriority implements DomainCode {
    URGENT(1),
    HIGH(2),
    NORMAL(3),
    LOW(3);

    private final int order;

    SupportPriority(int order) {
        this.order = order;
    }

    public int order() {
        return order;
    }

    public static SupportPriority from(String value) {
        return DomainCode.parse(SupportPriority.class, value, "support priority");
    }
}
