package com.societyapp.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

class TypedBoundaryArchitectureTest {
    private static final Pattern RAW_BOUNDED_INPUT_CODE =
            Pattern.compile(
                    "\\bString\\s+(role|status|priority|serviceCode|documentType|sourceType|paymentMethod|billingCycle|action)\\b");

    @Test
    void newAndMigratedInputPortsDoNotExposeGenericMapsOrObjects() {
        new ClassFileImporter()
                .importPackages("com.societyapp.application.port.in").stream()
                        .filter(javaClass -> javaClass.isInterface())
                        .forEach(javaClass -> assertTypedBoundary(javaClass.reflect()));
    }

    @Test
    void controllersDoNotExposeDynamicResponseMaps() {
        new ClassFileImporter()
                .importPackages("com.societyapp.controller").stream()
                        .filter(javaClass -> javaClass.isAnnotatedWith(RestController.class))
                        .forEach(
                                javaClass ->
                                        Arrays.stream(javaClass.reflect().getDeclaredMethods())
                                                .filter(
                                                        method ->
                                                                AnnotatedElementUtils.hasAnnotation(
                                                                        method,
                                                                        RequestMapping.class))
                                                .forEach(
                                                        method ->
                                                                assertThat(
                                                                                containsUntypedValue(
                                                                                        method
                                                                                                .getGenericReturnType()))
                                                                        .as(
                                                                                "return type of %s.%s",
                                                                                javaClass
                                                                                        .getSimpleName(),
                                                                                method.getName())
                                                                        .isFalse()));
    }

    @Test
    void controllersReturnHttpResponseModelsInsteadOfApplicationModels() {
        new ClassFileImporter()
                .importPackages("com.societyapp.controller").stream()
                        .filter(javaClass -> javaClass.isAnnotatedWith(RestController.class))
                        .forEach(
                                javaClass ->
                                        Arrays.stream(javaClass.reflect().getDeclaredMethods())
                                                .filter(
                                                        method ->
                                                                AnnotatedElementUtils.hasAnnotation(
                                                                        method,
                                                                        RequestMapping.class))
                                                .forEach(
                                                        method ->
                                                                assertThat(
                                                                                containsApplicationModel(
                                                                                        method
                                                                                                .getGenericReturnType()))
                                                                        .as(
                                                                                "return type of %s.%s",
                                                                                javaClass
                                                                                        .getSimpleName(),
                                                                                method.getName())
                                                                        .isFalse()));
    }

    @Test
    void compatibilityPayloadsLegacyPortsAndAdaptersStayDeleted() throws Exception {
        try (var files = Files.walk(Path.of("src/main/java/com/societyapp/application"))) {
            var forbidden =
                    files.filter(Files::isRegularFile)
                            .map(path -> path.getFileName().toString())
                            .filter(
                                    name ->
                                            name.equals("ApiPayload.java")
                                                    || name.equals("ApiPayloadList.java")
                                                    || name.startsWith("Legacy")
                                                            && name.endsWith("UseCase.java")
                                                    || name.endsWith("UseCaseAdapter.java"))
                            .toList();
            assertThat(forbidden).isEmpty();
        }
    }

    @Test
    void boundedBusinessCodesStayTypedAtInputPorts() throws Exception {
        try (var files = Files.walk(Path.of("src/main/java/com/societyapp/application/port/in"))) {
            var violations =
                    files.filter(path -> path.toString().endsWith(".java"))
                            .filter(
                                    path -> {
                                        try {
                                            return RAW_BOUNDED_INPUT_CODE
                                                    .matcher(Files.readString(path))
                                                    .find();
                                        } catch (java.io.IOException exception) {
                                            throw new IllegalStateException(exception);
                                        }
                                    })
                            .toList();
            assertThat(violations).isEmpty();
        }
    }

    private void assertTypedBoundary(Class<?> boundary) {
        for (var method : boundary.getDeclaredMethods()) {
            assertThat(containsUntypedValue(method.getGenericReturnType()))
                    .as("return type of %s.%s", boundary.getSimpleName(), method.getName())
                    .isFalse();
            Arrays.stream(method.getGenericParameterTypes())
                    .forEach(
                            parameter ->
                                    assertThat(containsUntypedValue(parameter))
                                            .as(
                                                    "parameter type of %s.%s",
                                                    boundary.getSimpleName(), method.getName())
                                            .isFalse());
        }
    }

    private boolean containsUntypedValue(Type type) {
        if (type == Object.class || type == Map.class) return true;
        if (type instanceof ParameterizedType parameterized) {
            if (parameterized.getRawType() == Map.class) return true;
            return Arrays.stream(parameterized.getActualTypeArguments())
                    .anyMatch(this::containsUntypedValue);
        }
        return false;
    }

    private boolean containsApplicationModel(Type type) {
        if (type instanceof Class<?> raw) {
            return raw.getPackageName().startsWith("com.societyapp.application.model");
        }
        if (type instanceof ParameterizedType parameterized) {
            return containsApplicationModel(parameterized.getRawType())
                    || Arrays.stream(parameterized.getActualTypeArguments())
                            .anyMatch(this::containsApplicationModel);
        }
        return false;
    }
}
