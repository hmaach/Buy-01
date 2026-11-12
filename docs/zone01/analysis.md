# BUY-01 Technical Analysis

Snapshot date: 2026-11-12. Branch analysed: `jenkins` @ `5e73478` (Merge pull request #57 from hmaach/phase0-parent-pom). The analysis was read-only: no build was run and no dependencies were downloaded.
Conventions: **(inferred)** = deduced, not directly verified. **not found** = searched, absent. Secret values are never shown (`***`).

---

## Changes since last snapshot (2026-11-04 @ `85b3436`)

| Area | Change | Evidence |
|---|---|---|
| Build | 5 standalone Maven projects became one multi-module build. Parent and aggregator is `backend/pom.xml` (`com.buy01:buy01-backend`), with one Maven wrapper in `backend/` | `backend/pom.xml`, `backend/mvnw` |
| Versions | All modules are now on Spring Boot 3.5.16, Spring Cloud 2025.0.3 and Java 21 (they were on 3 different combinations). The enforcer plugin requires Java 21+ and Maven 3.9+ | `backend/pom.xml` |
| Naming | All modules use groupId `com.buy01`, and each artifactId equals its folder name (`eurika-server` → `discovery-server`) | module `pom.xml` files |
| Security | New `common-security` module (`JwtIssuer`, `JwtVerifier`, servlet and reactive filters, auto-configuration). It replaces 4 copies of `JwtUtil` and 3 copies of `JwtAuthenticationFilter`. jjwt went from 0.11.5 to 0.12.7 | `backend/common-security/` |
| Tests | 17 real unit tests in common-security. The service tests are unchanged (`contextLoads` only) | `backend/common-security/src/test/` |
| Docker | One shared `backend/Dockerfile` (`SERVICE` build arg) builds discovery, user, product and media. The gateway Dockerfile now uses `backend/` as its context and no longer has an `SSL_PASSWORD` default | `backend/Dockerfile`, `backend/api-gateway/Dockerfile` |
| Compose | Memory limits, log rotation, `JAVA_TOOL_OPTIONS`, root log level WARN, actuator limited to `health,info`, Mongo `--quiet` with a 0.25 GB cache, 60 s healthchecks, Kafka pinned to 4.1.0, **Jenkins removed** | `docker-compose.yml` |
| Fix | Media files now persist: the volume mount is `./uploads/media:/app/uploads` | `docker-compose.yml` |
| Docs | `docs/AI-CONTEXT.md`, `docs/cicd.md`, `docs/security_roadmap.md`, the 3 gateway guides and `HEXAGONAL_CHEAT_SHEET.md` were deleted. `docs/zone01/` (plan, decisions, subjects, audits, this file) and `CLAUDE.md` + `.claude/` were added | `docs/`, `.claude/` |
| Deploy incident | After the rebuild, Kafka 4.1.0 crashed on an old `kafka-data` volume (`metadata.version` 29 > supported 27). Fix: delete the volume. Whether it was deleted on the VM is not visible from the repo | server logs, 2026-11-11 |

Unchanged: every functional bug in section 10 except media persistence, the frontend, the secrets in git history, and the absence of a Jenkinsfile.

---

## 1. Overview

Buy-01 is a microservices e-commerce platform. Sellers register, upload product images and create, update or delete products. Clients and anonymous users browse the products. It has 5 Spring Boot services: gateway, Eureka, user, product and media. They use one MongoDB per service, communicate asynchronously through Kafka, and are fronted by an Angular 20 SPA served by nginx. All of it is orchestrated by `docker-compose.yml` on an ARM64 VM with 1 OCPU and 6 GB of RAM. In production, host nginx forwards `/app/` and `/api/` to this stack (`CLAUDE.md`, `docs/zone01/decisions.md`).

Current state:
- **Phase 0 (foundation) is in progress** (`docs/zone01/plan.md`). The parent POM, the version alignment, common-security, the compose tuning and the media persistence fix are done. Still to do: rotate secrets, purge history, unit tests for the services, gateway logging, the gateway starter swap.
- Tests: common-security has 17 unit tests. The 5 services still have only `contextLoads()`, and the frontend has no specs.
- There is no CI pipeline file: no `Jenkinsfile`, no `infra/`, and `.github/workflows/` is an empty untracked directory.

### 1.1 Repository tree (depth 3; excludes node_modules, target, dist, .git)
```
.
├── CLAUDE.md  Makefile  README.MD  note.md  docker-compose.yml  .dockerignore  .env (untracked)  .env.example  .gitignore
├── .claude/   settings.json  agents/reviewer.md  skills/{audit-check,snapshot}/SKILL.md
├── .github/workflows/        (empty, untracked)
├── backend/   pom.xml  mvnw  mvnw.cmd  .mvn/  Dockerfile  .dockerignore  .gitignore  .gitattributes
│   ├── common-security/   pom.xml  src/
│   ├── api-gateway/       pom.xml  Dockerfile  README.md  src/
│   ├── discovery-server/  pom.xml  README.md  src/
│   ├── media-service/     pom.xml  .env (TRACKED)  compose.yaml  note.md  README.md  src/
│   ├── product-service/   pom.xml  compose.yaml  note.md  README.md  src/
│   └── user-service/      pom.xml  README.md  src/
├── docs/      api-endpoints.md  architecture.md  kafka.md  security.md  reviews/
│   └── zone01/   analysis.md  audits.md  decisions.md  plan.md  subjects.md
├── frontend/  angular.json  Dockerfile  nginx.conf  package.json  package-lock.json  tsconfig*.json  public/  src/
├── scripts/   generate-keys.sh  setup-ssl.sh  update-java-to-21.sh
└── uploads/   avatars/  media/   (bind-mount targets, git-ignored)
```

### 1.2 Git
| Item | Value |
|---|---|
| Remote | `origin https://github.com/hmaach/Buy-01.git` |
| Current branch | `jenkins` (PRs are merged into it, e.g. #57) |
| Local branches | `jenkins`, `main`, `phase0-parent-pom` (merged), `claude/project-thread-n6ds0r` (original Claude-authored version of #57) |
| Remote branches | `main`, `jenkins`, `phase0-parent-pom`, `claude/project-thread-n6ds0r`, `BUY01-3-test`, `jenkins-installation` |
| Commit count (HEAD) | 229 |
| Authorship | The #57 commits were rewritten to the owner identity. Older `jenkins` history still contains Claude `Co-Authored-By` trailers (`docs/zone01/plan.md` Phase 0: purge pending) |

Last 10 commits:
```
5e73478 Merge pull request #57 from hmaach/phase0-parent-pom
d8ce0f1 fix(security): reject non-positive JWT expiration, harden verifier tests
ce86726 docs: use the build command that passes until service tests are fixed
fcb23da docs: describe the multi-module backend build in the README
8b597a2 feat(security): extract common-security module and migrate all services
27d9516 build(backend): add parent POM and align on Boot 3.5 / Cloud 2025.0 / Java 21
3a051ba docs: add decisions log and full phase plan
49a005f update kafka version
68bb0e4 chore: add claude code project rules, skills and reviewer agent
d7b3c38 docs: add zone01 plan, subjects, audits and codebase analysis
```

---

## 2. Tech stack and versions

### 2.1 Java / Spring (aligned)
| Item | Value | Source |
|---|---|---|
| Spring Boot parent | 3.5.16 | `backend/pom.xml` |
| Spring Cloud BOM | 2025.0.3 | `backend/pom.xml` |
| `java.version` / `maven.compiler.release` | 21 | `backend/pom.xml` |
| Enforcer | Java `[21,)`, Maven `[3.9,)`, `banDuplicatePomDependencyVersions` | `backend/pom.xml` |
| Maven wrapper | 3.9.12 (single, `backend/.mvn/wrapper/maven-wrapper.properties`) | |
| Docker build image | `maven:3.9-eclipse-temurin-21` → runtime `eclipse-temurin:21-jre` (all 5 services) | `backend/Dockerfile`, `backend/api-gateway/Dockerfile` |

Installed on the analysis machine: `java` not found, `mvn` not found, node v20.20.2, npm 10.8.2.

### 2.2 Third-party versions (`backend/pom.xml` `<properties>` + `<dependencyManagement>`)
| Dependency | Version | Used in | Actually used? |
|---|---|---|---|
| io.jsonwebtoken jjwt-api/impl/jackson | 0.12.7 | common-security only | yes (`Jwts.parser().verifyWith`, `Jwts.SIG.RS256`) |
| org.apache.tika tika-core | 3.2.3 | user, media | yes (MIME detection) |
| org.apache.tika tika-parsers-standard-package | 3.2.3 | media | **no** (only `Tika.detect`) |
| com.bucket4j bucket4j-core | 8.10.1 | gateway | yes |
| org.springdoc springdoc-openapi-starter-webmvc-ui | 2.8.17 | user | no annotations (inferred: UI blocked by security) |
| me.paulschwarz spring-dotenv | 4.0.0 | user | implicit `.env` loader |
| com.cloudinary cloudinary-http44 | 1.30.0 | user | **no** |
| io.projectreactor.kafka reactor-kafka | 1.3.25 | product | **no** |
| spring-cloud-starter-openfeign + feign-httpclient | BOM | user | **no** `@FeignClient` |
| spring-boot-starter-oauth2-resource-server | BOM | product, media | **no** (common-security filters used) |
| spring-cloud-starter-gateway | BOM | gateway | yes. Plan: replace with `spring-cloud-starter-gateway-server-webflux` |
| spring-kafka, data-mongodb(-reactive), web/webflux, eureka, actuator, validation, security, lombok, devtools | BOM | per service | yes |

### 2.3 Frontend (`frontend/package.json`, unchanged)
| Item | Version |
|---|---|
| Angular core/common/forms/router/animations | ^20.3.0 |
| @angular/cli, @angular/build | ^20.3.6 |
| UI | @angular/material + cdk ^20.2.14, lucide-angular ^0.577.0 |
| rxjs / zone.js / TypeScript | ~7.8.0 / ~0.15.0 / ~5.9.2 |
| Tests | karma ~6.4, jasmine-core ~5.9, karma-coverage ~2.2, karma-chrome-launcher ~3.2 (no `karma.conf.js`) |
| Docker build node | node:20-alpine |

### 2.4 Build tooling
- **Multi-module Maven.** The modules, in reactor order: `common-security`, `discovery-server`, `api-gateway`, `user-service`, `product-service`, `media-service`.
- **Shared configuration in `backend/pom.xml`.** `pluginManagement` holds the compiler with the Lombok annotation processor and `release=21`, and spring-boot-maven-plugin excluding Lombok. The enforcer runs in `<plugins>`. Every version is `0.0.1-SNAPSHOT`.
- **Missing:** `<distributionManagement>`, JaCoCo, Sonar, a surefire/failsafe split.
- **Commands:**
  - Root `Makefile`: `make install` runs `cd backend && ./mvnw -B clean install -DskipTests` (after `make ssl`).
  - `README.MD` documents the same command.
  - `CLAUDE.md` lists `./mvnw -B clean verify`, which will fail until the service `contextLoads` tests are fixed (§7, inferred).

---

## 3. Services

All services share the security setup below (except the gateway, see §4.1):
- They depend on `com.buy01:common-security`.
- Their `SecurityConfig` instantiates `JwtAuthenticationFilter` (servlet: user, media) or `ReactiveJwtAuthenticationFilter` (WebFlux: product) with the auto-configured `JwtVerifier`.
- The principal is `com.buy01.common.security.jwt.AuthenticatedUser(id, email, role)`, with authority `ROLE_<ROLE>`.

### 3.1 user-service
| Item | Value |
|---|---|
| Purpose | Registration, login (JWT issuance), profile, avatars, user-deleted event |
| Port | 8081 (`backend/user-service/src/main/resources/application.yaml`) |
| GAV | `com.buy01:user-service:0.0.1-SNAPSHOT` |
| Stack | Spring MVC, blocking Mongo, hexagonal (`domain/port/{in,out}`, `application/service`, `infrastructure/adapters`) |
| Token issuance | `infrastructure/adapters/out/security/adapter/JwtTokenGeneratorAdapter.java` implements `TokenGeneratorPort.generateToken` by calling `JwtIssuer.issue` from common-security. The port now has only `generateToken`, since verification moved to infrastructure |

**Endpoints** (`infrastructure/adapters/in/web/AuthController.java`, `UserController.java`, `config/WebConfig.java`)
| Method | Path | Auth (service) | Gateway | Role |
|---|---|---|---|---|
| POST | `/users/auth/register` (multipart: name,email,password,role,avatar?) | public | public | any. **The client chooses CLIENT or SELLER** |
| POST | `/users/auth/login` (JSON) | public | public | – |
| GET | `/users/avatars/**` (static files) | public | public | – |
| GET | `/users` (returns "user service is working") | JWT | JWT | any |
| GET | `/users/me` | JWT | JWT | any |
| PUT | `/users/me` (multipart: name?, email?, avatar?) | JWT | JWT | self |
| DELETE | `/users/me` | JWT | JWT | self |
| GET | `/users/id/{id}` | JWT | JWT | any. **Exposes any user's email** |
| GET | `/users/exists/{id}` | JWT | JWT | any |

**Document** `users` (`.../persistence/entity/UserEntity.java`): `id: UUID`, `name`, `email`, `password` (bcrypt), `role: Role{CLIENT,SELLER}`, `avatar`, `createdAt`, `updatedAt`. There is no unique index on email.

**Config keys**:
- server and multipart: `server.port`, `spring.servlet.multipart.max-file-size/max-request-size=10MB`;
- infrastructure: `eureka.client.service-url.defaultZone=${EUREKA_URL}`, `spring.data.mongodb.{host,port,database,username,password=***}`, `spring.kafka.bootstrap-servers`;
- JWT: `security.jwt.{private-key=***,public-key=***,expiration=3600000 ms}`;
- unused: `security.jwt.refresh-expiration` (no refresh flow) and `media.service.url`;
- other: `app.avatar.upload-dir=./uploads/avatars`, `management.*`.

**Communication**: a Kafka producer publishes `user-deleted`. There are no outbound REST calls; Feign is enabled but has no clients.

**Avatar validation** (`application/service/AvatarServiceImpl.java`): 2 MB max; Tika allow-list of jpeg/png/gif/webp; files are stored on the local filesystem as `UUID + client extension`.

### 3.2 product-service
| Item | Value |
|---|---|
| Purpose | Product CRUD, keyset-paginated listing, image aggregation from media-service |
| Port | 8082 |
| GAV | `com.buy01:product-service:0.0.1-SNAPSHOT` |
| Stack | WebFlux + reactive Mongo |

**Endpoints** (`infrastructure/web/controller/ProductController.java`, `infrastructure/config/SecurityConfig.java`)
| Method | Path | Auth (service) | Gateway | Role |
|---|---|---|---|---|
| GET | `/products?beforeTime=ISO` (10 items) | public | public | – |
| GET | `/products/{id}` | public | public | – |
| GET | `/products/user?beforeTime=` | JWT | public (`GET /products/**`) | any |
| POST | `/products` | JWT | JWT | `SELLER` |
| PUT | `/products/{id}` | JWT | JWT | `SELLER` + owner (`ProductServiceImpl.updateProduct`) |
| DELETE | `/products/{id}` | JWT | JWT | `SELLER` + owner (`ProductServiceImpl.deleteProduct`) |

Validation (`infrastructure/web/dto/ProductCreateRequest.java`): name 3–100, description 10–500, price 1–10000, quantity 0–1000, imagesIds 1–10.

**Document** `products` (`infrastructure/persistence/mongo/ProductDocument.java`): `id`, `name`, `description`, `price: double`, `quantity: int`, `user_id`, `created_at`, `updated_at`. Image IDs are stored only in media-service.

**Config keys**: `spring.data.mongodb.*` (password=***, with a hardcoded fallback), `spring.kafka.bootstrap-servers`, `kafka.topics.user-deleted`, `eureka.*`, `security.jwt.public-key=***`, `management.*`.

**Communication**:
- REST through a `@LoadBalanced WebClient` to `http://MEDIA-SERVICE` (`GET /media/product/{id}`, `POST /media/batch`). The calls carry no token.
- Kafka: produces `images-linked` and `product-deleted`; consumes `user-deleted`.

### 3.3 media-service
| Item | Value |
|---|---|
| Purpose | Image upload, serve and delete; PENDING→LINKED status; orphan cleanup |
| Port | 8083 |
| GAV | `com.buy01:media-service:0.0.1-SNAPSHOT` |
| Stack | Spring MVC, blocking Mongo, filesystem storage |

**Endpoints** (`infrastructure/web/controller/MediaController.java`, `infrastructure/config/SecurityConfig.java`)
| Method | Path | Auth (service) | Gateway | Role |
|---|---|---|---|---|
| GET | `/media` (health string) | public | public | – |
| GET | `/media/user` (echo principal) | public → **NPE/500 when anonymous** (casts a null `Authentication` to `AuthenticatedUser`) | public | – |
| GET | `/media/{id}` (file, 1-day public cache) | public | public | – |
| GET | `/media/product/{productId}` | public | public | – |
| POST | `/media/batch` `{productIds}` → `{productId: firstMediaId}` | public | public | – |
| POST | `/media` multipart `files[]` | JWT | public (`/media/**`) | any authenticated (CLIENT can upload) |
| DELETE | `/media/{id}` | JWT | public | owner (`MediaServiceImpl.deleteById`) |

**Document** `medias` (`infrastructure/persistence/MediaDocument.java`): `id`, `image_path`, `product_id`, `user_id`, `status: FileStatus{PENDING,LINKED}`, `created_at`.

**Config keys**: multipart `max-file-size=2MB`, `max-request-size=50MB`, `spring.data.mongodb.*` (password=***, with a hardcoded fallback), `spring.kafka.bootstrap-servers`, `security.jwt.public-key=***`, `eureka.*`, `management.*`.

**Communication**:
- Kafka consumer only (`images-linked`, `product-deleted`).
- `@Scheduled` `OrphanedMediaCleanupJob` runs hourly and deletes PENDING media older than **1 minute**.

### 3.4 Actuator exposure
| Layer | Setting |
|---|---|
| `application.yaml` (gateway, user, product, media) | `management.endpoints.web.exposure.include: "*"`, `health.show-details: always` |
| `docker-compose.yml` (`x-java` anchor, all 5 Java services) | `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info`. This overrides the yaml in Docker |
| discovery-server | default |

The result is that only `health` and `info` are exposed in the deployed stack. A local `spring-boot:run` still exposes everything, since that only reads the yaml. The gateway's `/actuator/**` is unauthenticated (gateway `SecurityConfig` is `permitAll`). `show-details: always` remains, which leaks component details through `/actuator/health`.

---

## 4. Infrastructure services

### 4.1 API Gateway (`backend/api-gateway`)
- **Routes** (`application.yaml`, prefix `spring.cloud.gateway.server.webflux.routes`): `/users/**` → `lb://USER-SERVICE`, `/products/**` → `lb://PRODUCT-SERVICE`, `/media/**` → `lb://MEDIA-SERVICE`, each with the `Authentication` filter. `config/GatewayConfig.java` is commented-out dead code.
- **`filter/AuthenticationGatewayFilterFactory.java`**:
  - checks a public-endpoint allow-list;
  - then `BearerToken.from(...)` + `JwtVerifier.verify(...)` from common-security, which parses the token once and uses constructor injection;
  - adds the `X-User-Id`/`X-User-Role` headers, which no service reads.
- **User existence check is still dead.** The WebClient is not `@LoadBalanced`, the `http://USER-SERVICE` call is unauthenticated, and `onErrorResume` lets the request through.
- **`filter/LoggingFilter.java`** logs all request headers, including tokens, at INFO.
  - In Docker, `LOGGING_LEVEL_ROOT=WARN` suppresses these INFO lines (inferred).
  - `logging.level.org.springframework.web: DEBUG` in the yaml is more specific than root, so Spring web DEBUG logs are still emitted (inferred).
- **`filter/RateLimitFilter.java`**: Bucket4j with one bucket per IP (100 req/min). The key comes from `X-Forwarded-For`, which is spoofable, and the in-memory map is unbounded.
- **CORS** (`config/CorsConfig.java`): localhost origins only, credentials true. Production works same-origin through nginx (inferred).
- **TLS**: `server.ssl` with PKCS12 `classpath:edge-service.p12`, password `${SSL_PASSWORD}`. The Dockerfile generates a self-signed cert on every build and fails if `SSL_PASSWORD` is empty. HTTP/2 is on.
- **Dead code:** `TestController` (`GET /`), and the empty classes `security/JwtConverter.java` and `exception/GatewayException.java`.

### 4.2 Discovery (`backend/discovery-server`)
- Eureka server (`EurikaServerApplication.java`; the class name still has the typo), port 8761, not self-registering.
- Clients: GATEWAY, USER-SERVICE, PRODUCT-SERVICE, MEDIA-SERVICE, all through `EUREKA_URL=http://discovery-server:8761/eureka`.

### 4.3 Kafka
- **Broker:** `apache/kafka:4.1.0`, single-node KRaft, `KAFKA_HEAP_OPTS=-Xms128m -Xmx256m`, `mem_limit: 512m`.
- **Topics:** 1 partition by default, offsets topic with 1 partition, retention 24 h / 50 MB, segments of 16 MB (`docker-compose.yml`).
- **Missing:** topic creation beans, a DLQ, an error handler and a schema registry. Consumers auto-commit and use `addTrustedPackages("*")`.
- **Version history:**
  - The image moved from `:latest` → 3.9.1 → 4.1.0.
  - Two crash loops were caused by a `kafka-data` volume written by a newer broker (`docs/zone01/decisions.md` incident 2, and the 2026-11-11 crash with `metadata.version` 29).
  - Rule: the pinned version must be ≥ the version that wrote the volume. Otherwise, delete the volume.

| Topic | Producer | Consumer (group) | Payload |
|---|---|---|---|
| `user-deleted` | user `UserServiceImpl.sendKafkaUserDeleted` | product `messaging/UserListener` (`product-service-group`) | `{userId: UUID}` (read as `String`) |
| `images-linked` | product `ImageService.sendKafkaImageLinked` (create and update) | media `MediaStatusListener.onImagesLinked` (`media-service-group`) | `{productId, mediaIds}` |
| `product-deleted` | product `ImageService.sendKafkaProductDeleted` | media `MediaStatusListener.onProductDeleted` | `{productId}` |

### 4.4 Media storage
| Aspect | user avatars | product images |
|---|---|---|
| Location | `./uploads/avatars` → bind `./uploads/avatars:/app/uploads/avatars` | relative `uploads/` (`MediaServiceImpl.UPLOAD_DIR`) → bind `./uploads/media:/app/uploads`. **Persisted (fixed in `7568be7`)** |
| MinIO / GridFS | not found | not found |
| Size | 2 MB (code) + 10 MB multipart | 2 MB (code) + 2 MB file / 50 MB request |
| MIME | Tika magic bytes, jpeg/png/gif/webp | same |
| Filename | `UUID + client extension` | `UUID + client extension`; the Mongo ID differs from the file UUID |

---

## 5. Security

### 5.1 Auth flow end to end
1. **Register:** `POST /api/users/auth/register`. Host nginx forwards to the gateway (public route), then to user-service. The password is hashed with BCrypt and the role is chosen by the client. No token is returned.
2. **Login:** `POST /users/auth/login`. `AuthServiceImpl.login` checks the password, then `TokenGeneratorPort` → `JwtTokenGeneratorAdapter` → `JwtIssuer.issue`. The token is **RS256** with `sub`, `email`, `role`, `iat` and `exp` (`security.jwt.expiration`, default 1 h). The response is `{token, expiresAt}`. There is no refresh token.
3. **Client:** stores the token in `localStorage`. `authInterceptor` adds `Authorization: Bearer` and the `/api` prefix.
4. **Gateway:** `JwtVerifier.verify` checks the signature, expiry, and that `sub` and `role` are present. The user-exists check is dead (§4.1).
5. **Services:** the common-security filter re-verifies the token with the public key and sets the `AuthenticatedUser` principal.
6. **Fail-fast:** `CommonSecurityAutoConfiguration` throws at startup when `security.jwt.public-key` is empty or malformed, or when `expiration` ≤ 0. `JwtIssuer` is only created when `security.jwt.private-key` is set, which is user-service only.
7. **Service-to-service:** product→media calls carry no token and rely on public media endpoints.

### 5.2 Hashing, roles, ownership
- **Hashing:** `BCryptPasswordEncoder` (strength 10), in `user-service/.../infrastructure/security/BcryptPasswordEncoder.java`.
- **Roles:** `Role{CLIENT, SELLER}`. There is no ADMIN. Anyone can self-register as SELLER.
- **Enforcement:**
  | Rule | Where |
  |---|---|
  | SELLER required for POST/PUT/DELETE `/products/**` | `product-service/.../infrastructure/config/SecurityConfig.java` |
  | Product owner check | `ProductServiceImpl.updateProduct/deleteProduct`. A mismatch returns 400 instead of 403 |
  | Media owner check on delete | `MediaServiceImpl.deleteById` |
  | Media upload | any authenticated user |
  | Media → product linking | **no ownership check** (`MediaStatusListener.onImagesLinked`) |
  | Frontend | `roleGuard(['SELLER'])` on `/products/new` and `/products/:id/edit` |

### 5.3 HTTPS
- **Gateway:** self-signed PKCS12, generated at image build. `scripts/setup-ssl.sh` (`make ssl`) generates one into `src/main/resources` for local runs.
- **Frontend container:** nginx serves self-signed TLS on 443 and proxies `/api/` → `https://api-gateway:8080/` with `proxy_ssl_verify off`.
- **Production:** host nginx terminates Let's Encrypt TLS (`CLAUDE.md` Deployment).
- **Internal traffic:** service-to-service, Kafka and Mongo connections are plain.

### 5.4 Other risks
- The gateway logs bearer tokens (`LoggingFilter`), suppressed in Docker by root WARN (inferred).
- The JWT is stored in `localStorage`.
- Inbound `X-User-*` headers are not stripped (latent risk; no service reads them).
- `GET /users/id/{id}` leaks emails.
- Kafka consumers use `addTrustedPackages("*")`.
- Exception messages are echoed to clients (the gateway "Authentication failed: …" message and the default ProblemDetail templates).
- `management.endpoint.health.show-details: always`.

### 5.5 Hardcoded secrets (locations only)
| Location | What | Status |
|---|---|---|
| `backend/media-service/.env` | **tracked in git**: Mongo credentials | open (Phase 0) |
| `.env.example` | non-empty `SSL_PASSWORD`, `USER_MONGO_PASSWORD`, `PRODUCT_MONGO_DB_PWD`, `MEDIA_MONGO_DB_PWD` | open |
| `backend/{user,product,media}-service/src/main/resources/application.yaml` | fallback default values for the Mongo user and password | open |
| `backend/api-gateway/Dockerfile`, `frontend/Dockerfile` | the gateway `ARG SSL_PASSWORD` default **was removed**; the frontend Dockerfile still has an `ARG SSL_PASSWORD` default | partly fixed |
| Git history | root `.env` with JWT private/public keys and Mongo passwords (until `e606c48`), plus `gateway-keystore.p12`, `frontend/key.pem` and `frontend/cert.pem` | open: rotation and purge in Phase 0 |

---

## 6. Frontend (`frontend/`, unchanged since last snapshot)

- **Structure:** Angular 20, standalone components, `src/app/app.config.ts` with `provideRouter(withComponentInputBinding)` and `provideHttpClient(withInterceptors([authInterceptor, errorInterceptor]))`. `strict` is on.
- **Routes** (`src/app/app.routes.ts`, all lazy):
  | Path | Component | Guard |
  |---|---|---|
  | `` | ProductList | – |
  | `login`, `register` | Login, Register | `guestGuard` |
  | `profile` | Profile | `authGuard` |
  | `products/new`, `products/:id/edit` | ProductForm | `roleGuard(['SELLER'])` |
  | `products/list` | ProductList | – |
  | `dashboard` | ProductList (myProducts) | `authGuard` |
  | `products/:id` | ProductDetail | – |
  | `**` | → `''` | – |
- **Interceptors:**
  - `auth.interceptor.ts`: prefixes the base URL and adds the bearer token.
  - `error.interceptor.ts`: logs the user out on 401, on 429 and on 404 for `/me`.
  - `token-interceptor.ts`: unused. `token.service.ts` and `auth.model.ts` are empty.
- **API services:**
  | Service | Calls |
  |---|---|
  | `auth.service.ts` | login, register, `/users/me` |
  | `user.service.ts` | PUT and DELETE `/users/me` |
  | `product.service.ts` | `/products`, `/products/user`, `/products/{id}`. Uses `JSON.parse(r.imagesIds[0])` and **injects fake rating, reviews and oldPrice** |
  | `media.service.ts` | POST `/media`, DELETE `/media/{id}` |
- **Forms:** Reactive Forms. Frontend rules differ from the backend: description min 20 vs 10, price min 0.01 vs 1, no max price or quantity, login password max 100 vs 30. `ProductCreateRequest` (TS) has a stray `int` field.
- **Environment:** `environment.ts` and `environment.prod.ts` are identical: `backendUrl='/api'`, `mediaUrl='/api/media'`, `avatarUrl='/api/users'`.
- **Build:** `frontend/Dockerfile` uses `--base-href=/` (reverted in `85b3436`). Serving under `/app/` needs `/app/` (inferred to still be an issue if production serves at `/app/`).
- **README:** `frontend/README.md` is stale (mock JWT, `/seller/...` routes, test accounts).

---

## 7. Tests

### 7.1 Backend
| Module | Test files | Content | `@Test` count |
|---|---|---|---|
| common-security | `CommonSecurityAutoConfigurationTest`, `jwt/JwtVerifierTest`, `web/JwtAuthenticationFilterTest`, `web/ReactiveJwtAuthenticationFilterTest`, helper `TestKeys` | real unit tests (detailed below) | 17 |
| api-gateway | `GatewayApplicationTests` (+ a stray `src/test/index.html`) | `contextLoads()` | 1 |
| discovery-server | not found | – | 0 |
| user-service | `UserApplicationTests` | `contextLoads()` | 1 |
| product-service | `ProductApplicationTests`; `MongoConnectionTest` (fully commented out) | `contextLoads()` | 1 (+1 commented) |
| media-service | `MediaApplicationTests` | `contextLoads()` | 1 |

The common-security tests cover:
- token verification: valid, expired, wrong key, tampered, no role, garbage input;
- TTL handling with a fixed `Clock`;
- the role → authority mapping;
- both filters (authenticate on a valid token, stay anonymous otherwise);
- the auto-configuration (verifier only, issuer with a private key, fail without a public key, fail on non-positive expiration).

The service `contextLoads` tests need live Mongo, Kafka and Eureka. Since common-security fails fast, they **also need a valid `JWT_PUBLIC_KEY`**, so `./mvnw verify` will fail on a clean agent (inferred). Commit `ce86726` documents `-DskipTests` as the working build. Business-logic coverage in the services: 0. Testcontainers and embedded Kafka: not found.

### 7.2 Frontend
- `*.spec.ts` files: **0**. `karma.conf.js`: not found. A headless Chrome config for CI: not found.

### 7.3 Coverage tooling
- JaCoCo: not found. Sonar config: not found. `karma-coverage` is installed but has no lcov reporter configured.

---

## 8. Containerization and run setup

### 8.1 Dockerfiles
| Image | File | Context | Base | Notes |
|---|---|---|---|---|
| discovery, user, product, media | `backend/Dockerfile` | `backend/` | maven:3.9-eclipse-temurin-21 → eclipse-temurin:21-jre | Build arg `SERVICE` selects the module: `mvn -B -q -pl $SERVICE -am package -DskipTests` with a BuildKit `--mount=type=cache,target=/root/.m2`. `COPY . .` copies the whole backend, so any change invalidates the layer. Uses the image's `mvn`, not `./mvnw`. Runs as root. No HEALTHCHECK, no EXPOSE |
| api-gateway | `backend/api-gateway/Dockerfile` | `backend/` | same | Installs openssl, generates a self-signed PKCS12 (fails if `SSL_PASSWORD` is empty), same mvn command. Runs as root. No HEALTHCHECK |
| frontend | `frontend/Dockerfile` | `frontend/` | node:20-alpine → nginx:alpine | `npm ci`, `--base-href=/`, self-signed cert, `ARG SSL_PASSWORD` default still present |

`backend/.dockerignore` excludes `**/target`, `**/.env`, `**/*.p12`, `**/*.pem`, `**/uploads`, IDE folders, `**/node_modules` and `**/compose.yaml`. The root `.dockerignore` lists `.env` and `**/uploads`.

### 8.2 docker-compose (`docker-compose.yml`)
Shared anchors:
- `x-logging`: json-file, 5m × 2.
- `x-java`: `JAVA_TOOL_OPTIONS` (`-Xmx256m`, SerialGC, `TieredStopAtLevel=1`), `LOGGING_LEVEL_ROOT=WARN`, actuator `health,info`.
- `x-mongo-health`: mongosh ping every 60 s, start_period 30 s.
- `x-mongo-cmd`: `mongod --quiet --wiredTigerCacheSizeGB 0.25`, diagnostics off.

| Service | Image/build | mem_limit | Published port | Volumes | depends_on | Healthcheck |
|---|---|---|---|---|---|---|
| discovery-server | build `backend/` (SERVICE) | 320m (`-Xmx192m`) | none | – | – | no |
| kafka | apache/kafka:4.1.0 | 512m | none | kafka-data | – | no |
| mongo-user / mongo-media / mongo-product | mongo:7 | 384m each | none | *-data | – | yes |
| api-gateway | build `backend/` + `api-gateway/Dockerfile` | 384m | `127.0.0.1:${GATEWAY_PORT}:8080` | – | discovery-server | no |
| user-service | build (SERVICE) | 384m | none | `./uploads/avatars:/app/uploads/avatars` | mongo-user healthy, discovery, kafka | no |
| product-service | build (SERVICE) | 384m | none | – | mongo-product healthy, discovery, kafka | no |
| media-service | build (SERVICE) | 384m | none | `./uploads/media:/app/uploads` | mongo-media healthy, discovery, kafka | no |
| frontend | build `frontend/` | 64m | `127.0.0.1:4200:443` | – | api-gateway | no |

- Total memory limits ≈ 3.5 GB on a 6 GB VM with 4 GB of swap (`docs/zone01/decisions.md`).
- Jenkins is no longer in this file; CI tools go to the CI VM (`infra/ci/`, not found yet).
- `SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}` is passed but no `application-<profile>.yaml` exists.
- The per-service `compose.yaml` files in media and product are legacy, and excluded from the Docker context.

### 8.3 How to run today
1. `make env`: `scripts/generate-keys.sh` creates `.env` from `.env.example` if needed and writes `JWT_PRIVATE_KEY` and `JWT_PUBLIC_KEY`. Both are mandatory now: the services fail at startup without the public key.
2. Fill the Mongo credentials and `SSL_PASSWORD` in `.env`. The gateway build fails if `SSL_PASSWORD` is empty.
3. `make docker-build` (runs `make ssl`, then `docker compose build`). On the 1-OCPU VM, build one service at a time to avoid running out of memory (inferred from the 2026-11-11 deploy).
4. `make docker-up`.
5. Wait 60 to 120 s for Spring Boot startup and Eureka registration before testing. Hitting it earlier gives a 502 from host nginx.
6. Check: `curl -sk https://127.0.0.1:8080/actuator/health`.
7. Local Maven build: `cd backend && ./mvnw -B clean install -DskipTests`. Dev mode without Docker is still broken (no published infrastructure ports, gateway keystore needed; inferred).

### 8.4 README quality (`README.MD`)
- Now documents the single reactor build command.
- **Still wrong:**
  - Mongo ports (product/media swapped);
  - media described as "WebFlux" and "GridFS";
  - "Spring Boot 3.x" is now accurate, but the per-service README files are not updated (inferred);
  - actuator links point at unpublished ports.
- **Missing:** the deployment topology (only in `CLAUDE.md`, since `docs/AI-CONTEXT.md` was deleted), tests, CI/CD, troubleshooting.
- **Remaining `docs/`:** `api-endpoints.md`, `architecture.md`, `kafka.md`, `security.md` (pre-Phase-0 content, possibly stale), plus `docs/zone01/`.

---

## 9. Readiness assessment

### 9.1 mr-jenk (Jenkins CI/CD)
| Item | State |
|---|---|
| Jenkinsfile | **not found** |
| CI stack | `infra/ci/` not found. Jenkins removed from the app compose (by design: separate CI VM) |
| Build command | **Ready**: one reactor build `cd backend && ./mvnw -B clean verify`, plus `-pl <module> -am` per module |
| Unit tests | common-security yes (17). Services no; their `contextLoads` will break `verify` on an agent (inferred) |
| Test reports | Surefire XML will exist. Karma JUnit reporter not configured |
| Dockerfiles | Ready, multi-stage, ARM-compatible bases. They rebuild Maven inside Docker (CI could instead copy jars). Tags: none (`build:` only, no `image:`) |
| Health checks | Mongo only. Java services have none in compose or the Dockerfiles; actuator `health` is exposed |
| Deploy/rollback | Manual (`docker compose up -d`). Rollback design lives in `docs/zone01/decisions.md` (Nexus tags) |
| Secrets in CI | `.env` untracked. Jenkins needs credentials for the JWT keypair, `SSL_PASSWORD` and the Mongo passwords |

### 9.2 safe-zone (SonarQube): likely findings
- **Coverage:** low. Only common-security is covered; JaCoCo and lcov are not configured.
- **Duplication:**
  - **JWT duplication is resolved.**
  - Still duplicated: `GlobalExceptionHandler` + `ProblemTemplate` + `DetailGenerator` (product and media), Kafka consumer/producer config boilerplate, ~25 identical `ProblemDetail` blocks in the user-service handler, `getProductsList`/`getUserProductsList`, the gateway error JSON written 3 times.
- **Smells:**
  - empty classes: gateway `JwtConverter`, `GatewayException`; user-service `BcryptPasswordEncoderAdapter`, `adapters/out/security/config/SecurityConfig`, `BeanConfiguration`, `MongoConfig`;
  - commented code: `GatewayConfig.java`, `MongoConnectionTest.java`, `Media.Builder.build`;
  - `System.out`/`System.err`;
  - unused fields in `ProductServiceImpl`;
  - unused `JwtProperties.canIssue()`;
  - typos (`EurikaServerApplication`, `Faileduploadedfile`, `getProdutImages`, `EVEREY_HOURE`);
  - `ResponseEntity<?>`;
  - a null check after dereference in `MediaController.getImagePatch`;
  - `.subscribe()` inside a Kafka listener.
- **Security hotspots:**
  - CSRF disabled in 4 places (acceptable for stateless JWT, but flagged);
  - CORS with credentials;
  - the gateway `permitAll`;
  - actuator `"*"` in the yaml;
  - Mongo credential fallbacks in the yaml;
  - header logging;
  - `trustedPackages("*")`;
  - minimum password length 6;
  - `proxy_ssl_verify off`;
  - containers running as root;
  - the frontend `ARG SSL_PASSWORD` default;
  - `include-stacktrace: always`.
- **Frontend:** `any` types, `console.*` calls, empty files, an unused interceptor.

### 9.3 nexus: Java 11 constraint and versioning
- **Java 11:** unchanged analysis. Boot 3.5 requires Java 17+, and the code uses records, `Stream.toList()`, text blocks and `jakarta.*`. Decision: keep Java 21, pending staff confirmation (`docs/zone01/decisions.md`).
- **Versioning:** a single `com.buy01` groupId and one parent version `0.0.1-SNAPSHOT` make Nexus publishing straightforward. Still missing:
  - `<distributionManagement>`;
  - a version strategy (`${revision}` or release plugin);
  - `settings.xml`;
  - Docker `image:` names and tags in compose (the deploy-by-tag design depends on them).

### 9.4 buy-02: where features fit (unchanged)
| Feature | Placement |
|---|---|
| Cart and orders | New `order-service`: hexagonal, Mongo, `common-security` dependency, gateway route `/orders/**`. Kafka `order-created`/`order-status-changed` events; product-service decrements `quantity` |
| Search and filters | Extend product-service: text index plus price/name filters in `SpringDataProductRepository`; consider storing `mainImage` on the product |
| Analytics | Mongo aggregations in order-service |
| Profiles | user-service endpoints + aggregation calls |

New since the last snapshot: adding a module is now one `<module>` line in `backend/pom.xml`, plus one compose service using `backend/Dockerfile` with `SERVICE: order-service`.

### 9.5 Multi-module parent POM
**Done** (`27d9516`). Remaining improvements:
- add JaCoCo, surefire/failsafe and Sonar to `pluginManagement`;
- an optional `common-web` module for the duplicated ProblemDetail handler and the Kafka event DTOs (`UserDeletedEvent` types still differ: `UUID` in user, `String` in product);
- remove the unused dependencies listed in §2.2.

### 9.6 ARM64 (Oracle Ampere A1)
- **Confirmed in practice:** the stack was rebuilt and started on the A1 VM on 2026-11-11 (server output).
- All base images are multi-arch. Kafka and Mongo are pinned; `nginx:alpine`, `node:20-alpine`, `maven:3.9-…` and `eclipse-temurin:21-jre` are floating minor tags.
- `scripts/update-java-to-21.sh` downloads an x64-only JDK.

---

## 10. Issues and technical debt

Status legend: **open**, **fixed** (since the last snapshot).

### Critical
1. **open:** Secrets in git history (root `.env` with the JWT private key and DB passwords, `gateway-keystore.p12`, `frontend/*.pem`). `backend/media-service/.env` is still tracked. Rotate, untrack and purge (Phase 0).
2. **open:** The gateway user-existence check is dead (`AuthenticationGatewayFilterFactory`), so deleted users keep access until their token expires.
3. **fixed:** Media files not persisted (mount now `./uploads/media:/app/uploads`, `7568be7`).
4. **open:** Product deletion never deletes media: `MediaStatusListener.onProductDeleted` calls `deleteById(media.getImagePath())` and the file is never removed.
5. **open:** Orphan cleanup deletes PENDING media after **1 minute** (`OrphanedMediaCleanupJob.ORPHAN_AGE`).
6. **open (partly):** No service unit tests. The `contextLoads` tests need live infrastructure plus a JWT key, which blocks `mvn verify` in CI. common-security is covered.
7. **open:** Self-assigned SELLER role at registration.

### Important
8. **open:** Product→media JSON bug (`ImageService.getProductImages` uses `bodyToFlux(String.class)`), worked around in the frontend with `JSON.parse(imagesIds[0])`.
9. **open:** Media linking has no ownership check.
10. **open:** `ProductWebMapper.toResponse` omits `quantity`.
11. **open:** Product update never unlinks removed images. `Product.Builder(Product)` drops `imagesIds`.
12. **open:** `Product.build()` validates on read, so a stored document with invalid data throws.
13. **open:** Ownership violation returns 400 instead of 403, and not-found returns 400.
14. **open:** The gateway marks all `/media/**` (any method) and `GET /products/user` as public.
15. **open:** `GET /media/user` returns 500 for anonymous callers; `POST /media/batch` has an NPE on a null body.
16. **open (mitigated in Docker):** The gateway logs headers, including tokens. `org.springframework.web: DEBUG` is still in the gateway yaml.
17. **fixed:** Version drift (all modules on Boot 3.5.16 / Cloud 2025.0.3 / Java 21; tika unified on 3.2.3).
18. **open:** No unique index on `users.email`.
19. **open:** Kafka has no DLQ or error handler, uses auto-commit, calls `.subscribe()` in a listener, and trusts all packages. **New risk:** a pinned version below the volume's `metadata.version` crash-loops (incident 2026-11-11).
20. **open:** Frontend base-href `/` vs `/app/` deployment; the error interceptor logs users out on 429.
21. **open:** Dev mode without Docker is broken.
22. **open (mitigated in Docker):** actuator `"*"` in the yaml (compose overrides it to `health,info`), and `show-details: always`.
23. **open:** Fake rating, reviews and oldPrice in `ProductService.getProduct`.
24. **new, open:** The Java containers have no HEALTHCHECK, so `depends_on` cannot wait for the gateway or the services, and deploy health is checked by hand.
25. **new, open:** All containers run as root (`CLAUDE.md` rule: non-root).
26. **new, open:** `spring-cloud-starter-gateway` must be replaced by `spring-cloud-starter-gateway-server-webflux` (Phase 0 plan).

### Nice to have
27. **open:** Remove dead code and dependencies:
    - empty classes (§9.2), `GatewayConfig.java`, `TestController`, the `GET /users` and `GET /media` health strings;
    - frontend: `token-interceptor.ts`, `token.service.ts`, `auth.model.ts`;
    - product DTOs: `ProductCreatedEvent`, `ImageLinkedEvent`;
    - user-service config: `media.service.url`, `refresh-expiration`;
    - dependencies: Feign, Cloudinary, reactor-kafka, tika-parsers, oauth2-resource-server;
    - `JwtProperties.canIssue()`.
28. **fixed:** JWT logic duplicated 4× (now in `common-security`).
29. **fixed:** jjwt 0.11.5 deprecated API (now 0.12.7, claims parsed once).
30. **open:** Docker builds use the image's `mvn` instead of `./mvnw`, and `COPY . .` invalidates the cache on any backend change. There is no `EXPOSE` in the shared Dockerfile.
31. **open:** Fix the README errors (ports, GridFS/WebFlux), rewrite `frontend/README.md`, and review the remaining `docs/*.md` against the code.
32. **open:** Align frontend and backend validation; remove the `int` field from the TS `ProductCreateRequest`.
33. **open:** Rate limiter: bounded cache and trusted-proxy-aware IP.
34. **open:** Naming: `EurikaServerApplication` (class), `Faileduploadedfile`, `getProdutImages`, `EVEREY_HOURE`, `SpringDataProductRepository` in media-service.
35. **open:** Delete the legacy per-service `compose.yaml` files, the unused `KAFKA_ZOOKEEPER`, and `SPRING_PROFILES_ACTIVE` (no profile files).
36. **new, open:** Remove the stale local branches: `phase0-parent-pom` (merged) and `claude/project-thread-n6ds0r`, both locally and on origin.
