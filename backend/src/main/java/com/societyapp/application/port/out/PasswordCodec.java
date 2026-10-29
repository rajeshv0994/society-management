package com.societyapp.application.port.out;

public interface PasswordCodec {
    String encode(String rawValue);

    boolean matches(String rawValue, String encodedValue);
}
