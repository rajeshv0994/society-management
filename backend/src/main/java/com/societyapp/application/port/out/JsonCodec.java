package com.societyapp.application.port.out;

import java.util.List;
import java.util.Map;

public interface JsonCodec {
    String write(Object value);

    Object readObject(String value);

    List<String> readStrings(String value);

    List<Map<String, Object>> readObjectList(String value);

    JsonValue readTree(String value);

    interface JsonValue {
        JsonValue path(String field);

        boolean isMissingNode();

        boolean hasNonNull(String field);

        String asText();

        String asText(String defaultValue);

        long asLong();

        boolean asBoolean(boolean defaultValue);
    }
}
