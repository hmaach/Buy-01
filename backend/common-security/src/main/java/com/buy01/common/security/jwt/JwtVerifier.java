package com.buy01.common.security.jwt;

import java.security.PublicKey;
import java.util.Optional;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

/**
 * Verifies RS256 access tokens with the platform public key. Every service, including the gateway, uses this class.
 */
public class JwtVerifier {

    private final JwtParser parser;

    public JwtVerifier(PublicKey publicKey) {
        this.parser = Jwts.parser().verifyWith(publicKey).build();
    }

    /**
     * Parses and verifies the token once. Returns empty for any invalid, expired, tampered or incomplete token.
     */
    public Optional<AuthenticatedUser> verify(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims = parser.parseSignedClaims(token).getPayload();
            String subject = claims.getSubject();
            String role = claims.get(JwtClaims.ROLE, String.class);
            if (subject == null || role == null) {
                return Optional.empty();
            }
            return Optional.of(new AuthenticatedUser(subject, claims.get(JwtClaims.EMAIL, String.class), role));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
