package com.buy01.common.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.buy01.common.security.TestKeys;

import io.jsonwebtoken.Jwts;

class JwtVerifierTest {

    private final JwtIssuer issuer = new JwtIssuer(TestKeys.PAIR.getPrivate(), Duration.ofDays(1), Clock.systemUTC());
    private final JwtVerifier verifier = new JwtVerifier(TestKeys.PAIR.getPublic());

    @Test
    void verifiesTokenIssuedWithMatchingKey() {
        JwtIssuer.IssuedToken issued = issuer.issue("user-1", "seller@buy01.test", "SELLER");

        assertThat(verifier.verify(issued.token()))
                .contains(new AuthenticatedUser("user-1", "seller@buy01.test", "SELLER"));
    }

    @Test
    void expiryFollowsConfiguredTtl() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        JwtIssuer fixed = new JwtIssuer(TestKeys.PAIR.getPrivate(), Duration.ofMinutes(15), Clock.fixed(now, ZoneOffset.UTC));

        assertThat(fixed.issue("u", "e", "CLIENT").expiresAt()).isEqualTo(now.plus(Duration.ofMinutes(15)));
    }

    @Test
    void rejectsExpiredToken() {
        Instant past = Instant.now().minus(Duration.ofHours(2));
        JwtIssuer expired = new JwtIssuer(TestKeys.PAIR.getPrivate(), Duration.ofMinutes(1), Clock.fixed(past, ZoneOffset.UTC));

        assertThat(verifier.verify(expired.issue("u", "e", "CLIENT").token())).isEmpty();
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        JwtIssuer foreign = new JwtIssuer(TestKeys.OTHER_PAIR.getPrivate(), Duration.ofDays(1), Clock.systemUTC());

        assertThat(verifier.verify(foreign.issue("u", "e", "SELLER").token())).isEmpty();
    }

    @Test
    void rejectsTamperedPayload() {
        String[] parts = issuer.issue("u", "e", "CLIENT").token().split("\\.");
        String forgedPayload = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"u\",\"role\":\"SELLER\"}".getBytes());

        assertThat(verifier.verify(parts[0] + "." + forgedPayload + "." + parts[2])).isEmpty();
    }

    @Test
    void rejectsTokenWithoutRole() {
        String token = Jwts.builder()
                .subject("u")
                .expiration(Date.from(Instant.now().plus(Duration.ofDays(1))))
                .signWith(TestKeys.PAIR.getPrivate(), Jwts.SIG.RS256)
                .compact();

        assertThat(verifier.verify(token)).isEmpty();
    }

    @Test
    void rejectsGarbageAndBlankInput() {
        assertThat(verifier.verify("not-a-jwt")).isEmpty();
        assertThat(verifier.verify("")).isEmpty();
        assertThat(verifier.verify(null)).isEmpty();
    }

    @Test
    void mapsRoleToSpringAuthority() {
        assertThat(new AuthenticatedUser("u", "e", "seller").authorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_SELLER");
    }
}
