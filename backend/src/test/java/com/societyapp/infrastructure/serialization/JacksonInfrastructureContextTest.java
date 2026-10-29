package com.societyapp.infrastructure.serialization;

import static org.assertj.core.api.Assertions.assertThat;

import com.societyapp.application.port.out.JsonCodec;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.ObjectMapper;

@JsonTest
@Import(JacksonJsonCodec.class)
class JacksonInfrastructureContextTest {
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JsonCodec codec;

    @Test
    void bootFourProvidesJacksonThreeToTheJsonAdapter() {
        assertThat(objectMapper.version().getMajorVersion()).isEqualTo(3);
        assertThat(codec.write(List.of("PAYMENTS"))).isEqualTo("[\"PAYMENTS\"]");
    }
}
