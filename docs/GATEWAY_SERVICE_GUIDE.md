# Gateway Service - Complete Setup Guide

## 🎯 What is the Gateway?

The **API Gateway** is the **single entry point** for all client requests. It:

- ✅ Routes requests to the right microservice
- ✅ Handles authentication (validates JWT tokens)
- ✅ Manages CORS
- ✅ Provides rate limiting (optional)
- ✅ Centralizes cross-cutting concerns

```
Client Request
     ↓
API Gateway (Port 8080)
     ↓
├─→ User Service (Port 8081)
├─→ Product Service (Port 8082)
└─→ Media Service (Port 8083)
```

---

## 📁 Project Structure

```
gateway-service/
├── src/main/java/com/ecommerce/gateway/
│   ├── GatewayApplication.java
│   │
│   ├── config/
│   │   ├── GatewayConfig.java          # Route configuration
│   │   └── CorsConfig.java             # CORS configuration
│   │
│   ├── filter/
│   │   ├── AuthenticationFilter.java   # JWT validation
│   │   └── LoggingFilter.java          # Request/response logging
│   │
│   └── exception/
│       └── GlobalErrorHandler.java     # Error handling
│
├── src/main/resources/
│   ├── application.yml
│   └── application-dev.yml
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 🚀 Quick Start

### Step 1: Create the Maven Project

```bash
cd ecommerce-microservices
mkdir gateway-service
cd gateway-service
```

Create `pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.ecommerce</groupId>
    <artifactId>gateway-service</artifactId>
    <version>1.0.0</version>
    <name>gateway-service</name>
    <description>API Gateway for E-commerce Microservices</description>

    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
    </properties>

    <dependencies>
        <!-- Spring Cloud Gateway -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>

        <!-- Eureka Client (Service Discovery) -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <!-- JWT for token validation -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.11.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.11.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.11.5</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Actuator for health checks -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Lombok (optional) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

### Step 2: Create Directory Structure

```bash
mkdir -p src/main/java/com/ecommerce/gateway/{config,filter,exception}
mkdir -p src/main/resources
mkdir -p src/test/java/com/ecommerce/gateway
```

---

## 📄 Source Files

### 1. Main Application Class

**File:** `src/main/java/com/ecommerce/gateway/GatewayApplication.java`

```java
package com.ecommerce.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

---

### 2. Gateway Route Configuration

**File:** `src/main/java/com/ecommerce/gateway/config/GatewayConfig.java`

```java
package com.ecommerce.gateway.config;

import com.ecommerce.gateway.filter.AuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final AuthenticationFilter authenticationFilter;

    public GatewayConfig(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

            // ============================================
            // USER SERVICE ROUTES
            // ============================================

            // Public routes (no authentication)
            .route("user-service-public", r -> r
                .path("/auth/**")
                .uri("lb://user-service"))  // lb = load balanced via Eureka

            // Protected routes (require authentication)
            .route("user-service-protected", r -> r
                .path("/me")
                .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                .uri("lb://user-service"))

            // ============================================
            // PRODUCT SERVICE ROUTES
            // ============================================

            // Public routes (anyone can view products)
            .route("product-service-public", r -> r
                .path("/products", "/products/{id}")
                .and()
                .method("GET")
                .uri("lb://product-service"))

            // Seller-only routes (create/update/delete products)
            .route("product-service-seller", r -> r
                .path("/products", "/products/**")
                .and()
                .method("POST", "PUT", "DELETE")
                .filters(f -> f.filter(authenticationFilter.apply(
                    new AuthenticationFilter.Config("SELLER"))))
                .uri("lb://product-service"))

            // ============================================
            // MEDIA SERVICE ROUTES
            // ============================================

            // Public routes (view images)
            .route("media-service-public", r -> r
                .path("/media/images/{id}")
                .and()
                .method("GET")
                .uri("lb://media-service"))

            // Seller-only routes (upload/delete images)
            .route("media-service-seller", r -> r
                .path("/media/images", "/media/images/**")
                .and()
                .method("POST", "DELETE")
                .filters(f -> f.filter(authenticationFilter.apply(
                    new AuthenticationFilter.Config("SELLER"))))
                .uri("lb://media-service"))

            .build();
    }
}
```

---

### 3. CORS Configuration

**File:** `src/main/java/com/ecommerce/gateway/config/CorsConfig.java`

```java
package com.ecommerce.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Allowed origins (frontend URLs)
        corsConfig.setAllowedOrigins(List.of(
            "http://localhost:4200",      // Angular dev server
            "http://localhost:3000",      // React dev server (if needed)
            "https://your-production-domain.com"
        ));

        // Allowed HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // Allowed headers
        corsConfig.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "X-Requested-With"
        ));

        // Allow credentials (cookies, authorization headers)
        corsConfig.setAllowCredentials(true);

        // Max age for preflight requests (in seconds)
        corsConfig.setMaxAge(3600L);

        // Expose headers to the client
        corsConfig.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
