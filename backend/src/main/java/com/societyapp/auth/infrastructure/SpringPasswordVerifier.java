package com.societyapp.auth.infrastructure;

import com.societyapp.auth.application.port.out.PasswordVerifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SpringPasswordVerifier implements PasswordVerifier {
    private final PasswordEncoder encoder;

    public SpringPasswordVerifier(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
