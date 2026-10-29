package com.societyapp.domain.type;

public enum ExpenseCategory implements DomainCode {
    UTILITIES,
    REPAIRS,
    SECURITY,
    HOUSEKEEPING,
    VENDOR,
    PURCHASE,
    OTHER;

    public static ExpenseCategory from(String value) {
        return DomainCode.parse(ExpenseCategory.class, value, "expense category");
    }
}