```

---

### 4. Authentication Filter (JWT Validation)

**File:** `src/main/java/com/ecommerce/gateway/filter/AuthenticationFilter.java`

```java
package com.ecommerce.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Check if Authorization header exists
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                // Validate and extract claims from JWT
                Claims claims = validateToken(token);

                // Check role if required
                if (config.getRequiredRole() != null) {
                    String userRole = claims.get("role", String.class);
                    if (!config.getRequiredRole().equals(userRole)) {
                        return onError(exchange, "Insufficient permissions", HttpStatus.FORBIDDEN);
                    }
                }

                // Add user info to headers for downstream services
                ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", claims.getSubject())
                    .header("X-User-Role", claims.get("role", String.class))
                    .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                return onError(exchange, "Invalid or expired token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
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
```

---

### 5. Logging Filter (Optional but Useful)

**File:** `src/main/java/com/ecommerce/gateway/filter/LoggingFilter.java`

```java
package com.ecommerce.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        logger.info("========== Incoming Request ==========");
        logger.info("Method: {}", request.getMethod());
        logger.info("Path: {}", request.getPath());
        logger.info("Headers: {}", request.getHeaders());
        logger.info("======================================");

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            logger.info("========== Outgoing Response ==========");
            logger.info("Status: {}", exchange.getResponse().getStatusCode());
            logger.info("=======================================");
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;  // Execute first
    }
}
```

---

### 6. Global Error Handler

**File:** `src/main/java/com/ecommerce/gateway/exception/GlobalErrorHandler.java`

```java
package com.ecommerce.gateway.exception;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String errorMessage = String.format(
            "{\"error\": \"Internal server error\", \"message\": \"%s\", \"status\": 500}",
            ex.getMessage()
        );

        byte[] bytes = errorMessage.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
```

---

### 7. Application Configuration

**File:** `src/main/resources/application.yml`

```yaml
spring:
  application:
    name: gateway-service

  cloud:
    gateway:
      # Enable discovery locator (automatic route creation from Eureka)
      discovery:
        locator:
          enabled: false # We define routes manually in GatewayConfig
          lower-case-service-id: true

      # Global CORS configuration (handled by CorsConfig bean)
      globalcors:
        add-to-simple-url-handler-mapping: true

server:
  port: 8080

# JWT Configuration (must match User Service)
jwt:
  secret: your-secret-key-change-this-in-production-make-it-at-least-256-bits

# Eureka Client Configuration
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    preferIpAddress: true
    instance-id: ${spring.application.name}:${random.value}

# Actuator endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,gateway
  endpoint:
    health:
      show-details: always
    gateway:
      enabled: true

# Logging
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    com.ecommerce.gateway: DEBUG
    reactor.netty: INFO
```

---

**File:** `src/main/resources/application-dev.yml`

```yaml
# Development-specific configuration
logging:
  level:
    org.springframework.cloud.gateway: TRACE
    com.ecommerce.gateway: TRACE

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

---

### 8. Dockerfile

**File:** `Dockerfile`

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built jar
COPY target/gateway-service-*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

---

### 10. Test File

**File:** `src/test/java/com/ecommerce/gateway/GatewayApplicationTests.java`

```java
package com.ecommerce.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GatewayApplicationTests {

    @Test
    void contextLoads() {
        // Verify Spring context loads successfully
    }
}
```

---

## 🚀 Build and Run

```bash
# Build the project
mvn clean package

# Run locally
mvn spring-boot:run

# Build Docker image
docker build -t ecommerce/gateway-service:latest .

# Run in Docker
docker run -p 8080:8080 \
  -e EUREKA_URL=http://eureka-server:8761/eureka \
  ecommerce/gateway-service:latest
```

---

## 📊 Testing the Gateway

### 1. Check Health

```bash
curl http://localhost:8080/actuator/health
```

### 2. View Routes

```bash
curl http://localhost:8080/actuator/gateway/routes
```

### 3. Test Authentication

```bash
# Should fail (no token)
curl http://localhost:8080/me

# Should succeed (with valid token)
curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/me
```

---

## ⚙️ Key Configuration Points

1. **JWT Secret:** Must match User Service exactly
2. **Eureka URL:** Point to Discovery Service
3. **CORS Origins:** Add your frontend URLs
4. **Route Order:** More specific routes should come before generic ones

---

## 🔧 Troubleshooting

**Problem:** "Service not found"

- ✅ Check if service is registered in Eureka: `http://localhost:8761`

**Problem:** "CORS error"

- ✅ Add your frontend URL to `CorsConfig.java`

**Problem:** "Invalid token"

- ✅ Verify JWT secret matches User Service
- ✅ Check token hasn't expired
