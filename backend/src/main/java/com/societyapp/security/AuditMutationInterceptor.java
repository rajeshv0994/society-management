package com.societyapp.security;

import com.societyapp.application.model.audit.AuditModels;
import com.societyapp.application.port.in.AuditUseCase;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

@Component
public class AuditMutationInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(AuditMutationInterceptor.class);
    private static final String CONTEXT = AuditMutationInterceptor.class.getName() + ".context";
    private final AuditUseCase audits;

    public AuditMutationInterceptor(AuditUseCase audits) {
        this.audits = audits;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod method && isMutation(method)) {
            request.setAttribute(
                    CONTEXT,
                    new Context(
                            currentClaimUuid("societyId"),
                            currentSubject(),
                            action(method),
                            entityType(method)));
        }
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception) {
        var context = request.getAttribute(CONTEXT);
        if (!(context instanceof Context auditContext)
                || exception != null
                || response.getStatus() >= 400) {
            return;
        }
        String route =
                Objects.toString(
                        request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE),
                        request.getRequestURI());
        @SuppressWarnings("unchecked")
        Map<String, String> pathVariables =
                Optional.ofNullable(
                                (Map<String, String>)
                                        request.getAttribute(
                                                HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
                        .orElse(Map.of());
        try {
            audits.record(
                    new AuditModels.RecordCommand(
                            auditContext.societyId(),
                            auditContext.actorId(),
                            auditContext.action(),
                            auditContext.entityType(),
                            entityId(pathVariables),
                            isSensitive(request.getMethod(), route),
                            new AuditModels.Details(
                                    Map.of(
                                            "http_method",
                                            request.getMethod(),
                                            "route",
                                            route,
                                            "outcome",
                                            "SUCCEEDED"))));
        } catch (RuntimeException failure) {
            log.error(
                    "Unable to persist audit event for {} {}", request.getMethod(), route, failure);
        }
    }

    private boolean isMutation(HandlerMethod method) {
        var element = method.getMethod();
        return AnnotatedElementUtils.hasAnnotation(element, PostMapping.class)
                || AnnotatedElementUtils.hasAnnotation(element, PutMapping.class)
                || AnnotatedElementUtils.hasAnnotation(element, PatchMapping.class)
                || AnnotatedElementUtils.hasAnnotation(element, DeleteMapping.class);
    }

    private String action(HandlerMethod method) {
        return "API_" + snake(method.getMethod().getName());
    }

    private String entityType(HandlerMethod method) {
        return snake(method.getBeanType().getSimpleName().replaceFirst("Controller$", ""));
    }

    private String snake(String value) {
        return value.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toUpperCase(Locale.ROOT);
    }

    private String entityId(Map<String, String> values) {
        if (values.containsKey("id")) return values.get("id");
        return values.entrySet().stream()
                .filter(entry -> entry.getKey().toLowerCase(Locale.ROOT).endsWith("id"))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private boolean isSensitive(String method, String route) {
        if (Set.of("PUT", "PATCH", "DELETE").contains(method)) return true;
        String normalized = route.toLowerCase(Locale.ROOT);
        return List.of(
                        "/residents",
                        "/staff",
                        "/households",
                        "/permissions",
                        "/billing",
                        "/payments",
                        "/finance",
                        "/configuration",
                        "/structure",
                        "/documents",
                        "/storage",
                        "/profile",
                        "/emergencies")
                .stream()
                .anyMatch(normalized::contains);
    }

    private Claims claims() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getDetails() instanceof Claims value
                ? value
                : null;
    }

    private UUID currentSubject() {
        var claims = claims();
        return uuid(claims == null ? null : claims.getSubject());
    }

    private UUID currentClaimUuid(String name) {
        var claims = claims();
        return uuid(claims == null ? null : claims.get(name, String.class));
    }

    private UUID uuid(String value) {
        try {
            return value == null ? null : UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private record Context(UUID societyId, UUID actorId, String action, String entityType) {}
}
