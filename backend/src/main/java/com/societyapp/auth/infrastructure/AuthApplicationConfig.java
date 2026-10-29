package com.societyapp.auth.infrastructure;

import com.societyapp.auth.application.AuthenticateUserService;
import com.societyapp.auth.application.UserSessionService;
import com.societyapp.auth.application.port.in.UserLogin;
import com.societyapp.auth.application.port.in.UserSessions;
import com.societyapp.auth.application.port.out.AccessTokenIssuer;
import com.societyapp.auth.application.port.out.PasswordVerifier;
import com.societyapp.auth.application.port.out.RefreshSessionStore;
import com.societyapp.auth.application.port.out.SocietyAccessPolicy;
import com.societyapp.auth.application.port.out.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AuthApplicationConfig {
    @Bean
    AuthenticateUserService authenticateUserService(
            UserAccountRepository accounts,
            PasswordVerifier passwords,
            AccessTokenIssuer tokens,
            SocietyAccessPolicy societyAccess) {
        return new AuthenticateUserService(accounts, passwords, tokens, societyAccess);
    }

    @Bean
    UserSessionService userSessionService(
            UserAccountRepository accounts,
            AccessTokenIssuer accessTokens,
            RefreshSessionStore sessions,
            @Value("${jwt.refresh-expiration-days:30}") long refreshExpirationDays) {
        return new UserSessionService(accounts, accessTokens, sessions, refreshExpirationDays);
    }

    @Bean
    @Primary
    UserLogin transactionalUserLogin(
            AuthenticateUserService delegate, PlatformTransactionManager transactions) {
        return new TransactionalUserLogin(delegate, transactions);
    }

    @Bean
    @Primary
    UserSessions transactionalUserSessions(
            UserSessionService delegate, PlatformTransactionManager transactions) {
        return new TransactionalUserSessions(delegate, transactions);
    }
}
