package com.societyapp.controller;

import com.societyapp.auth.application.port.in.UserLogin;
import com.societyapp.auth.application.port.in.UserSessions;
import com.societyapp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserLogin login;
    private final UserSessions sessions;

    public AuthController(UserLogin login, UserSessions sessions) {
        this.login = login;
        this.sessions = sessions;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return response(
                sessions.start(
                        login.authenticate(
                                new UserLogin.Command(
                                        request.email(),
                                        request.password(),
                                        request.societyCode()))));
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return response(sessions.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshRequest request) {
        sessions.logout(request.refreshToken());
    }

    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logoutAll() {
        sessions.logoutAll(CurrentUser.id());
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserSessions.Profile me() {
        return sessions.current(CurrentUser.id());
    }

    private AuthResponse response(UserSessions.Session session) {
        return new AuthResponse(
                session.accessToken(),
                session.accessToken(),
                session.refreshToken(),
                "Bearer",
                session.expiresIn(),
                session.userId(),
                session.fullName(),
                session.role(),
                session.societyId());
    }

    public record LoginRequest(
            @NotBlank @Email String email, @NotBlank String password, String societyCode) {}

    public record RefreshRequest(@NotBlank String refreshToken) {}

    public record AuthResponse(
            String token,
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn,
            UUID userId,
            String fullName,
            String role,
            UUID societyId) {}
}
