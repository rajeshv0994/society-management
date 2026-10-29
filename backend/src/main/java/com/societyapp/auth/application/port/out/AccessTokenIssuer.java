package com.societyapp.auth.application.port.out;

import com.societyapp.auth.domain.UserAccount;

public interface AccessTokenIssuer {
    String issue(UserAccount account);

    long expiresInSeconds();
}
