package com.societyapp.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SourceSizeArchitectureTest {
    private static final int USE_CASE_TARGET = 350;
    private static final int CONTROLLER_TARGET = 250;

    @Test
    void useCasesCannotGrowBeyondTheMigrationBaseline() throws IOException {
        assertSourceLimits(
                Path.of("src/main/java/com/societyapp/application/usecase"), USE_CASE_TARGET);
        assertSourceLimits(
                Path.of("src/main/java/com/societyapp/application/service"), USE_CASE_TARGET);
    }

    @Test
    void controllersStayFocusedOnHttpAdaptation() throws IOException {
        assertSourceLimits(Path.of("src/main/java/com/societyapp/controller"), CONTROLLER_TARGET);
    }

    private void assertSourceLimits(Path folder, int target) throws IOException {
        try (var files = Files.list(folder)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                long lines;
                try (var sourceLines = Files.lines(file)) {
                    lines = sourceLines.count();
                }
                assertThat(lines)
                        .as(
                                "%s has %d lines; extract a cohesive use case, model, or mapper instead of increasing its %d-line ceiling",
                                file.getFileName(), lines, target)
                        .isLessThanOrEqualTo(target);
            }
        }
    }
}
