# Review map: what to read to explain the whole project

Scan of branch `service-unit-tests` on 2026-11-13. Line counts are code lines only: blank lines, comment lines, `import` and `package` lines are not counted. Difficulty ratings and time estimates are judgments (marked as estimates), based on the size of each file, its number of branches (`if`, `catch`, `&&`...), its reactive operators (`flatMap`, `switchIfEmpty`...), and how much Spring, Kafka or security machinery it uses.

## Summary

| Scope | Files | Code lines |
|---|---|---|
| Backend Java (main) | 115 | 2,954 |
| Backend Java (tests) | 12 | 281 |
| Frontend TypeScript | 31 | 1,375 |
| Frontend HTML templates | 10 | 915 |
| Build, Docker, config, scripts | 30 | 1,658 |
| **To review** | **198** | **7,183** |
| Not to review: CSS (11 files) | 11 | 2,769 |
| Not to review: Markdown docs (25 files) | 25 | 3,500 |
| Not counted at all | | `package-lock.json`, Maven wrapper, images, `uploads/`, `infra/` (not in git yet) |

CSS is presentation only, and the docs are reference material, so neither needs a line-by-line review.

## Difficulty levels

| Level | Meaning | Reading pace (estimate) |
|---|---|---|
| Easy | Records, DTOs, enums, mappers, simple adapters, plain config. Read once, explain in one sentence | about 400 lines per hour |
| Medium | Business rules with branches, Spring configuration, Kafka setup, exception handlers, Angular components with forms | about 150 lines per hour |
| Hard | Reactive chains, security and JWT, the gateway filters, events that cross services, the compose start-up chain, the Angular auth flow | about 60 lines per hour |

| Level | Files | Code lines | Time (estimate) |
|---|---|---|---|
| Hard | 24 | 1,474 | about 25 h |
| Medium | 54 | 2,721 | about 18 h |
| Easy | 120 | 2,988 | about 7 h |
| **Total** | **198** | **7,183** | **about 50 h** |

At about 19 hours a week (`decisions.md`), a full review is about 2.5 weeks. The "core path" at the end of this file covers the audit-critical part in about half of that.

## Hard files (24 files, 1,474 lines)

### Backend (14 files, 645 lines)

