package com.societyapp.domain.type;

public enum IncidentSeverity implements DomainCode {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int rank;

    IncidentSeverity(int rank) {
        this.rank = rank;
    }

    public int rank() {
        return rank;
    }

    public static IncidentSeverity from(String value) {
        return DomainCode.parse(IncidentSeverity.class, value, "incident severity");
    }
}
