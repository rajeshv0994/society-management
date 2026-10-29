package com.societyapp.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.societyapp.auth.application.port.in.UserLogin;
import com.societyapp.auth.application.port.out.AccessTokenIssuer;
import com.societyapp.auth.application.port.out.RefreshSessionStore;
import com.societyapp.auth.application.port.out.UserAccountRepository;
import com.societyapp.auth.domain.RefreshSession;
import com.societyapp.auth.domain.UserAccount;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class UserSessionServiceTest {
    private final UserAccountRepository accounts = mock(UserAccountRepository.class);
    private final AccessTokenIssuer accessTokens = mock(AccessTokenIssuer.class);
    private final RefreshSessionStore sessions = mock(RefreshSessionStore.class);
    private final UserSessionService service =
            new UserSessionService(accounts, accessTokens, sessions, 30);

    @Test
    void startsARefreshSessionAlongsideTheAccessToken() {
        var userId = UUID.randomUUID();
        var login =
                new UserLogin.Result(
                        "access-token",
                        userId,
                        "Society Admin",
                        "SOCIETY_ADMIN",
                        UUID.randomUUID());
        when(accessTokens.expiresInSeconds()).thenReturn(900L);

        var result = service.start(login);

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.expiresIn()).isEqualTo(900L);
        var captured = ArgumentCaptor.forClass(RefreshSession.class);
        verify(sessions).create(captured.capture());
        assertThat(captured.getValue().userId()).isEqualTo(userId);
        assertThat(captured.getValue().tokenHash()).hasSize(64);
    }

    @Test
    void rotatesAValidRefreshToken() {
        var userId = UUID.randomUUID();
        var societyId = UUID.randomUUID();
        var account =
                new UserAccount(
                        userId,
                        "Resident",
                        "resident@example.com",
                        "hash",
                        "RESIDENT",
                        societyId,
                        "SOCIETY",
                        UserAccount.Status.ACTIVE);
        var current =
                new RefreshSession(
                        UUID.randomUUID(),
                        userId,
                        UUID.randomUUID(),
                        "stored-hash",
                        OffsetDateTime.now().plusDays(1),
                        OffsetDateTime.now().minusDays(1),
                        null,
                        null);
        when(sessions.findForUpdate(anyString())).thenReturn(Optional.of(current));
        when(accounts.findById(userId)).thenReturn(Optional.of(account));
        when(accessTokens.issue(account)).thenReturn("new-access");
        when(accessTokens.expiresInSeconds()).thenReturn(900L);

        var result = service.refresh("valid-refresh-token");

        assertThat(result.accessToken()).isEqualTo("new-access");
        assertThat(result.refreshToken()).isNotEqualTo("valid-refresh-token");
        var replacement = ArgumentCaptor.forClass(RefreshSession.class);
        verify(sessions).replace(eq(current.id()), replacement.capture(), any());
        assertThat(replacement.getValue().familyId()).isEqualTo(current.familyId());
    }

    @Test
    void revokesTheWholeFamilyWhenAReplacedTokenIsReused() {
        var current =
                new RefreshSession(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "stored-hash",
                        OffsetDateTime.now().plusDays(1),
                        OffsetDateTime.now().minusDays(1),
                        OffsetDateTime.now(),
                        UUID.randomUUID());
        when(sessions.findForUpdate(anyString())).thenReturn(Optional.of(current));

        assertThatThrownBy(() -> service.refresh("reused-refresh-token"))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("revoked");
        verify(sessions).revokeFamily(eq(current.familyId()), any());
    }
}
