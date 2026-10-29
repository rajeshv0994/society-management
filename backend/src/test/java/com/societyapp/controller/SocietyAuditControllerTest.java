package com.societyapp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.societyapp.application.model.audit.AuditModels;
import com.societyapp.application.port.in.AuditUseCase;
import com.societyapp.security.JwtService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class SocietyAuditControllerTest {
    private final AuditUseCase audits = mock(AuditUseCase.class);
    private final SocietyAuditController controller = new SocietyAuditController(audits);

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void alwaysScopesSensitiveHistoryToAuthenticatedSociety() {
        var societyId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var jwt = new JwtService("test-secret-key-that-is-long-enough-for-hmac", 900_000);
        var claims = jwt.parse(jwt.issue(userId, "admin@example.com", "SOCIETY_ADMIN", societyId));
        var authentication =
                new UsernamePasswordAuthenticationToken(
                        userId, null, List.of(new SimpleGrantedAuthority("ROLE_SOCIETY_ADMIN")));
        authentication.setDetails(claims);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(audits.search(any())).thenReturn(new AuditModels.Page(List.of(), 0, 50, 0, 0));
        controller.audit(0, 50, true, null, null, null, null, null, null);

        var criteria = org.mockito.ArgumentCaptor.forClass(AuditModels.SearchCriteria.class);
        verify(audits).search(criteria.capture());
        assertThat(criteria.getValue().societyId()).isEqualTo(societyId);
        assertThat(criteria.getValue().sensitive()).isTrue();
    }
}
