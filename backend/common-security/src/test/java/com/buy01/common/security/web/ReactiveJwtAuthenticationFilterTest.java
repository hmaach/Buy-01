package com.buy01.common.security.web;

import java.time.Clock;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.WebFilterChain;

import com.buy01.common.security.TestKeys;
import com.buy01.common.security.jwt.AuthenticatedUser;
import com.buy01.common.security.jwt.JwtIssuer;
import com.buy01.common.security.jwt.JwtVerifier;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ReactiveJwtAuthenticationFilterTest {

    private final JwtIssuer issuer = new JwtIssuer(TestKeys.PAIR.getPrivate(), Duration.ofHours(1), Clock.systemUTC());
    private final ReactiveJwtAuthenticationFilter filter =
            new ReactiveJwtAuthenticationFilter(new JwtVerifier(TestKeys.PAIR.getPublic()));

    private final WebFilterChain captureAuthentication = exchange -> ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .doOnNext(auth -> exchange.getAttributes().put("auth", auth))
            .then();

    @Test
    void authenticatesValidBearerToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/products")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + issuer.issue("u1", "a@b.c", "CLIENT").token()));

        StepVerifier.create(filter.filter(exchange, captureAuthentication)).verifyComplete();

        Authentication auth = exchange.getAttribute("auth");
        org.assertj.core.api.Assertions.assertThat(auth).isNotNull();
        org.assertj.core.api.Assertions.assertThat(auth.getPrincipal())
                .isEqualTo(new AuthenticatedUser("u1", "a@b.c", "CLIENT"));
    }

    @Test
    void passesThroughWithoutAuthenticationWhenTokenMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/products"));
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, captureAuthentication)).verifyComplete();
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        org.assertj.core.api.Assertions.assertThat((Object) exchange.getAttribute("auth")).isNull();
    }
}
