package com.societyapp.security;

import com.societyapp.auth.application.port.out.AccessTokenIssuer;
import com.societyapp.auth.domain.UserAccount;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService implements AccessTokenIssuer {
    private final byte[] secret;
    private final long expiration;

    public JwtService(
            @Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expiration) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT secret must contain at least 32 bytes");
        }
        if (expiration <= 0) {
            throw new IllegalArgumentException("JWT expiration must be positive");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expiration = expiration;
    }

    public String issue(UUID userId, String email, String role, UUID societyId) {
        var claims = new HashMap<String, Object>();
        claims.put("role", role);
        if (societyId != null) claims.put("societyId", societyId.toString());
        var now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setId(UUID.randomUUID().toString())
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration))
                .signWith(Keys.hmacShaKeyFor(secret), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String issue(UserAccount account) {
        return issue(account.id(), account.email(), account.role(), account.societyId());
    }

    @Override
    public long expiresInSeconds() {
        return expiration / 1000;
    }

    public <T> T claim(String token, Function<Claims, T> resolver) {
        return resolver.apply(parse(token));
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
