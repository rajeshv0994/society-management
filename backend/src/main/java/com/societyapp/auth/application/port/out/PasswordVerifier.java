package com.societyapp.auth.application.port.out;

public interface PasswordVerifier {
    boolean matches(String rawPassword, String encodedPassword);
}