| File | Lines | Why it is hard |
|---|---|---|
| `api-gateway/.../filter/AuthenticationGatewayFilterFactory.java` | 113 | Gateway filter factory, public-route list, JWT check, a WebClient call that silently fails (known bug), reactive error handling |
| `api-gateway/.../filter/RateLimitFilter.java` | 71 | Bucket4j token buckets per client IP, `X-Forwarded-For`, a 429 response written by hand |
| `common-security/.../jwt/JwtVerifier.java` | 22 | RS256 signature check, claim extraction, why invalid tokens return `Optional.empty()` |
| `common-security/.../jwt/RsaKeys.java` | 26 | Base64 DER to `PublicKey` / `PrivateKey` (X.509 and PKCS#8 formats) |
| `common-security/.../jwt/JwtIssuer.java` | 25 | Token signing, claims, expiry, why only user-service gets this bean |
| `common-security/.../jwt/JwtProperties.java` | 18 | `@ConfigurationProperties` record, milliseconds unit, fail-fast validation |
| `common-security/.../CommonSecurityAutoConfiguration.java` | 15 | Spring Boot auto-configuration, `@ConditionalOnProperty`, `@ConditionalOnMissingBean` |
| `common-security/.../web/JwtAuthenticationFilter.java` | 15 | Servlet filter that fills the `SecurityContext` |
| `common-security/.../web/ReactiveJwtAuthenticationFilter.java` | 14 | Same idea for WebFlux: `contextWrite` instead of a thread-local context |
| `product-service/.../application/service/ProductServiceImpl.java` | 105 | Reactive chains (`flatMap`, `switchIfEmpty`, `zip`, `flatMapMany`), ownership checks, Kafka events |
| `product-service/.../application/service/ImageService.java` | 101 | WebClient to media-service through Eureka, `onStatus` error mapping, two Kafka templates, known `bodyToFlux(String)` bug |
| `product-service/.../web/controller/ProductController.java` | 60 | Reactive controller, `Mono<ResponseEntity<?>>`, keyset pagination by `beforeTime` |
| `media-service/.../messaging/MediaStatusListener.java` | 43 | Kafka listeners: PENDING to LINKED, product deletion (known bug) |
| `product-service/.../messaging/UserListener.java` | 17 | Deletion cascade: user-deleted event, then delete each product, a `.subscribe()` inside a listener |

### Frontend (7 files, 425 lines)

| File | Lines | Why it is hard |
|---|---|---|
| `core/auth/services/auth.service.ts` | 139 | Token storage, expiry, signals plus BehaviorSubject, `switchMap` login flow |
| `features/products/create-product/create-product.ts` | 178 | Reactive form, image upload before product creation, create and edit modes, signals |
| `core/auth/interceptors/error.interceptor.ts` | 50 | Global error handling, logout rules (401, 429, 404 on `/me`) |
| `core/auth/interceptors/auth.interceptor.ts` | 20 | Adds `/api` prefix and the bearer token to every request |
| `core/auth/guards/role.guard.ts`, `auth.guard.ts`, `guest.guard.ts` | 38 | Route protection, guard factory with roles |

### Infrastructure (3 files, 404 lines)

| File | Lines | Why it is hard |
|---|---|---|
| `docker-compose.yml` | 310 | YAML anchors, memory limits, JVM flags, healthchecks, `depends_on: service_healthy` chain, volumes, Kafka KRaft settings |
| `api-gateway/src/main/resources/application.yaml` | 65 | TLS keystore, `lb://` routes through Eureka, route filters, rate-limit settings |
| `frontend/nginx.conf` | 29 | TLS, `/api/` reverse proxy to the gateway, Angular fallback routing |

## Medium files (54 files, 2,721 lines)

### Backend (24 files, 1,144 lines)

| Group | Files | Lines |
|---|---|---|
| Application services: `MediaServiceImpl` (143), `AvatarServiceImpl` (72), `UserServiceImpl` (56), `AuthServiceImpl` (41), `OrphanedMediaCleanupJob` (26) | 5 | 338 |
| Domain with rules: `Product` (127, builder plus validation), `Email` (30), `Password` (27) | 3 | 184 |
| Kafka setup: `KafkaConsumerConfig` (product, media), `KafkaProducerConfig` (product, user) | 4 | 143 |
| Security configs: `SecurityConfig` in user, product, media, gateway | 4 | 77 |
| Error handling: `GlobalExceptionHandler` (product 96, media 69), gateway `GlobalErrorHandler` (70) | 3 | 235 |
| Controllers: `MediaController` (55), `UserController` (48) | 2 | 103 |
| Gateway and wiring: `CorsConfig` (28), `LoggingFilter` (22), product `BeanConfiguration` (14, `@LoadBalanced` WebClient) | 3 | 64 |

### Frontend (11 files, 834 lines)

`user-profile.ts` (193), `register.component.ts` (107), `product-detail.ts` (92), `login.component.ts` (81), `product.service.ts` (75, includes the `JSON.parse` workaround and fake rating data), `product-list.ts` (73), `navbar.component.ts` (66), `delete-account-dialog.component.ts` (63), `app.routes.ts` (58), `user.service.ts` and `media.service.ts` (26 together).

### Build, Docker, scripts (12 files, 483 lines)

`backend/pom.xml` (156, parent POM: dependency management, plugin management, enforcer), 3 Dockerfiles (62: multi-stage builds, non-root user, keystore generation), 4 service `application.yaml` files (128), `scripts/fresh-start.sh` (44), `generate-keys.sh` (37), `setup-ssl.sh` (17), `Makefile` (39).

### Tests (7 files, 260 lines)

The 5 common-security test classes (178, including reactive filter tests with `StepVerifier`), plus `AuthServiceImplTest` and `UserServiceImplTest` (79). See `docs/testing.md`.

## Easy files (120 files, 2,988 lines)

| Group | Files (about) | Lines (about) |
|---|---|---|
| Backend DTOs, records, commands, events, enums, exceptions | 45 | 450 |
| Backend mappers, Mongo documents and adapters, Spring Data repositories | 18 | 400 |
| Backend domain getters and setters (`User`, `Media`), application main classes | 8 | 215 |
| user-service `GlobalExceptionHandler` (187 lines, the same block repeated) | 1 | 187 |
| Small configs and empty placeholder classes (`JwtConverter`, `GatewayException`, `MongoConfig`...) | 15 | 100 |
| Frontend models, utils, `app.config.ts`, `main.ts` | 8 | 116 |
| Frontend HTML templates (skim: bindings and structure) | 10 | 915 |
| Module `pom.xml` files, `angular.json`, `package.json`, `tsconfig*.json`, `.env.example`, legacy per-service `compose.yaml`, `update-java-to-21.sh` | 15 | 771 |
| `contextLoads` tests and the commented-out `MongoConnectionTest` (being removed) | 5 | 21 |

## Suggested reading order (follows the request flow)

1. **How it runs:** `docker-compose.yml`, `backend/pom.xml`, `backend/Dockerfile`, the gateway Dockerfile, `frontend/nginx.conf`.
2. **A request from the browser:** `auth.interceptor.ts`, then `nginx.conf` `/api/`, then the gateway `application.yaml` routes, then `RateLimitFilter`, then `AuthenticationGatewayFilterFactory`, then a service `SecurityConfig` and the common-security filter.
3. **Login and tokens:** `AuthController`, then `AuthServiceImpl`, then `JwtTokenGeneratorAdapter`, then `JwtIssuer`; on every later request, `JwtVerifier`.
4. **Products and images:** `MediaController.uploadImages` and `MediaServiceImpl.uploadImage` (PENDING), then `ProductController`, `ProductServiceImpl.createProduct`, and the `images-linked` event to `MediaStatusListener` (LINKED). Then reading: `ImageService.getImagesBatch` (WebClient to media).
5. **Deletion cascade:** `UserServiceImpl.deleteUser`, then the `user-deleted` event, then `UserListener`, then `product-deleted`, then `MediaStatusListener.onProductDeleted` (known bug). Then `OrphanedMediaCleanupJob`.
6. **Frontend:** `app.routes.ts` and the guards, then `auth.service.ts`, `error.interceptor.ts`, then `create-product.ts` and `user-profile.ts`.
7. **Tests:** `docs/testing.md`, then the test classes.

## Core path for the audits (estimate: about 25 h)

If time is short, these cover what auditors ask most (pipeline, security, deployment, the main business flows): every Hard file (about 1,474 lines), the 5 application services (338 lines), the parent POM and the Dockerfiles (218 lines), and the tests (260 lines). That is about 2,300 lines in about 40 files. The rest can be explained from the patterns these files show (one mapper or DTO explains them all).
