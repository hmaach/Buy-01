package com.buy01.common.security.web;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.buy01.common.security.jwt.JwtVerifier;

import reactor.core.publisher.Mono;

/**
 * WebFlux counterpart of JwtAuthenticationFilter, registered at SecurityWebFiltersOrder.AUTHENTICATION.
 */
public class ReactiveJwtAuthenticationFilter implements WebFilter {

    private final JwtVerifier verifier;

    public ReactiveJwtAuthenticationFilter(JwtVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return BearerToken.from(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .flatMap(verifier::verify)
                .map(user -> chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                        new UsernamePasswordAuthenticationToken(user, null, user.authorities()))))
                .orElseGet(() -> chain.filter(exchange));
    }
}
