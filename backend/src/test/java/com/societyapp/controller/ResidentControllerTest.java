package com.societyapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.societyapp.application.model.resident.ResidentModels;
import com.societyapp.application.service.ResidentService;
import com.societyapp.domain.type.RegistrationStatus;
import com.societyapp.domain.type.ResidentType;
import com.societyapp.security.JwtService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class ResidentControllerTest {
    private final ResidentService residents = mock(ResidentService.class);
    private final ResidentController controller = new ResidentController(residents);

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void adminCreationUsesAuthenticatedSocietyAndActor() {
        var societyId = UUID.randomUUID();
        var adminId = UUID.randomUUID();
        var flatId = UUID.randomUUID();
        authenticate(adminId, societyId);
        var moveIn = LocalDate.of(2026, 7, 1);
        when(residents.createResident(any()))
                .thenReturn(
                        new ResidentModels.CreatedResident(
                                UUID.randomUUID(), RegistrationStatus.ACTIVE, UUID.randomUUID()));

        controller.create(
                new ResidentController.AdminCreate(
                        flatId,
                        "OWNER",
                        "New Member",
                        "9000000000",
                        "member@example.com",
                        null,
                        null,
                        null,
                        moveIn,
                        null,
                        "Member@123"));

        verify(residents)
                .createResident(
                        eq(
                                new ResidentModels.AdminCreateCommand(
                                        new ResidentModels.RegistrationCommand(
                                                societyId,
                                                flatId,
                                                ResidentType.OWNER,
                                                "New Member",
                                                "9000000000",
                                                "member@example.com",
                                                null,
                                                null,
                                                null,
                                                moveIn,
                                                null),
                                        adminId,
                                        "Member@123")));
    }

    private void authenticate(UUID userId, UUID societyId) {
        var jwt = new JwtService("test-secret-key-that-is-long-enough-for-hmac", 900_000);
        var claims = jwt.parse(jwt.issue(userId, "admin@example.com", "SOCIETY_ADMIN", societyId));
        var authentication =
                new UsernamePasswordAuthenticationToken(
                        userId, null, List.of(new SimpleGrantedAuthority("ROLE_SOCIETY_ADMIN")));
        authentication.setDetails(claims);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
