package com.buy01.common.security;

import java.time.Clock;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.buy01.common.security.jwt.JwtIssuer;
import com.buy01.common.security.jwt.JwtProperties;
import com.buy01.common.security.jwt.JwtVerifier;
import com.buy01.common.security.jwt.RsaKeys;

/**
 * Exposes JwtVerifier to every service and JwtIssuer to the service that owns the private key.
 * Authentication filters are not beans: each SecurityConfig instantiates the one matching its stack,
 * so a filter is never registered twice (once by the container, once in the security chain).
 */
@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
public class CommonSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtVerifier jwtVerifier(JwtProperties properties) {
        return new JwtVerifier(RsaKeys.publicKey(properties.publicKey()));
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "security.jwt", name = "private-key")
    public JwtIssuer jwtIssuer(JwtProperties properties) {
        return new JwtIssuer(RsaKeys.privateKey(properties.privateKey()), properties.expiration(), Clock.systemUTC());
    }
}
