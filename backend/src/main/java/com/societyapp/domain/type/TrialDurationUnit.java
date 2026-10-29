package com.societyapp.domain.type;

public enum TrialDurationUnit implements DomainCode {
    DAYS,
    MONTHS;

    public static TrialDurationUnit from(String value) {
        return DomainCode.parse(TrialDurationUnit.class, value, "trial duration unit");
    }
}
