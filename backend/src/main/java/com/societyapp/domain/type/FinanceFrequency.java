package com.societyapp.domain.type;

public enum FinanceFrequency implements DomainCode {
    MONTHLY,
    QUARTERLY,
    ANNUAL,
    ONE_TIME;

    public static FinanceFrequency from(String value) {
        return DomainCode.parse(FinanceFrequency.class, value, "finance frequency");
    }
}
