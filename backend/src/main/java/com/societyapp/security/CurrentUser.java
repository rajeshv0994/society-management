package com.societyapp.security;

import io.jsonwebtoken.Claims;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {
    private CurrentUser() {}

    private static Claims c() {
        return (Claims) SecurityContextHolder.getContext().getAuthentication().getDetails();
    }

    public static UUID id() {
        return UUID.fromString(c().getSubject());
    }

    public static UUID societyId() {
        var v = c().get("societyId", String.class);
        return v == null ? null : UUID.fromString(v);
    }

    public static String role() {
        return c().get("role", String.class);
    }
}
