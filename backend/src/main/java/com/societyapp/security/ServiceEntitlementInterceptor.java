package com.societyapp.security;

import com.societyapp.application.port.in.ServiceEntitlementUseCase;
import com.societyapp.application.port.in.SubscriptionAccessUseCase;
import com.societyapp.domain.type.BuiltInServiceCode;
import com.societyapp.domain.type.UserRole;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ServiceEntitlementInterceptor implements HandlerInterceptor {
    private static final Map<String, BuiltInServiceCode> ROUTES = routes();
    private final ServiceEntitlementUseCase entitlements;
    private final SubscriptionAccessUseCase subscriptions;

    public ServiceEntitlementInterceptor(
            ServiceEntitlementUseCase entitlements, SubscriptionAccessUseCase subscriptions) {
        this.entitlements = entitlements;
        this.subscriptions = subscriptions;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        var path = request.getRequestURI();
        if (path.startsWith("/api/public/")
                || path.startsWith("/api/auth/")
                || path.startsWith("/api/admin/platform/")) return true;
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getDetails() instanceof Claims) {
            var societyId = CurrentUser.societyId();
            if (societyId != null) {
                var decision = subscriptions.evaluate(societyId);
                var role = ((Claims) authentication.getDetails()).get("role", String.class);
                boolean paymentRecovery =
                        UserRole.SOCIETY_ADMIN.matches(role)
                                && (path.startsWith("/api/society/subscription")
                                        || ("GET".equals(request.getMethod())
                                                && path.equals("/api/society/profile")));
                if (!decision.allowed() && !paymentRecovery) {
                    response.setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"" + decision.reason() + "\"}");
                    return false;
                }
            }
        }
        if (path.startsWith("/api/society/staff/me")) return true;
        var required =
                ROUTES.entrySet().stream()
                        .filter(entry -> path.startsWith(entry.getKey()))
                        .map(Map.Entry::getValue)
                        .findFirst()
                        .orElse(null);
        if (required == null) return true;
        authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getDetails() instanceof Claims)) return true;
        var societyId = CurrentUser.societyId();
        if (societyId == null) return true;
        if (!entitlements.isEnabled(societyId, required)) {
            // sendError dispatches through /error, where the security filter can replace the
            // intended entitlement response with 401. A direct status preserves the real result.
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    private static Map<String, BuiltInServiceCode> routes() {
        var routes = new LinkedHashMap<String, BuiltInServiceCode>();
        routes.put("/api/billing/expenses", BuiltInServiceCode.BILLING);
        routes.put("/api/billing/charge-settings", BuiltInServiceCode.BILLING);
        routes.put("/api/billing/finance-summary", BuiltInServiceCode.BILLING);
        routes.put("/api/billing/budget", BuiltInServiceCode.BILLING);
        routes.put("/api/billing/staff-salaries", BuiltInServiceCode.BILLING);
        routes.put("/api/billing", BuiltInServiceCode.PAYMENTS);
        routes.put("/api/activities", BuiltInServiceCode.ACTIVITIES_GALLERIES);
        routes.put("/api/amenities", BuiltInServiceCode.AMENITIES);
        routes.put("/api/complaints", BuiltInServiceCode.COMPLAINTS);
        routes.put("/api/deliveries", BuiltInServiceCode.DELIVERIES);
        routes.put("/api/emergencies", BuiltInServiceCode.EMERGENCY_INCIDENTS);
        routes.put("/api/households", BuiltInServiceCode.RESIDENTS);
        routes.put("/api/member/documents", BuiltInServiceCode.DOCUMENTS);
        routes.put("/api/member", BuiltInServiceCode.RESIDENTS);
        routes.put("/api/notices", BuiltInServiceCode.NOTICES);
        routes.put("/api/society/documents", BuiltInServiceCode.DOCUMENTS);
        routes.put("/api/society/staff", BuiltInServiceCode.STAFF);
        routes.put("/api/tasks", BuiltInServiceCode.STAFF);
        routes.put("/api/visitors", BuiltInServiceCode.VISITORS);
        return Collections.unmodifiableMap(routes);
    }
}
