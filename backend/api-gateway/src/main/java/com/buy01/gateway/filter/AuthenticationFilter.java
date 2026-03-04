package com.buy01.gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.buy01.gateway.security.JwtUtil;

import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Check if Authorization header exists
            if (!request.getHeaders().containsHeader(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                // Validate token using JwtUtil
                if (!jwtUtil.validateToken(token)) {
                    return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
                }

                String userId = jwtUtil.extractUserId(token);
                String role = jwtUtil.extractRole(token);

                // Check role if required
                if (config.getRequiredRole() != null) {
                    if (!config.getRequiredRole().equals(role)) {
                        return onError(exchange, "Insufficient permissions", HttpStatus.FORBIDDEN);
                    }
                }

                // Add user info to headers for downstream services
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Role", role)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                return onError(exchange, "Invalid or expired token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json");

        String errorJson = String.format(
                "{\"error\": \"%s\", \"status\": %d}",
                error,
                httpStatus.value()
        );

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(errorJson.getBytes()))
        );
    }

    // Configuration class
    public static class Config {

        private String requiredRole;

        public Config() {
        }

        public Config(String requiredRole) {
            this.requiredRole = requiredRole;
        }

        public String getRequiredRole() {
            return requiredRole;
        }

        public void setRequiredRole(String requiredRole) {
            this.requiredRole = requiredRole;
        }
    }
}
