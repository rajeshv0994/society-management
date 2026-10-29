package com.societyapp.auth.application;

import com.societyapp.auth.application.port.in.UserLogin;
import com.societyapp.auth.application.port.in.UserSessions;
import com.societyapp.auth.application.port.out.AccessTokenIssuer;
import com.societyapp.auth.application.port.out.RefreshSessionStore;
import com.societyapp.auth.application.port.out.UserAccountRepository;
import com.societyapp.auth.domain.RefreshSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

public class UserSessionService implements UserSessions {
    private final UserAccountRepository accounts;
    private final AccessTokenIssuer accessTokens;
    private final RefreshSessionStore sessions;
    private final SecureRandom random = new SecureRandom();
    private final long refreshExpirationDays;

    public UserSessionService(
            UserAccountRepository accounts,
            AccessTokenIssuer accessTokens,
            RefreshSessionStore sessions,
            long refreshExpirationDays) {
        if (refreshExpirationDays <= 0) {
            throw new IllegalArgumentException("Refresh expiration must be positive");
        }
        this.accounts = accounts;
        this.accessTokens = accessTokens;
        this.sessions = sessions;
        this.refreshExpirationDays = refreshExpirationDays;
    }

    @Override
    public Session start(UserLogin.Result login) {
        var rawToken = newRefreshToken();
        var now = OffsetDateTime.now();
        var session =
                new RefreshSession(
                        UUID.randomUUID(),
                        login.userId(),
                        UUID.randomUUID(),
                        hash(rawToken),
                        now.plusDays(refreshExpirationDays),
                        now,
                        null,
                        null);
        sessions.create(session);
        return new Session(
                login.token(),
                rawToken,
                accessTokens.expiresInSeconds(),
                login.userId(),
                login.fullName(),
                login.role(),
                login.societyId());
    }

    @Override
    public Session refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthenticationFailedException("Session has expired. Please sign in again.");
        }
        var current =
                sessions.findForUpdate(hash(refreshToken))
                        .orElseThrow(
                                () ->
                                        new AuthenticationFailedException(
                                                "Session has expired. Please sign in again."));
        var now = OffsetDateTime.now();
        if (current.isRevoked()) {
            sessions.revokeFamily(current.familyId(), now);
            throw new AuthenticationFailedException(
                    "This session was already used and has been revoked.");
        }
        if (current.isExpired(now)) {
            sessions.revoke(current.id(), now);
            throw new AuthenticationFailedException("Session has expired. Please sign in again.");
        }
        var account =
                accounts.findById(current.userId())
                        .filter(candidate -> candidate.isActive())
                        .orElseThrow(
                                () ->
                                        new AuthenticationFailedException(
                                                "Account is no longer active."));
        var replacementToken = newRefreshToken();
        var replacement =
                new RefreshSession(
                        UUID.randomUUID(),
                        account.id(),
                        current.familyId(),
                        hash(replacementToken),
                        now.plusDays(refreshExpirationDays),
                        now,
                        null,
                        null);
        sessions.replace(current.id(), replacement, now);
        return new Session(
                accessTokens.issue(account),
                replacementToken,
                accessTokens.expiresInSeconds(),
                account.id(),
                account.fullName(),
                account.role(),
                account.societyId());
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;
        sessions.findForUpdate(hash(refreshToken))
                .ifPresent(session -> sessions.revoke(session.id(), OffsetDateTime.now()));
    }

    @Override
    public void logoutAll(UUID userId) {
        sessions.revokeAllForUser(userId, OffsetDateTime.now());
    }

    @Override
    public Profile current(UUID userId) {
        var account =
                accounts.findById(userId)
                        .filter(candidate -> candidate.isActive())
                        .orElseThrow(
                                () -> new AuthenticationFailedException("Account is not active."));
        return new Profile(
                account.id(),
                account.fullName(),
                account.email(),
                account.role(),
                account.societyId());
    }

    private String newRefreshToken() {
        var bytes = new byte[48];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
