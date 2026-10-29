package com.societyapp.auth.infrastructure;

import com.societyapp.auth.application.AuthenticationFailedException;
import com.societyapp.auth.application.port.in.UserLogin;
import com.societyapp.auth.application.port.in.UserSessions;
import java.util.UUID;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

final class TransactionalUserSessions implements UserSessions {
    private final UserSessions delegate;
    private final TransactionTemplate writeTransaction;
    private final TransactionTemplate readTransaction;

    TransactionalUserSessions(UserSessions delegate, PlatformTransactionManager transactions) {
        this.delegate = delegate;
        this.writeTransaction = new TransactionTemplate(transactions);
        this.readTransaction = new TransactionTemplate(transactions);
        this.readTransaction.setReadOnly(true);
    }

    @Override
    public Session start(UserLogin.Result login) {
        return writeTransaction.execute(status -> delegate.start(login));
    }

    @Override
    public Session refresh(String refreshToken) {
        var outcome =
                writeTransaction.execute(
                        status -> {
                            try {
                                return RefreshOutcome.success(delegate.refresh(refreshToken));
                            } catch (AuthenticationFailedException exception) {
                                return RefreshOutcome.failure(exception);
                            }
                        });
        if (outcome.failure() != null) throw outcome.failure();
        return outcome.session();
    }

    @Override
    public void logout(String refreshToken) {
        writeTransaction.executeWithoutResult(status -> delegate.logout(refreshToken));
    }

    @Override
    public void logoutAll(UUID userId) {
        writeTransaction.executeWithoutResult(status -> delegate.logoutAll(userId));
    }

    @Override
    public Profile current(UUID userId) {
        return readTransaction.execute(status -> delegate.current(userId));
    }

    private record RefreshOutcome(Session session, AuthenticationFailedException failure) {
        static RefreshOutcome success(Session session) {
            return new RefreshOutcome(session, null);
        }

        static RefreshOutcome failure(AuthenticationFailedException failure) {
            return new RefreshOutcome(null, failure);
        }
    }
}
