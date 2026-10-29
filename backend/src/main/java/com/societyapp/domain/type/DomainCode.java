package com.societyapp.domain.type;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface DomainCode {
    default String code() {
        return ((Enum<?>) this).name();
    }

    default boolean matches(String value) {
        return value != null && code().equals(value.trim().toUpperCase(Locale.ROOT));
    }

    static <E extends Enum<E>> E parse(Class<E> type, String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid " + field + ": " + value);
        }
    }

    static boolean matchesAny(String value, DomainCode... candidates) {
        return Stream.of(candidates).anyMatch(candidate -> candidate.matches(value));
    }

    static Set<String> codes(DomainCode... values) {
        return Stream.of(values).map(DomainCode::code).collect(Collectors.toUnmodifiableSet());
    }
}
