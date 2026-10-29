package com.societyapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    OpenAPI societyOpenApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("SocietyHub API")
                                .version("1.0.0")
                                .description(
                                        "Multi-tenant API for platform administrators, society administrators, staff, and members. Public registration and login operations do not require a token; secured operations use the JWT returned by /api/auth/login.")
                                .contact(
                                        new Contact()
                                                .name("SocietyHub API Support")
                                                .email("support@society.local"))
                                .license(new License().name("Proprietary")))
                .servers(
                        List.of(
                                new Server()
                                        .url("http://localhost:8080")
                                        .description("Local development")))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        BEARER_AUTH,
                                        new SecurityScheme()
                                                .name(BEARER_AUTH)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description(
                                                        "Paste the JWT access token returned by POST /api/auth/login.")));
    }

    @Bean
    GlobalOpenApiCustomizer publicOperationsDoNotRequireAuthentication() {
        return openApi ->
                openApi.getPaths()
                        .forEach(
                                (path, item) -> {
                                    boolean publicOperation =
                                            path.startsWith("/api/public/")
                                                    || path.equals("/api/auth/login")
                                                    || path.equals("/api/auth/refresh")
                                                    || path.equals("/api/auth/logout");
                                    item.readOperations()
                                            .forEach(
                                                    operation ->
                                                            operation.setSecurity(
                                                                    publicOperation
                                                                            ? List.of()
                                                                            : List.of(
                                                                                    new SecurityRequirement()
                                                                                            .addList(
                                                                                                    BEARER_AUTH))));
                                });
    }
}
