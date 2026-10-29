package com.societyapp.auth.infrastructure;

import com.societyapp.auth.application.port.in.UserLogin;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

final class TransactionalUserLogin implements UserLogin {
    private final UserLogin delegate;
    private final TransactionTemplate transaction;

    TransactionalUserLogin(UserLogin delegate, PlatformTransactionManager transactions) {
        this.delegate = delegate;
        this.transaction = new TransactionTemplate(transactions);
        this.transaction.setReadOnly(true);
    }

    @Override
    public Result authenticate(Command command) {
        return transaction.execute(status -> delegate.authenticate(command));
    }
}
