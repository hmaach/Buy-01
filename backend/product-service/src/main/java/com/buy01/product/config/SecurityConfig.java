package com.buy01.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

import reactor.core.publisher.Flux;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity // ← enables @PreAuthorize in reactive world
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/products", "/products/**").permitAll()
                        .anyExchange().authenticated());

        // .oauth2ResourceServer(oauth2 -> oauth2
        // .jwt(jwt -> jwt
        // .jwtAuthenticationConverter(jwtAuthenticationConverter())
        // )
        // );

        return http.build();
    }

    // @Bean
    // public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
    // ReactiveJwtAuthenticationConverter converter = new
    // ReactiveJwtAuthenticationConverter();

    // converter.setJwtGrantedAuthoritiesConverter(jwt -> {
    // return Flux.empty(); // replace with real impl
    // });

    // return converter;
    // }
}