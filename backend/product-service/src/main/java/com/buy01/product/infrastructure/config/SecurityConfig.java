package com.buy01.product.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.buy01.common.security.jwt.JwtVerifier;
import com.buy01.common.security.web.ReactiveJwtAuthenticationFilter;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final JwtVerifier jwtVerifier;

    public SecurityConfig(JwtVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        // Docker healthcheck calls this without a token
                        .pathMatchers("/actuator/health").permitAll()
                        .pathMatchers("/products/user").authenticated()
                        .pathMatchers(HttpMethod.GET, "/products/**").permitAll()
                        .pathMatchers("/products/**").hasRole("SELLER")
                        .anyExchange().authenticated())
                .addFilterAt(new ReactiveJwtAuthenticationFilter(jwtVerifier), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
