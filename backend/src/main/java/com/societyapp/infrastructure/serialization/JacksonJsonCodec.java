package com.societyapp.infrastructure.serialization;

import com.societyapp.application.port.out.JsonCodec;
import com.societyapp.application.port.out.JsonCodecException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class JacksonJsonCodec implements JsonCodec {
    private final ObjectMapper mapper;

    public JacksonJsonCodec(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String write(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new JsonCodecException("JSON serialization failed", exception);
        }
    }

    @Override
    public Object readObject(String value) {
        try {
            return mapper.readValue(value, Object.class);
        } catch (JacksonException exception) {
            throw new JsonCodecException("JSON parsing failed", exception);
        }
    }

    @Override
    public List<String> readStrings(String value) {
        try {
            return mapper.readValue(value, new TypeReference<>() {});
        } catch (JacksonException exception) {
            throw new JsonCodecException("JSON string-list parsing failed", exception);
        }
    }

    @Override
    public List<Map<String, Object>> readObjectList(String value) {
        try {
            return mapper.readValue(value, new TypeReference<>() {});
        } catch (JacksonException exception) {
            throw new JsonCodecException("JSON object-list parsing failed", exception);
        }
    }

    @Override
    public JsonValue readTree(String value) {
        try {
            return new JacksonJsonValue(mapper.readTree(value));
        } catch (JacksonException exception) {
            throw new JsonCodecException("JSON tree parsing failed", exception);
        }
    }

    private record JacksonJsonValue(JsonNode node) implements JsonValue {
        @Override
        public JsonValue path(String field) {
            return new JacksonJsonValue(node.path(field));
        }

        @Override
        public boolean isMissingNode() {
            return node.isMissingNode();
        }

        @Override
        public boolean hasNonNull(String field) {
            return node.hasNonNull(field);
        }

        @Override
        public String asText() {
            return node.asText();
        }

        @Override
        public String asText(String defaultValue) {
            return node.asText(defaultValue);
        }

        @Override
        public long asLong() {
            return node.asLong();
        }

        @Override
        public boolean asBoolean(boolean defaultValue) {
            return node.asBoolean(defaultValue);
        }
    }
}
