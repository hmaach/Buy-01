// package com.buy01.gateway.filter;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.security.Keys;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.cloud.gateway.filter.GatewayFilter;
// import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.server.reactive.ServerHttpRequest;
// import org.springframework.http.server.reactive.ServerHttpResponse;
// import org.springframework.stereotype.Component;
// import org.springframework.web.server.ServerWebExchange;
// import reactor.core.publisher.Mono;

// import javax.crypto.SecretKey;
// import java.nio.charset.StandardCharsets;

// @Component
// public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

//     @Value("${jwt.secret}")
//     private String jwtSecret;

//     public AuthenticationFilter() {
//         super(Config.class);
//     }

//     @Override
//     public GatewayFilter apply(Config config) {
//         return (exchange, chain) -> {
//             ServerHttpRequest request = exchange.getRequest();

//             // Check if Authorization header exists
//             if (!request.getHeaders().containsHeader(HttpHeaders.AUTHORIZATION)) {
//                 return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
//             }

//             String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

//             if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//                 return onError(exchange, "Invalid authorization header", HttpStatus.UNAUTHORIZED);
//             }

//             String token = authHeader.substring(7);

//             try {
//                 // Validate and extract claims from JWT
//                 Claims claims = validateToken(token);

//                 // Check role if required
//                 if (config.getRequiredRole() != null) {
//                     String userRole = claims.get("role", String.class);
//                     if (!config.getRequiredRole().equals(userRole)) {
//                         return onError(exchange, "Insufficient permissions", HttpStatus.FORBIDDEN);
//                     }
//                 }

//                 // Add user info to headers for downstream services
//                 ServerHttpRequest modifiedRequest = request.mutate()
//                         .header("X-User-Id", claims.getSubject())
//                         .header("X-User-Role", claims.get("role", String.class))
//                         .build();

//                 return chain.filter(exchange.mutate().request(modifiedRequest).build());

//             } catch (Exception e) {
//                 return onError(exchange, "Invalid or expired token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
//             }
//         };
//     }

//     private Claims validateToken(String token) {
//         SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

//         return Jwts.parserBuilder()
//                 .setSigningKey(key)
//                 .build()
//                 .parseClaimsJws(token)
//                 .getBody();
//     }

//     private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus httpStatus) {
//         ServerHttpResponse response = exchange.getResponse();
//         response.setStatusCode(httpStatus);
//         response.getHeaders().add("Content-Type", "application/json");

//         String errorJson = String.format(
//                 "{\"error\": \"%s\", \"status\": %d}",
//                 error,
//                 httpStatus.value()
//         );

//         return response.writeWith(
//                 Mono.just(response.bufferFactory().wrap(errorJson.getBytes()))
//         );
//     }

//     // Configuration class
//     public static class Config {

//         private String requiredRole;

//         public Config() {
//         }

//         public Config(String requiredRole) {
//             this.requiredRole = requiredRole;
//         }

//         public String getRequiredRole() {
//             return requiredRole;
//         }

//         public void setRequiredRole(String requiredRole) {
//             this.requiredRole = requiredRole;
//         }
//     }
// }
