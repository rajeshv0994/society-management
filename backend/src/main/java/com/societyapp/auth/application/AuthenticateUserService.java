package com.societyapp.auth.application;

import com.societyapp.auth.application.port.in.UserLogin;
import com.societyapp.auth.application.port.out.AccessTokenIssuer;
import com.societyapp.auth.application.port.out.PasswordVerifier;
import com.societyapp.auth.application.port.out.SocietyAccessPolicy;
import com.societyapp.auth.application.port.out.UserAccountRepository;

public class AuthenticateUserService implements UserLogin {
    private final UserAccountRepository accounts;
    private final PasswordVerifier passwords;
    private final AccessTokenIssuer tokens;
    private final SocietyAccessPolicy societyAccess;

    public AuthenticateUserService(
            UserAccountRepository accounts,
            PasswordVerifier passwords,
            AccessTokenIssuer tokens,
            SocietyAccessPolicy societyAccess) {
        this.accounts = accounts;
        this.passwords = passwords;
        this.tokens = tokens;
        this.societyAccess = societyAccess;
    }

    @Override
    public Result authenticate(Command command) {
        var account =
                accounts.findByEmail(command.email().trim())
                        .filter(
                                candidate ->
                                        passwords.matches(
                                                command.password(), candidate.passwordHash()))
                        .filter(candidate -> candidate.isActive())
                        .filter(
                                candidate ->
                                        candidate.societyId() == null
                                                || normalize(command.societyCode())
                                                        .equals(candidate.societyCode()))
                        .orElseThrow(AuthenticationFailedException::new);

        societyAccess
                .denialReason(account)
                .ifPresent(
                        reason -> {
                            throw new AuthenticationFailedException(reason);
                        });

        return new Result(
                tokens.issue(account),
                account.id(),
                account.fullName(),
                account.role(),
                account.societyId());
    }

    private String normalize(String societyCode) {
        return societyCode == null ? "" : societyCode.trim().toUpperCase();
    }
}
