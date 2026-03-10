package com.buy01.gateway.filter;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import com.buy01.gateway.security.JwtUtil;

import reactor.core.publisher.Mono;

record PublicEndpoint(HttpMethod method, String pattern) {
    public boolean matches(HttpMethod reqMethod, String reqPath) {
        if (method != null && method != reqMethod) {
            return false;
        }

        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return reqPath.startsWith(prefix);
        }

        return pattern.equals(reqPath);
    }
}

@Component
public class AuthenticationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    private final WebClient webClient;

    private static final List<PublicEndpoint> PUBLIC_ENDPOINTS = List.of(
            new PublicEndpoint(HttpMethod.POST, "/users/auth/login"),
            new PublicEndpoint(HttpMethod.POST, "/users/auth/register"),
            new PublicEndpoint(null, "/media/**"),
            new PublicEndpoint(HttpMethod.GET, "/products"),
            new PublicEndpoint(HttpMethod.GET, "/products/**"));

    public AuthenticationGatewayFilterFactory() {
        super(Config.class);
        this.webClient = WebClient.builder().build();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            HttpMethod method = request.getMethod();

            if (HttpMethod.OPTIONS.equals(method)) {
                return chain.filter(exchange);
            }

            String path = request.getURI().getPath();
            boolean isPublic = PUBLIC_ENDPOINTS.stream()
                    .anyMatch(endpoint -> endpoint.matches(method, path));

            if (isPublic) {
                return chain.filter(exchange);
            }
            
            // 1. Check for Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Missing or invalid authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                // 2. Validate token
                if (!jwtUtil.validateToken(token)) {
                    return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
                }

                String userId = jwtUtil.extractUserId(token);
                String role = jwtUtil.extractRole(token);

                // 3. Optional Role check
                if (config.getRequiredRole() != null && !config.getRequiredRole().isEmpty()) {
                    if (!config.getRequiredRole().equals(role)) {
                        return onError(exchange, "Insufficient permissions", HttpStatus.FORBIDDEN);
                    }
                }

                // 4. Check if user exists in user-service
                return webClient.get()
                        .uri("http://USER-SERVICE/users/exists/" + userId)
                        .retrieve()
                        .bodyToMono(Boolean.class)
                        .flatMap(exists -> {
                            if (!exists) {
                                return onError(exchange, "User not found or has been deleted", HttpStatus.UNAUTHORIZED);
                            }
                            
                            // 5. Mutate request to pass info downstream
                            ServerHttpRequest modifiedRequest = request.mutate()
                                    .header("X-User-Id", userId)
                                    .header("X-User-Role", role)
                                    .build();

                            return chain.filter(exchange.mutate().request(modifiedRequest).build());
                        })
                        .onErrorResume(e -> {
                            // If user-service is unavailable, allow request to proceed but log warning
                            System.err.println("Warning: Could not verify user existence: " + e.getMessage());
                            ServerHttpRequest modifiedRequest = request.mutate()
                                    .header("X-User-Id", userId)
                                    .header("X-User-Role", role)
                                    .build();
                            return chain.filter(exchange.mutate().request(modifiedRequest).build());
                        });

            } catch (Exception e) {
                return onError(exchange, "Authentication failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");

        String path = exchange.getRequest().getURI().getPath();
        String title = httpStatus == HttpStatus.UNAUTHORIZED ? "Unauthorized" : "Forbidden";

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, error);
        problemDetail.setTitle(title);
        problemDetail.setInstance(URI.create(path));

        // TODO: return just problem detail
        String errorJson = String.format(
                "{\"detail\": \"%s\", \"instance\": \"%s\", \"status\": %d, \"title\": \"%s\"}",
                error, path, httpStatus.value(), title);
        byte[] bytes = errorJson.getBytes(StandardCharsets.UTF_8);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    public static class Config {

        private String requiredRole;

        public Config() {
        }

        public String getRequiredRole() {
            return requiredRole;
        }

        public void setRequiredRole(String requiredRole) {
            this.requiredRole = requiredRole;
        }
    }
}
