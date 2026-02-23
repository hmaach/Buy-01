package com.buy01.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll()
                // .pathMatchers("/products", "/products/**").permitAll()
                );

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