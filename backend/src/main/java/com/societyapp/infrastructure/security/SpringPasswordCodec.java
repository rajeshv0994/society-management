package com.societyapp.infrastructure.security;

import com.societyapp.application.port.out.PasswordCodec;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SpringPasswordCodec implements PasswordCodec {
    private final PasswordEncoder encoder;

    public SpringPasswordCodec(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public String encode(String rawValue) {
        return encoder.encode(rawValue);
    }

    @Override
    public boolean matches(String rawValue, String encodedValue) {
        return encoder.matches(rawValue, encodedValue);
    }
}
