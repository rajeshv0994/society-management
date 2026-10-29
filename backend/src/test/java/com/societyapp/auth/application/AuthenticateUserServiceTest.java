package com.societyapp.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.societyapp.auth.application.port.in.UserLogin;
import com.societyapp.auth.application.port.out.AccessTokenIssuer;
import com.societyapp.auth.application.port.out.PasswordVerifier;
import com.societyapp.auth.application.port.out.SocietyAccessPolicy;
import com.societyapp.auth.application.port.out.UserAccountRepository;
import com.societyapp.auth.domain.UserAccount;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuthenticateUserServiceTest {
    private final UserAccountRepository accounts = mock(UserAccountRepository.class);
    private final PasswordVerifier passwords = mock(PasswordVerifier.class);
    private final AccessTokenIssuer tokens = mock(AccessTokenIssuer.class);
    private final SocietyAccessPolicy societyAccess = mock(SocietyAccessPolicy.class);
    private final AuthenticateUserService service =
            new AuthenticateUserService(accounts, passwords, tokens, societyAccess);

    @Test
    void authenticatesAnActiveAccount() {
        var account =
                new UserAccount(
                        UUID.randomUUID(),
                        "Platform Admin",
                        "admin@example.com",
                        "encoded",
                        "SUPER_ADMIN",
                        null,
                        null,
                        UserAccount.Status.ACTIVE);
        when(accounts.findByEmail("admin@example.com")).thenReturn(Optional.of(account));
        when(passwords.matches("secret", "encoded")).thenReturn(true);
        when(tokens.issue(account)).thenReturn("access-token");
        when(societyAccess.denialReason(account)).thenReturn(Optional.empty());

        var result =
                service.authenticate(new UserLogin.Command(" admin@example.com ", "secret", null));

        assertThat(result.token()).isEqualTo("access-token");
        assertThat(result.userId()).isEqualTo(account.id());
        assertThat(result.role()).isEqualTo("SUPER_ADMIN");
        verify(accounts).findByEmail("admin@example.com");
    }

    @Test
    void rejectsAnUnknownAccountWithoutLeakingItsExistence() {
        when(accounts.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                service.authenticate(
                                        new UserLogin.Command(
                                                "missing@example.com", "secret", "SOCIETY")))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void rejectsAnInactiveAccount() {
        var account =
                new UserAccount(
                        UUID.randomUUID(),
                        "Suspended User",
                        "user@example.com",
                        "encoded",
                        "RESIDENT",
                        UUID.randomUUID(),
                        "SOCIETY",
                        UserAccount.Status.SUSPENDED);
        when(accounts.findByEmail("user@example.com")).thenReturn(Optional.of(account));
        when(passwords.matches("secret", "encoded")).thenReturn(true);

        assertThatThrownBy(
                        () ->
                                service.authenticate(
                                        new UserLogin.Command(
                                                "user@example.com", "secret", "SOCIETY")))
                .isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    void rejectsAValidTenantAccountWhenSocietyCodeDoesNotMatch() {
        var account =
                new UserAccount(
                        UUID.randomUUID(),
                        "Resident",
                        "resident@example.com",
                        "encoded",
                        "RESIDENT",
                        UUID.randomUUID(),
                        "GREEN-VALLEY",
                        UserAccount.Status.ACTIVE);
        when(accounts.findByEmail("resident@example.com")).thenReturn(Optional.of(account));
        when(passwords.matches("secret", "encoded")).thenReturn(true);

        assertThatThrownBy(
                        () ->
                                service.authenticate(
                                        new UserLogin.Command(
                                                "resident@example.com", "secret", "SUNRISE")))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void explainsInactiveSocietyOnlyAfterCredentialsAndCodeAreValid() {
        var account =
                new UserAccount(
                        UUID.randomUUID(),
                        "Resident",
                        "resident@example.com",
                        "encoded",
                        "RESIDENT",
                        UUID.randomUUID(),
                        "GREEN-VALLEY",
                        UserAccount.Status.ACTIVE);
        when(accounts.findByEmail("resident@example.com")).thenReturn(Optional.of(account));
        when(passwords.matches("secret", "encoded")).thenReturn(true);
        when(societyAccess.denialReason(account))
                .thenReturn(Optional.of("Society account is not active"));

        assertThatThrownBy(
                        () ->
                                service.authenticate(
                                        new UserLogin.Command(
                                                "resident@example.com", "secret", "GREEN-VALLEY")))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Society account is not active");
    }
}
