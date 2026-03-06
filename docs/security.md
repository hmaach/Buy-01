# Security Documentation

## Overview

The Buy-01 platform implements comprehensive security measures including JWT-based authentication, role-based access control, API Gateway filtering, and secure communication between services.

## Security Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Requests                           │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API Gateway (8080)                          │
│  • JWT Token Validation                                          │
│  • Rate Limiting                                                 │
│  • Request Logging                                                │
└─────────────────────────────────────────────────────────────────┘
                                │
                    ┌───────────┴───────────┐
                    ▼                       ▼
            Protected Routes          Public Routes
                    │                       │
                    ▼                       ▼
        ┌──────────────────┐     ┌──────────────────┐
        │ Auth Required    │     │ No Auth Required │
        │ - /products     │     │ - /auth/login    │
        │ - /products/{id}│     │ - /auth/register │
        │ - /media        │     │ - /products      │
        │ - /me           │     │ - /media/{id}    │
        └──────────────────┘     └──────────────────┘
```

## Authentication

### JWT (JSON Web Tokens)

The system uses JWT for stateless authentication:

1. **Token Generation**: User Service generates JWT tokens on successful login
2. **Token Validation**: API Gateway validates tokens on each request
3. **Token Propagation**: Valid tokens are passed to backend services

### Token Structure

```jwt
Header:
{
  "alg": "RS256",
  "typ": "JWT"
}

Payload:
{
  "sub": "user-uuid",
  "email": "john@example.com",
  "role": "SELLER",
  "iat": 1704067200,
  "exp": 1704070800
}
```

### Token Configuration

| Property | Environment Variable | Default | Description |
|----------|---------------------|---------|-------------|
| JWT Private Key | JWT_PRIVATE_KEY | - | Private key for signing tokens |
| JWT Public Key | JWT_PUBLIC_KEY | - | Public key for verifying tokens |
| Token Expiration | JWT_EXPIRATION | 3600000 | Access token validity (ms) |
| Refresh Expiration | JWT_REFRESH_EXPIRATION | 86400000 | Refresh token validity (ms) |

---

## Authorization

### Roles

The system supports two roles:

| Role | Description | Permissions |
|------|-------------|-------------|
| CLIENT | Standard customer | View products, view images, manage own profile |
| SELLER | Merchant/seller | All CLIENT permissions + create/update/delete products, upload images |

### Role-Based Access Control (RBAC)

#### API Gateway Level
Routes are protected based on URL patterns:

```yaml
- id: product-service
  uri: lb://product-service
  predicates:
    - Path=/products/**
  filters:
    - AuthenticationGatewayFilterFactory
```

#### Service Level
Endpoints are secured using `@PreAuthorize` annotations:

```java
@PreAuthorize("hasRole('SELLER')")
@PostMapping
public Mono<ResponseEntity<?>> createProduct(...) { }

@PreAuthorize("hasRole('SELLER') or hasRole('CLIENT')")
@GetMapping
public Mono<ResponseEntity<?>> getProducts(...) { }
```

---

## API Gateway Security

### Authentication Filter

The API Gateway includes an authentication filter (`AuthenticationGatewayFilterFactory`) that:

1. Extracts JWT token from Authorization header
2. Validates token using public key
3. Extracts user information (ID, email, role)
4. Adds user info to request headers for downstream services

### Rate Limiting

Rate limiting is implemented using Bucket4j:

```java
@Value("${gateway.rate-limit.capacity:100}")
private int capacity;

@Value("${gateway.rate-limit.refill-minutes:1}")
private int refillDurationMinutes;
```

**Configuration:**
| Property | Default | Description |
|----------|---------|-------------|
| gateway.rate-limit.capacity | 100 | Maximum requests allowed |
| gateway.rate-limit.refill-minutes | 1 | Refill interval (minutes) |
| gateway.rate-limit.enabled | true | Enable/disable rate limiting |

### CORS Configuration

CORS is configured at the API Gateway:

```java
@Bean
public CorsWebFilter corsWebFilter() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(Arrays.asList("*"));
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(Arrays.asList("*"));
    config.setExposedHeaders(Arrays.asList("Authorization", "X-RateLimit-Limit", "X-RateLimit-Remaining"));
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    
    return new CorsWebFilter(source);
}
```

---

## Service-Level Security

### User Service

Uses Spring Security with JWT authentication filter:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

### Product Service (Reactive)

Uses Spring Security WebFlux with reactive JWT filter:

```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchange -> exchange
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/products/**").permitAll()
                .pathMatchers("/products/**").hasRole("SELLER")
                .anyExchange().authenticated()
            )
            .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build();
    }
}
```

### Media Service (Reactive)

Similar configuration to Product Service with role-based access.

---

## Password Security

### Password Encryption

Passwords are encrypted using BCrypt:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**Best Practices:**
- Minimum 8 characters required
- BCrypt automatically generates salt
- Work factor: 10 (default)

---

## Secure Communication

### Inter-Service Communication

Services communicate through:
- **REST**: Through API Gateway (authenticated)
- **Kafka**: For event-driven communication

### Service Discovery

Services register with Eureka and discover each other dynamically:

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://discovery-server:8761/eureka
  instance:
    prefer-ip-address: true
```

---

## Security Headers

The API Gateway adds security headers to responses:

```java
exchange.getResponse().getHeaders().add("X-Content-Type-Options", "nosniff");
exchange.getResponse().getHeaders().add("X-Frame-Options", "DENY");
exchange.getResponse().getHeaders().add("X-XSS-Protection", "1; mode=block");
```

---

## Error Handling

### Authentication Errors

| Error | HTTP Status | Description |
|-------|-------------|-------------|
| Invalid Token | 401 | Token is malformed or expired |
| Missing Token | 401 | No Authorization header |
| Insufficient Permissions | 403 | Valid token but wrong role |

### Error Response Format

```json
{
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "Invalid or expired token"
}
```

---

## Environment Variables

### Required Security Variables

| Variable | Description | Example |
|----------|-------------|---------|
| JWT_PRIVATE_KEY | RSA Private Key (PEM format) | -----BEGIN RSA PRIVATE KEY-----\n... |
| JWT_PUBLIC_KEY | RSA Public Key (PEM format) | -----BEGIN PUBLIC KEY-----\n... |
| JWT_EXPIRATION | Token validity in milliseconds | 3600000 |
| JWT_REFRESH_EXPIRATION | Refresh token validity (ms) | 86400000 |

---

## Best Practices Implemented

1. **Stateless Authentication**: JWT tokens enable stateless, scalable authentication
2. **Role-Based Access**: Clear separation between CLIENT and SELLER permissions
3. **API Gateway Centralization**: Single entry point for auth, logging, rate limiting
4. **Password Hashing**: BCrypt with salt for secure password storage
5. **HTTPS Ready**: Can be configured with TLS/SSL
6. **CORS Configuration**: Controlled cross-origin access
7. **Rate Limiting**: Prevents abuse and DDoS attacks
8. **Request Logging**: Audit trail for security monitoring

---

## Future Security Enhancements

- Implement OAuth2/OpenID Connect
- Add mTLS for inter-service communication
- Implement API key rotation
- Add more granular permissions
- Implement IP whitelisting
