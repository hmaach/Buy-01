package com.buy01.common.security.jwt;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

/**
 * Binds the existing security.jwt.* keys. expiration keeps its historical unit (milliseconds).
 */
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        String publicKey,
        String privateKey,
        @DurationUnit(ChronoUnit.MILLIS) Duration expiration) {

    private static final Duration DEFAULT_EXPIRATION = Duration.ofHours(1);

    public JwtProperties {
        if (expiration == null) {
            expiration = DEFAULT_EXPIRATION;
        }
    }

    public boolean canIssue() {
        return privateKey != null && !privateKey.isBlank();
    }
}
