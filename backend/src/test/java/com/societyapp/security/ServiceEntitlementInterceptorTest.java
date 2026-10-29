package com.societyapp.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.societyapp.application.port.in.ServiceEntitlementUseCase;
import com.societyapp.application.port.in.SubscriptionAccessUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class ServiceEntitlementInterceptorTest {
    private final UUID societyId = UUID.randomUUID();
    private ServiceEntitlementUseCase entitlements;
    private ServiceEntitlementInterceptor interceptor;
    private SubscriptionAccessUseCase subscriptions;

    @BeforeEach
    void setUp() {
        entitlements = mock(ServiceEntitlementUseCase.class);
        subscriptions = mock(SubscriptionAccessUseCase.class);
        interceptor = new ServiceEntitlementInterceptor(entitlements, subscriptions);
        when(entitlements.isEnabled(
                        societyId, com.societyapp.domain.type.BuiltInServiceCode.PAYMENTS))
                .thenReturn(true);
        when(subscriptions.evaluate(societyId))
                .thenReturn(SubscriptionAccessUseCase.Decision.permit());
        authenticate(societyId, "RESIDENT");
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void disabledServiceEndpointIsForbidden() throws Exception {
        var response = mock(HttpServletResponse.class);

        assertThat(request("/api/amenities", response)).isFalse();
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void enabledServiceEndpointIsAllowed() throws Exception {
        assertThat(request("/api/billing/invoices", mock(HttpServletResponse.class))).isTrue();
    }

    @Test
    void expiredMembershipBlocksAnExistingAccessTokenImmediately() throws Exception {
        var response = mock(HttpServletResponse.class);
        var output = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(output));
        when(subscriptions.evaluate(societyId))
                .thenReturn(SubscriptionAccessUseCase.Decision.deny("Payment required"));

        assertThat(request("/api/amenities", response)).isFalse();

        verify(response).setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
        assertThat(output.toString()).contains("Payment required");
    }

    @Test
    void expiredSocietyAdminCanOnlyReachMembershipRecoveryAndReadProfile() throws Exception {
        when(subscriptions.evaluate(societyId))
                .thenReturn(SubscriptionAccessUseCase.Decision.deny("Payment required"));
        authenticate(societyId, "SOCIETY_ADMIN");

        assertThat(request("/api/society/subscription/orders", mock(HttpServletResponse.class)))
                .isTrue();
        var profileRequest = mock(HttpServletRequest.class);
        when(profileRequest.getRequestURI()).thenReturn("/api/society/profile");
        when(profileRequest.getMethod()).thenReturn("GET");
        assertThat(
                        interceptor.preHandle(
                                profileRequest, mock(HttpServletResponse.class), new Object()))
                .isTrue();
    }

    @Test
    void financeRequiresBillingEvenWhenPaymentsAreEnabled() throws Exception {
        var response = mock(HttpServletResponse.class);

        assertThat(request("/api/billing/expenses", response)).isFalse();
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void platformAdminWithoutSocietyIsNotFiltered() throws Exception {
        authenticate(null, "SUPER_ADMIN");
        assertThat(request("/api/amenities", mock(HttpServletResponse.class))).isTrue();
        verifyNoInteractions(entitlements);
    }

    @Test
    void anonymousSecurityContextCannotBreakPublicOrPreAuthenticationRequests() throws Exception {
        SecurityContextHolder.clearContext();

        assertThat(request("/api/amenities", mock(HttpServletResponse.class))).isTrue();
        assertThat(request("/api/public/plans", mock(HttpServletResponse.class))).isTrue();
        verifyNoInteractions(entitlements);
    }

    private boolean request(String path, HttpServletResponse response) throws Exception {
        var request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(path);
        return interceptor.preHandle(request, response, new Object());
    }

    private void authenticate(UUID tenantId, String role) {
        var jwt = new JwtService("test-secret-key-that-is-long-enough-for-hmac", 900_000);
        var userId = UUID.randomUUID();
        var claims = jwt.parse(jwt.issue(userId, "test@example.com", role, tenantId));
        var authentication =
                new UsernamePasswordAuthenticationToken(
                        userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        authentication.setDetails(claims);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
