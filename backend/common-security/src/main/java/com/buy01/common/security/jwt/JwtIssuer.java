package com.buy01.common.security.jwt;

import java.security.PrivateKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import io.jsonwebtoken.Jwts;

/**
 * Signs RS256 access tokens. Only the user-service holds the private key, so only it gets this bean.
 */
public class JwtIssuer {

    private final PrivateKey privateKey;
    private final Duration ttl;
    private final Clock clock;

    public JwtIssuer(PrivateKey privateKey, Duration ttl, Clock clock) {
        this.privateKey = privateKey;
        this.ttl = ttl;
        this.clock = clock;
    }

    public IssuedToken issue(String subject, String email, String role) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(ttl);
        String token = Jwts.builder()
                .subject(subject)
                .claim(JwtClaims.EMAIL, email)
                .claim(JwtClaims.ROLE, role)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
        return new IssuedToken(token, expiresAt);
    }

    public record IssuedToken(String token, Instant expiresAt) {
    }
}
