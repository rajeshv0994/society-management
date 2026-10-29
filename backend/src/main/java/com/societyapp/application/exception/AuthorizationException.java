package com.societyapp.application.exception;

/** Framework-independent authorization failure raised by business rules. */
public class AuthorizationException extends RuntimeException {
    public AuthorizationException(String message) {
        super(message);
    }
}
