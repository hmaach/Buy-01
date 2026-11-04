# BUY-01 Technical Analysis

Snapshot date: 2026-11-04. Branch analysed: `jenkins` @ `85b3436`. The analysis was read-only: no build was run and no dependencies were downloaded.
Conventions: **(inferred)** = deduced, not directly verified. **not found** = searched, absent. Secret values are never shown (`***`).

---

## 1. Overview

Buy-01 is a microservices e-commerce platform. Sellers register, upload product images and create, update or delete products. Clients and anonymous users browse the products. It has 5 Spring Boot backends: gateway, Eureka, user, product and media. The services use MongoDB (one instance per service), communicate asynchronously through Kafka, and are fronted by an Angular 20 SPA served by nginx. All of it is orchestrated by one `docker-compose.yml`, which also starts a Jenkins container.

Current state:
- The functional MVP (auth, product CRUD, media upload) works according to `docs/AI-CONTEXT.md` §11.
- There are effectively **zero tests**: only `contextLoads()` stubs, and no frontend specs.
- There is **no CI pipeline file**: no `Jenkinsfile` and an empty `.github/workflows/`. The `jenkins` branch so far only adds a Jenkins container and a planning doc (`docs/cicd.md`).
- The team is deploying to a VM behind host nginx at `/app/`, `/api/` and `/jenkins/` (`docs/AI-CONTEXT.md`).

### 1.1 Repository tree (depth 3; excludes node_modules, target, dist, .git)
```
.
├── .dockerignore  .env (untracked)  .env.example  .gitignore  Makefile  README.MD  note.md  docker-compose.yml
├── .github/workflows/            (empty)
├── .vscode/settings.json
├── backend/
│   ├── api-gateway/       Dockerfile  pom.xml  mvnw  mvnw.cmd  .mvn/  README.md  src/
│   ├── discovery-server/  dockerfile (lowercase)  pom.xml  mvnw  .mvn/  README.md  src/
│   ├── media-service/     Dockerfile  compose.yaml  .env (TRACKED)  note.md  pom.xml  mvnw  .mvn/  README.md  src/
│   ├── product-service/   Dockerfile  compose.yaml  note.md  pom.xml  mvnw  .mvn/  README.md  src/
│   └── user-service/      Dockerfile  pom.xml  mvnw  .mvn/  README.md  src/
├── docs/   AI-CONTEXT.md api-endpoints.md architecture.md cicd.md GATEWAY_SERVICE_GUIDE.md GATEWAY_SETUP_SUMMARY.md
│           GATEWAY_TESTING_GUIDE.md HEXAGONAL_CHEAT_SHEET.md kafka.md security.md security_roadmap.md
├── frontend/  angular.json Dockerfile nginx.conf package.json package-lock.json theme.css tsconfig*.json README.md
│   ├── public/  (assets/, empty.png, favicon.ico)
│   └── src/     app/ environments/ index.html main.ts styles.css custom-theme.scss
├── scripts/   generate-keys.sh  setup-ssl.sh  update-java-to-21.sh
└── uploads/   avatars/  media/   (bind-mount targets, git-ignored)
```

### 1.2 Git
| Item | Value |
|---|---|
| Remote | `origin https://github.com/hmaach/Buy-01.git` |
| Current branch | `jenkins` (in sync with `origin/jenkins`) |
| Other branches | local `main`; remote `origin/main`, `origin/BUY01-3-test`, `origin/jenkins-installation` (1 commit `b9da339 feat: init jenkins & Plugins` not on `jenkins`) |
| `jenkins` vs `main` | 10 commits ahead, 0 behind |
| Commit count (HEAD) | 215 (2026-01-26 → 2026-08-19) |
| Authors | Hamza Maach 134+6, Rachid Serraf 40+35 |

Last 10 commits:
```
85b3436 update the docker file of the frontend
6c309a1 update the nginx config file of the frontend
a3b058a fix the environment links
1cf990d refactor: change the  base href of the frontend to app/
59aaf3f docs: add AI-CONTEXT.md
52ec1d1 refactor: remove the expose of services in docker compose config
bd6d2c0 refactor: use apt-get instead of apk to install openssl in api-gateway
fde355a remove alpine image from the docker files in the backend services
2aef3e9 docs: create roadmap doc to implement CI/CD
cd762ca feat: add jenkins service to docker compose
```
Note: `85b3436` reverts `--base-href=/app/` back to `/` in `frontend/Dockerfile`, which contradicts the deployment requirement in `docs/AI-CONTEXT.md` §9.1.

---

## 2. Tech stack and versions

### 2.1 Java / Spring (versions are inconsistent across services)
| Service | Spring Boot parent | Spring Cloud | `java.version` | Build image (Dockerfile) |
|---|---|---|---|---|
| api-gateway | **3.5.11** | 2025.0.1 | 21 | maven:3.9-eclipse-temurin-21 → eclipse-temurin:21-jre |
| discovery-server | **4.0.0** | 2025.1.0 | **17** | maven:3.9-eclipse-temurin-17 → eclipse-temurin:17-jre |
| user-service | 3.4.12 | 2024.0.2 | 21 | temurin-21 |
| product-service | 3.4.12 | 2024.0.2 | 21 | temurin-21 |
| media-service | 3.4.12 | 2024.0.2 | 21 | temurin-21 |

Installed locally: `java` **not found**, `mvn` **not found**, node **v20.20.2**, npm **10.8.2**. `ng version` could not run because `node_modules` is not installed. Local arch: x86_64.

### 2.2 Major dependencies (explicit versions; others are managed by the Boot BOM)
| Dependency | Version | Used in | Actually used? |
|---|---|---|---|
| io.jsonwebtoken jjwt-api/impl/jackson | 0.11.5 | all 4 non-Eureka services | yes (deprecated `parserBuilder` API) |
| com.bucket4j bucket4j-core | 8.10.1 | gateway | yes (RateLimitFilter) |
| org.apache.tika tika-core | 2.9.1 (user) / 3.0.0 (media) | user, media | yes (MIME detection) |
| org.apache.tika tika-parsers-standard-package | 3.0.0 | media | **no** (only `Tika.detect` used, heavy dep) |
| io.projectreactor.kafka reactor-kafka | 1.3.25 | product | **no** (no import found) |
| org.springdoc springdoc-openapi-starter-webmvc-ui | 2.7.0 | user | no annotations; UI path is blocked by security (inferred) |
| me.paulschwarz spring-dotenv | 4.0.0 | user | implicit `.env` loader |
| com.cloudinary cloudinary-http44 | 1.30.0 | user | **no** |
| spring-cloud-starter-openfeign + feign-httpclient | BOM | user | **no** `@FeignClient` (only `@EnableFeignClients`) |
| spring-boot-starter-oauth2-resource-server | BOM | product, media | **no** (custom JWT filter used) |
| spring-kafka | BOM | user, product, media | yes |
| spring-boot-starter-data-mongodb (servlet) / -reactive | BOM | user, media / product | yes |
| spring-boot-starter-web / webflux | BOM | user, media / product, gateway | yes |
| spring-cloud-starter-gateway (WebFlux) | 2025.0.1 BOM | gateway | yes |
| spring-cloud-starter-netflix-eureka-client / -server | BOM | all | yes |
| lombok | BOM | user, product, media | yes |
| spring-boot-starter-actuator | BOM | gateway, user, product, media | yes |
| spring-boot-devtools | BOM | gateway, user, product, media | runtime-optional |

### 2.3 Frontend (`frontend/package.json`)
| Item | Version |
|---|---|
| Angular (core/common/forms/router/animations) | ^20.3.0 |
| @angular/cli, @angular/build | ^20.3.6 |
| UI library | @angular/material + @angular/cdk ^20.2.14; lucide-angular ^0.577.0 |
| rxjs / zone.js / TypeScript | ~7.8.0 / ~0.15.0 / ~5.9.2 |
| Test tooling | karma ~6.4, jasmine-core ~5.9, karma-coverage ~2.2, karma-chrome-launcher ~3.2 (builder `@angular/build:karma`, no `karma.conf.js`) |
| Docker build node | node:20-alpine |

### 2.4 Build tooling
- **5 independent single-module Maven projects.** There is no root/aggregator POM and no shared parent; each one inherits `spring-boot-starter-parent` directly.
- Every service has a Maven wrapper (`mvnw`, `.mvn/wrapper/maven-wrapper.properties`). The gateway's wrapper pins Maven 3.9.11; the other four pin 3.9.12.
- groupIds are inconsistent: `com.buy01` for 4 services and `com.buy-01-backend` for discovery. The discovery artifactId is `eurika-server` (typo).
- Every version is `0.0.1-SNAPSHOT`. No `<distributionManagement>`, no JaCoCo, no Sonar or surefire config.
- `Makefile` loops `./mvnw clean install -DskipTests` per service (`make install`).

---

## 3. Services

### 3.1 user-service
| Item | Value |
|---|---|
| Purpose | Registration, login (JWT issuance), profile, avatar storage, user-deleted event |
| Port | 8081 (`backend/user-service/src/main/resources/application.yaml`) |
| GAV | `com.buy01:user:0.0.1-SNAPSHOT` |
| Stack | Spring MVC (servlet), blocking Mongo |
| Layout | Hexagonal: `domain/port/{in,out}`, `application/service`, `infrastructure/adapters` |

**Endpoints** (`infrastructure/adapters/in/web/AuthController.java`, `UserController.java`, `config/WebConfig.java`)
| Method | Path | Auth (service) | Gateway | Role |
|---|---|---|---|---|
| POST | `/users/auth/register` (multipart: name,email,password,role,avatar?) | public | public | any. **The client chooses its own role (CLIENT/SELLER)** |
| POST | `/users/auth/login` (JSON) | public | public | – |
| GET | `/users/avatars/**` (static files from `app.avatar.upload-dir`) | public | public | – |
| GET | `/users` (returns "user service is working") | JWT | JWT | any |
| GET | `/users/me` | JWT | JWT | any |
| PUT | `/users/me` (multipart: name?, email?, avatar?) | JWT | JWT | any (self) |
| DELETE | `/users/me` | JWT | JWT | any (self) |
| GET | `/users/id/{id}` | JWT | JWT | any. **Returns any user's email** |
| GET | `/users/exists/{id}` | JWT | JWT | any |

**Document** `users` (`infrastructure/adapters/out/persistence/entity/UserEntity.java`): `id: UUID`, `name`, `email`, `password` (bcrypt), `role: Role{CLIENT,SELLER}`, `avatar`, `createdAt: Instant`, `updatedAt: Instant`. There is **no unique index on email**: uniqueness relies on an `existsByEmail` check, which can race.

**Config keys**: `server.port`, `eureka.client.service-url.defaultZone=${EUREKA_URL}`, `spring.servlet.multipart.max-file-size/max-request-size=10MB`, `spring.data.mongodb.{host,port,database,username,password=***,authentication-database}`, `spring.kafka.bootstrap-servers`, `security.jwt.{private-key=***,public-key=***,expiration=3600000,refresh-expiration}`, `media.service.url` (unused), `app.avatar.upload-dir=./uploads/avatars`, `management.*`. The datasource password has a hardcoded fallback default (see §5.5).

**Communication**: Kafka producer → topic `user-deleted` (`application/service/UserServiceImpl.java`). No outbound REST calls. Feign is enabled but there are no clients.

**Avatar validation** (`application/service/AvatarServiceImpl.java`): 2 MB max; Tika magic-byte detection against an allow-list of jpeg/png/gif/webp; stored as `UUID + original extension` on the local filesystem.

### 3.2 product-service
| Item | Value |
|---|---|
| Purpose | Product CRUD, paginated listing, image aggregation from media-service |
| Port | 8082 |
| GAV | `com.buy01:product:0.0.1-SNAPSHOT` |
| Stack | WebFlux + reactive Mongo |

**Endpoints** (`infrastructure/web/controller/ProductController.java`; security in `infrastructure/config/SecurityConfig.java`)
| Method | Path | Auth (service) | Gateway | Role |
|---|---|---|---|---|
| GET | `/products?beforeTime=ISO` (10 items, keyset on createdAt) | public | public | – |
| GET | `/products/{id}` (product + media IDs) | public | public | – |
| GET | `/products/user?beforeTime=` | JWT | **public** at gateway (`GET /products/**`); the service still requires JWT | any |
| POST | `/products` (JSON `ProductCreateRequest`) | JWT | JWT | `SELLER` |
| PUT | `/products/{id}` | JWT | JWT | `SELLER` + owner (`ProductServiceImpl.updateProduct`) |
| DELETE | `/products/{id}` | JWT | JWT | `SELLER` + owner (`ProductServiceImpl.deleteProduct`) |

Validation (`infrastructure/web/dto/ProductCreateRequest.java`): name 3–100, description 10–500, price 1–10000, quantity 0–1000, imagesIds 1–10.

**Document** `products` (`infrastructure/persistence/mongo/ProductDocument.java`): `id: String(ObjectId)`, `name`, `description`, `price: double`, `quantity: int`, `user_id`, `created_at`, `updated_at`. **Image IDs are not stored here**; they live only in media-service.

**Config keys**: `spring.data.mongodb.*` (password=***, with a hardcoded fallback default), `spring.kafka.bootstrap-servers`, `kafka.topics.user-deleted`, `eureka.*`, `security.jwt.public-key=***`, `management.*`.

**Communication**:
- REST via `@LoadBalanced WebClient` to `http://MEDIA-SERVICE` (`infrastructure/config/BeanConfiguration.java`, `application/service/ImageService.java`): `GET /media/product/{id}` and `POST /media/batch`.
- Kafka produces `images-linked` and `product-deleted`, and consumes `user-deleted`.

### 3.3 media-service
| Item | Value |
|---|---|
| Purpose | Image upload/serve/delete; link status (PENDING→LINKED); orphan cleanup |
| Port | 8083 |
| GAV | `com.buy01:media:0.0.1-SNAPSHOT` |
| Stack | Spring MVC (servlet), blocking Mongo. **The README claims WebFlux + GridFS; both are false** |

**Endpoints** (`infrastructure/web/controller/MediaController.java`; security in `infrastructure/config/SecurityConfig.java`)
| Method | Path | Auth (service) | Gateway | Role |
|---|---|---|---|---|
| GET | `/media` (health string) | public | public | – |
| GET | `/media/user` (echo principal) | public → **NPE/500 when anonymous** | public | – |
| GET | `/media/{id}` (file stream, 1-day public cache) | public | public | – |
| GET | `/media/product/{productId}` (list of media IDs) | public | public | – |
| POST | `/media/batch` `{productIds:[]}` → `{productId: firstMediaId}` | public | public | – |
| POST | `/media` multipart `files[]` → `[{imagesId,status}]` | JWT | **public** (`/media/**` any method) | any authenticated (**CLIENT can upload**) |
| DELETE | `/media/{id}` | JWT | public | owner (`MediaServiceImpl.deleteById`) |

**Document** `medias` (`infrastructure/persistence/MediaDocument.java`): `id`, `image_path`, `product_id`, `user_id`, `status: FileStatus{PENDING,LINKED}`, `created_at`.

**Config keys**: `spring.servlet.multipart.max-file-size=2MB`, `max-request-size=50MB`, `spring.data.mongodb.*` (password=***, with a hardcoded fallback), `spring.kafka.bootstrap-servers`, `security.jwt.public-key=***`, `eureka.*`, `management.*`. It also has `logback-spring.xml` and `banner.txt`.

**Communication**: Kafka consumer only (`images-linked`, `product-deleted`). `@Scheduled` `OrphanedMediaCleanupJob` runs hourly and deletes PENDING media older than **1 minute**.

### 3.4 Actuator exposure
| Service | `management.endpoints.web.exposure.include` | health details |
|---|---|---|
| api-gateway | `"*"` (includes `/actuator/gateway`, `env`, `heapdump`) | always |
| user / product / media | `"*"` | always |
| discovery-server | default (`health` only) | default |

Exposure of actuator through the gateway:
- On user and media, `/actuator/**` falls under `anyRequest().authenticated()`. On product it falls under `anyExchange().authenticated()`.
- The gateway itself does **not** protect its own `/actuator/**`: its `SecurityConfig` is `permitAll`, and port 8080 is published on `127.0.0.1`.

---

## 4. Infrastructure services

### 4.1 API Gateway (`backend/api-gateway`)
- **Routes** (`application.yaml`, `spring.cloud.gateway.server.webflux.routes`):
  | id | uri | predicate | filters |
  |---|---|---|---|
  | user-service | `lb://USER-SERVICE` | `Path=/users/**` | `Authentication` |
  | product-service | `lb://PRODUCT-SERVICE` | `Path=/products/**` | `Authentication` |
  | media-service | `lb://MEDIA-SERVICE` | `Path=/media/**` | `Authentication` |

  `config/GatewayConfig.java` is fully commented-out dead code.
- **Filters**:
  - `filter/AuthenticationGatewayFilterFactory.java`: route filter. It works from a public-endpoint allow-list, validates `Bearer` RS256, has an optional `requiredRole` that no route uses, and adds `X-User-Id`/`X-User-Role` headers. No downstream service reads these headers; downstream services re-validate the JWT themselves.
  - `filter/LoggingFilter.java`: global filter that logs **all request headers, including Authorization tokens**, at INFO.
  - `filter/RateLimitFilter.java`: WebFilter using a Bucket4j bucket per IP (100 req/min). The key comes from `X-Forwarded-For`, which is spoofable. The map is unbounded and in-memory.
- **User existence check**: `WebClient.builder()` is **not** `@LoadBalanced`, so `http://USER-SERVICE/...` cannot resolve. The target endpoint also requires JWT and no token is forwarded. Every check therefore errors, and `onErrorResume` lets the request through. Net effect: the check is dead, and tokens of deleted users stay valid until they expire.
- **CORS** (`config/CorsConfig.java`): origins `http(s)://localhost[:4200]`, `https://frontend:443`, `http://127.0.0.1:5500`; methods GET/POST/PUT/DELETE/OPTIONS/PATCH; credentials true; maxAge 3600. The production domain is not listed; this works only because traffic is same-origin through nginx (inferred).
- **Security**: `config/SecurityConfig.java` has CSRF off and `anyExchange().permitAll()`. All auth is done in the route filter.
- **TLS**: `server.ssl` uses PKCS12 `classpath:edge-service.p12`, with `${SSL_PASSWORD}` as the keystore password. The Dockerfile generates a self-signed certificate at build time. HTTP/2 is on.
- **Misc**:
  - `TestController` (`GET /` returns a string).
  - Empty classes `security/JwtConverter.java` and `exception/GatewayException.java`.
  - `spring.cloud.gateway.server.error.include-stacktrace: always` is probably not a valid key (inferred).
  - `logging.level.org.springframework.web: DEBUG`.

### 4.2 Discovery (`backend/discovery-server`)
- Netflix **Eureka** server (`EurikaServerApplication.java`, `@EnableEurekaServer`), port 8761, with `register-with-eureka=false` and `fetch-registry=false`.
- Registered clients: `GATEWAY`, `USER-SERVICE`, `PRODUCT-SERVICE`, `MEDIA-SERVICE`. Each sets `EUREKA_URL=http://discovery-server:8761/eureka` in compose.
- The service runs on Spring Boot 4.0.0 / Java 17, unlike all the others.

### 4.3 Kafka
- Broker: `apache/kafka:latest`, single-node KRaft (`docker-compose.yml`), 3 partitions by default, RF 1.
- **No topic creation**: topics are auto-created and there are no `NewTopic` beans.
- **No DLQ, no error handler, no schema registry.** Consumers use auto-commit.
- `KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://${KAFKA_BROKER}`. `.env.example` also defines an unused `KAFKA_ZOOKEEPER`.

| Topic | Producer (file) | Consumer (file, group) | Payload (JSON, `JsonSerializer`) |
|---|---|---|---|
| `user-deleted` | user `UserServiceImpl.sendKafkaUserDeleted` | product `messaging/UserListener` (`product-service-group`) | `{userId: UUID}` (consumer reads `String`) |
| `images-linked` | product `ImageService.sendKafkaImageLinked` (on create and update) | media `messaging/MediaStatusListener.onImagesLinked` (`media-service-group`) | `{productId, mediaIds: [String]}` |
| `product-deleted` | product `ImageService.sendKafkaProductDeleted` | media `MediaStatusListener.onProductDeleted` | `{productId}` |

Cascade: user deleted → product deletes all of that user's products → product-deleted → media *attempts* deletion, which is broken (§10).

### 4.4 Media storage
| Aspect | user avatars | product images |
|---|---|---|
| Location | local filesystem `./uploads/avatars` → bind mount `./uploads/avatars:/app/uploads/avatars` | local filesystem, relative `uploads/` (`MediaServiceImpl.UPLOAD_DIR`) → container path `/app/uploads/`. **Compose mounts `./uploads/media:/app/uploads/media`, so media files are NOT persisted** |
| MinIO / GridFS | not found | not found |
| Size limit | 2 MB in code + 10 MB multipart | 2 MB in code + `max-file-size: 2MB`, `max-request-size: 50MB` |
| MIME validation | Tika magic-byte detection, allow-list jpeg/png/gif/webp | same (Tika) |
| Filename | `UUID + client extension` | `UUID + client extension`; Mongo ID ≠ file UUID |
| Frontend check | `shared/utils/media-validation.utils.ts`: `file.type` + 2 MB | same |

---

## 5. Security

### 5.1 Auth flow end to end
1. **Register**: `POST /api/users/auth/register` (multipart). nginx strips `/api` and forwards to the gateway (public route), then to user-service. The client picks its role, the password is hashed with BCrypt and the user is saved. Response: `UserResponse`, with no token.
2. **Login**: `POST /users/auth/login`. `AuthServiceImpl.login` checks BCrypt. `security/JwtUtil.generateToken` signs an **RS256** JWT with `sub=userId`, `email`, `role`, `iat`, and `exp` (default 1 h via `JWT_EXPIRATION`). The response is `{token, expiresAt}`. A refresh token is **not implemented** even though the `refresh-expiration` key exists.
3. **Storage on the client**: `localStorage` (`auth_token`, `auth_expires_at`) in `core/auth/services/auth.service.ts`.
4. **Propagation**: `authInterceptor` prefixes the URL with `/api` and adds `Authorization: Bearer`.
5. **Gateway validation**: RS256 signature check with `JWT_PUBLIC_KEY`, then headers are added. The user-exists check is dead (§4.1).
6. **Service validation**: each service has its own `JwtAuthenticationFilter` + `JwtUtil` (copy-pasted ×4). They re-verify the signature with the public key and build `UserPrincipal(id,email,role)` with authority `ROLE_<role>`.
7. **Service-to-service**: product→media calls carry **no token**. They work only because the media GET and batch endpoints are public.

### 5.2 Hashing, roles, ownership
- Password hashing: `BCryptPasswordEncoder` (default strength 10) in `user-service/.../infrastructure/security/BcryptPasswordEncoder.java`.
- Roles: `Role{CLIENT, SELLER}` (`user-service/.../domain/model/Role.java`). There is no ADMIN role, and **any user can self-register as SELLER**.
- Enforcement:
  | Rule | Where |
  |---|---|
  | SELLER for POST/PUT/DELETE `/products/**` | `product-service/.../infrastructure/config/SecurityConfig.java` |
  | Product owner check | `product-service/.../application/service/ProductServiceImpl.java` (`updateProduct`, `deleteProduct`). A mismatch returns 400 `IllegalArgumentException` instead of 403 |
  | Media owner check on delete | `media-service/.../application/MediaServiceImpl.deleteById` |
  | Media upload | authenticated only, any role |
  | Linking images to a product | **no ownership check**: any seller can link another user's PENDING media IDs to their own product (`MediaStatusListener.onImagesLinked`) |
  | Frontend | `roleGuard(['SELLER'])` on `/products/new` and `/products/:id/edit` |

### 5.3 HTTPS
- The gateway serves HTTPS on 8080 with a self-signed PKCS12 cert generated in `backend/api-gateway/Dockerfile`; `scripts/setup-ssl.sh` provides the same for local runs.
- The frontend nginx container terminates TLS on 443 with a self-signed cert generated in `frontend/Dockerfile`. It redirects 80→443 and proxies `/api/` → `https://api-gateway:8080/` with `proxy_ssl_verify off`.
- In production, host nginx terminates Let's Encrypt TLS (`docs/AI-CONTEXT.md` §8). Inside the stack there are therefore two layers of self-signed TLS.
- Internal service-to-service traffic is plain HTTP. Kafka and Mongo connections are unencrypted.

### 5.4 Other risks
- `LoggingFilter` logs bearer tokens (`api-gateway/.../filter/LoggingFilter.java`).
- JWT in `localStorage` (XSS-exposed).
- The `X-User-*` headers are not stripped from inbound requests. They are currently unused downstream, so the risk is latent.
- `GET /users/id/{id}` exposes any user's email to any authenticated user.
- Kafka consumers use `addTrustedPackages("*")` (`product/.../KafkaConsumerConfig.java`, `media/.../KafkaConsumerConfig.java`).
- Exception messages are returned to clients: `"Authentication failed: " + e.getMessage()` in the gateway, and `Throwable::getMessage` in the default templates.

### 5.5 Hardcoded secrets (locations only)
| Location | What |
|---|---|
| `backend/media-service/.env` (**tracked in git**) | Mongo user/password/db/port |
| `.env.example` | non-empty `SSL_PASSWORD`, `USER_MONGO_PASSWORD`, `PRODUCT_MONGO_DB_PWD`, `MEDIA_MONGO_DB_PWD` default values |
| `backend/media-service/src/main/resources/application.yaml` | fallback default for `MEDIA_MONGO_DB_USER` / `MEDIA_MONGO_DB_PWD` |
| `backend/product-service/src/main/resources/application.yaml` | fallback default for `PRODUCT_MONGO_DB_USER` / `PRODUCT_MONGO_DB_PWD` |
| `backend/user-service/src/main/resources/application.yaml` | fallback default for `USER_MONGO_USERNAME` / `USER_MONGO_PASSWORD` |
| `backend/api-gateway/Dockerfile`, `frontend/Dockerfile` | `ARG SSL_PASSWORD` default value |
| **Git history** | root `.env` committed until `e606c48 delete .env file`; that version contains non-empty `JWT_PRIVATE_KEY`, `JWT_PUBLIC_KEY` and Mongo passwords |
| **Git history** | `backend/api-gateway/src/main/resources/gateway-keystore.p12`, `frontend/key.pem`, `frontend/cert.pem` were committed at some point |

Root `.env` is currently untracked and ignored (`.gitignore`). Any key that was in the historical `.env` must be considered compromised.

---

## 6. Frontend (`frontend/`)

- **Structure**: Angular 20, **standalone** components, no NgModules. Bootstrapped in `src/main.ts` with `src/app/app.config.ts`, which provides `provideRouter(withComponentInputBinding)`, `provideHttpClient(withInterceptors([authInterceptor, errorInterceptor]))` and animations. The code mixes signals with BehaviorSubject (`AuthService`). `tsconfig.json` has strict mode on.
- **Routes** (`src/app/app.routes.ts`, all lazy `loadComponent`):
  | Path | Component | Guard |
  |---|---|---|
  | `` | ProductList | – |
  | `login`, `register` | LoginComponent, RegisterComponent | `guestGuard` |
  | `profile` | ProfileComponent | `authGuard` |
  | `products/new`, `products/:id/edit` | ProductForm | `roleGuard(['SELLER'])` |
  | `products/list` | ProductList | – |
  | `dashboard` (`data.myProducts=true`) | ProductList | `authGuard` |
  | `products/:id` | ProductDetail | – |
  | `**` | redirect `''` | – |
- **Guards**: `core/auth/guards/{auth,guest,role}.guard.ts`. `roleGuard` reads the role from the user signal, which loads asynchronously, so a hard refresh on a SELLER route can redirect wrongly (inferred).
- **Interceptors**:
  - `auth.interceptor.ts`: base URL prefix plus the bearer token.
  - `error.interceptor.ts`: logs out on 401, on **429** and on 404 for `/me`.
  - `token-interceptor.ts`: unused duplicate.
  - `core/auth/services/token.service.ts` and `core/models/auth.model.ts` are empty files.
- **API services**:
  | Service | Calls |
  |---|---|
  | `core/auth/services/auth.service.ts` | `/users/auth/login`, `/users/auth/register`, `/users/me` |
  | `core/services/user.service.ts` | PUT and DELETE `/users/me` |
  | `core/services/product.service.ts` | `/products`, `/products/user`, `/products/{id}` |
  | `core/services/media.service.ts` | POST `/media`, DELETE `/media/{id}` |

  `ProductService.getProduct` does `JSON.parse(r.imagesIds[0])` to work around a backend serialization bug (§10). It also **injects fake data**: `rating: 4.8`, `reviewsCount: 124`, `oldPrice = price + 20.4`.
- **Forms**: Reactive Forms with `FormBuilder` + `Validators` in login, register, user-profile and create-product. Frontend and backend rules disagree:
  | Field | Frontend | Backend |
  |---|---|---|
  | description min | 20 | 10 |
  | price min | 0.01 | 1 |
  | price/quantity max | none | 10000 / 1000 |
  | login password max | 100 | 30 |

  `models/product.model.ts` `ProductCreateRequest` has a stray `int: number` field.
- **Environment**: `src/environments/environment.ts` and `environment.prod.ts` are identical: `backendUrl='/api'`, `mediaUrl='/api/media'`, `avatarUrl='/api/users'`.
- **Build**: the `angular.json` production config has budgets of 500 kB warn / 1 MB error. `frontend/Dockerfile` builds with `--base-href=/`, while `index.html` has `<base href="/">`. The VM deployment needs `/app/` (`docs/AI-CONTEXT.md` §9).
- `frontend/README.md` is stale: it describes mock JWT, `/seller/...` routes and test accounts that do not exist.

---

## 7. Tests

### 7.1 Backend
| Service | Test files | Content | Count |
|---|---|---|---|
| api-gateway | `src/test/java/.../GatewayApplicationTests.java`, plus a stray `src/test/index.html` | `@SpringBootTest contextLoads()` | 1 |
| discovery-server | **not found** | – | 0 |
| user-service | `UserApplicationTests.java` | `contextLoads()` | 1 |
| product-service | `ProductApplicationTests.java`; `MongoConnectionTest.java` (fully commented out) | `contextLoads()` | 1 |
| media-service | `MediaApplicationTests.java` | `contextLoads()` | 1 |

- Frameworks available: JUnit 5, Spring Boot Test, Mockito (via starter-test) and reactor-test (product, media). Testcontainers and embedded Kafka/Mongo are **not found**.
- Coverage of business logic: **0**.
- Each `contextLoads` needs live Mongo, Kafka, Eureka and JWT env vars, so they **will fail in a clean CI agent** unless the infrastructure is provided (inferred). That is likely why every build uses `-DskipTests`.

### 7.2 Frontend
- `*.spec.ts` files: **0**. Running `ng test` would find no specs.
- `karma.conf.js`: not found. The test builder `@angular/build:karma` is configured in `angular.json`; `tsconfig.spec.json` exists.
- A headless Chrome launcher config for CI is not found.

### 7.3 Coverage tooling
- JaCoCo: **not found** in any POM.
- `karma-coverage` is installed but not configured with reporters (lcov) for Sonar.
- SonarQube config (`sonar-project.properties`, sonar-maven-plugin): **not found**.

---

## 8. Containerization and run setup

### 8.1 Dockerfiles
| Service | File | Base images | Multi-stage | Notes |
|---|---|---|---|---|
| api-gateway | `backend/api-gateway/Dockerfile` | maven:3.9-eclipse-temurin-21 → eclipse-temurin:21-jre | yes | apt installs openssl; self-signed cert baked into the jar; `-DskipTests`; runs as root; no HEALTHCHECK |
| discovery-server | `backend/discovery-server/dockerfile` (lowercase) | maven:3.9-eclipse-temurin-17 → eclipse-temurin:17-jre | yes | Java 17 |
| user/product/media | `backend/*/Dockerfile` | temurin-21 → 21-jre | yes | no dependency layer caching (`COPY pom.xml` + `COPY src` then package; no `dependency:go-offline`); root user; no HEALTHCHECK |
| frontend | `frontend/Dockerfile` | node:20-alpine → nginx:alpine | yes | `npm ci`; self-signed cert via openssl; `--base-href=/` |

`.dockerignore` (root) only lists `.env` and `**/uploads`. The services have no `.dockerignore` of their own, and the build contexts are the service directories (inferred: `target/` gets copied into the context, which is harmless).

### 8.2 docker-compose
- **Root `docker-compose.yml`**, single network `ecommerce-network` (bridge):
  | Service | Image/build | Published port | Volumes | depends_on | Healthcheck |
  |---|---|---|---|---|---|
  | discovery-server | build | none | – | – | no |
  | kafka | apache/kafka:latest | none | kafka-data | – | no |
  | mongo-user | mongo:7 | none | mongo-user-data | – | mongosh ping |
  | mongo-media (container_name `${MEDIA_MONGO_DB_NAME}`) | mongo:7 | none | media-mongo-data | – | yes |
  | mongo-product (container_name `${PRODUCT_MONGO_DB_NAME}`) | mongo:7 | none | product-mongo-data | – | yes |
  | api-gateway | build (arg SSL_PASSWORD) | `127.0.0.1:${GATEWAY_PORT}:8080` | – | discovery, user, product, media (started only) | no |
  | user-service | build | none | `./uploads/avatars:/app/uploads/avatars` | mongo-user healthy, discovery, kafka | no |
  | product-service | build | none | – | mongo-product healthy, discovery, kafka | no |
  | media-service | build | none | `./uploads/media:/app/uploads/media` (wrong path, §4.4) | mongo-media healthy, discovery, kafka | no |
  | frontend | build (arg SSL_PASSWORD) | `127.0.0.1:4200:443` | – | api-gateway | no |
  | jenkins | jenkins/jenkins:lts | `127.0.0.1:8090:8080` | jenkins-data | – | no |
- **Env vars**: `TZ`, `SPRING_PROFILES_ACTIVE=docker` (no `application-docker.yaml` exists, so this has no effect), `EUREKA_URL`, `JWT_PUBLIC_KEY=***`, `JWT_PRIVATE_KEY=***` (user only), `JWT_EXPIRATION`, `SSL_PASSWORD=***`, `RATE_LIMIT_*`, `*_MONGO_HOST/PORT/DB/USER/PASSWORD=***`, `KAFKA_BROKER=kafka:9092`, `MEDIA_SERVICE_URL`.
- **Jenkins container**: no Docker socket, no JDK/Maven/Node tools and no `--prefix=/jenkins`. It cannot build images as-is.
- **Per-service `compose.yaml`** (`backend/media-service/compose.yaml`, `backend/product-service/compose.yaml`): standalone dev Mongo with `mongo:latest`. These are legacy files.

### 8.3 How to run today (from `README.MD` + `Makefile`, completed by inference)
1. `cp .env.example .env` (or let the script do it), then `make env`. This runs `scripts/generate-keys.sh`, which generates an RSA-2048 pair and writes base64 DER into `JWT_PRIVATE_KEY`/`JWT_PUBLIC_KEY`.
2. Fill the Mongo credentials and `SSL_PASSWORD` in `.env`.
3. `make docker-build`. This runs `scripts/setup-ssl.sh`, then `docker compose build`.
4. `make docker-up`. This starts all 11 containers, including Jenkins.
5. Open `https://localhost:4200` (self-signed). The API is at `https://localhost:8080`, or at `/api/` via the frontend nginx.
6. Dev mode: `make docker-up-dev` starts the infra only, then run each service with `./mvnw spring-boot:run`. **This mode is broken (inferred)** for three reasons:
   - the compose file publishes no Mongo or Kafka ports;
   - the yaml defaults expect Mongo on 27017/27018/27019;
   - the gateway needs `edge-service.p12` on its classpath.
7. Stop: `make docker-down`.

### 8.4 README quality (`README.MD`, 303 lines)
- **Covers**: services and ports, architecture diagram, hexagonal pattern, make targets, actuator URLs, links to the `docs/` set.
- **Wrong**:
  - Mongo ports are swapped (README: product 27018 / media 27019; yaml: media 27018 / product 27019).
  - Media is described as "WebFlux" and "GridFS"; it is servlet + filesystem.
  - It says "Spring Boot 3.x" (discovery is 4.0.0).
  - The clone URL is lowercase.
  - Actuator links point at ports that are not published.
- **Missing**:
  - prerequisites for `.env` values;
  - local dev without Docker;
  - how to run tests (there are none);
  - the CI/CD flow;
  - the deployment topology (`/app`, `/api`, `/jenkins`), which is documented only in `docs/AI-CONTEXT.md`;
  - troubleshooting, API examples at root, license.
- `docs/` holds 11 extensive docs, partially overlapping (3 gateway guides) and partially aspirational (`security_roadmap.md`, `cicd.md`).

---

## 9. Readiness assessment

### 9.1 mr-jenk (Jenkins CI/CD)
| Item | State |
|---|---|
| Jenkinsfile | **not found**. A draft exists in `docs/cicd.md` §3.2 (tools JDK-21, Maven-3.9, Node-20; githubPush) |
| Build commands | Ready per service: `./mvnw -B clean package` (each dir), `npm ci && npx ng build` |
| Unit tests | **Missing**. The only backend tests are `contextLoads`, which need live infra (will fail on an agent). There are no frontend specs |
| Test reports | Surefire XML will exist; Karma JUnit reporter not configured |
| Dockerfiles | Present for all 6 images; multi-stage; they rebuild with Maven inside Docker (duplicate work vs CI build, no layer cache) |
| Health checks | Actuator present on 4/5 services. No container HEALTHCHECK on app services; `depends_on` only waits for Mongo |
| Image tagging/registry | none (compose `build:` only, no `image:` names/tags). Rollback strategy only in docs |
| Jenkins runtime | container lacks Docker CLI/socket, tools and subpath prefix; `origin/jenkins-installation` has 1 unmerged commit |
| Secrets for CI | must be injected as Jenkins credentials (`.env` is untracked); the JWT keypair and SSL password are needed at build/run |
| Notifications | not configured (planned in `docs/cicd.md` §1.6–1.7) |

### 9.2 safe-zone (SonarQube): likely findings
- **Coverage ≈ 0%.** A quality gate on new-code coverage will fail. JaCoCo and the lcov reporter must be added first.
- **Duplication**:
  - `JwtUtil` ×4 (gateway/user/product/media, near-identical);
  - `JwtAuthenticationFilter` ×3;
  - `GlobalExceptionHandler` + `ProblemTemplate` + `DetailGenerator` duplicated between product and media;
  - `KafkaConsumerConfig` and `KafkaProducerConfig` boilerplate;
  - the user-service `GlobalExceptionHandler` repeats the same `ProblemDetail` block ~25 times;
  - `ProductServiceImpl.getProductsList` and `getUserProductsList` are identical;
  - the gateway's `onError` is duplicated in the RateLimit, Auth and GlobalErrorHandler code.
- **Bugs/smells**:
  - `System.out` / `System.err` (7 occurrences);
  - unused fields (`ProductServiceImpl.kafkaTemplate`, `TOPIC_*` constants);
  - empty classes (`JwtConverter`, `GatewayException`, `BcryptPasswordEncoderAdapter`, `JwtTokenGeneratorAdapter`, `adapters/out/security/config/SecurityConfig`, `BeanConfiguration`, `MongoConfig` in user-service);
  - commented-out code (`GatewayConfig.java`, `MongoConnectionTest.java`, `Media.Builder.build`);
  - parameter naming (`ProductId`), class naming (`Faileduploadedfile`), typos (`EVEREY_HOURE`, `getProdutImages`, `prodctList`, `EurikaServerApplication`);
  - `ResponseEntity<?>` wildcards;
  - field `@Autowired` in the gateway filter;
  - the null check after dereference in `MediaController.getImagePatch`;
  - `float` vs `double` comparison in `Product.validate`;
  - a `.subscribe()` inside a Kafka listener (fire-and-forget).
- **Security hotspots**:
  - CSRF disabled ×4 (acceptable for stateless JWT, but it will be flagged);
  - CORS with credentials;
  - `permitAll` in the gateway;
  - actuator `include: "*"`;
  - hardcoded credential defaults in yaml;
  - logging of tokens;
  - `addTrustedPackages("*")`;
  - weak password policy (min 6);
  - `proxy_ssl_verify off`;
  - Dockerfiles running as root;
  - the `ARG SSL_PASSWORD` default;
  - `include-stacktrace: always`.
- **Frontend**: `any` types in `product.service.ts`, `console.log`/`console.error`, empty files, the unused `token-interceptor.ts`.

### 9.3 nexus: "Java 11" constraint
- Spring Boot 3.x requires **Java 17+**, and Boot 4.0 requires 17+. Spring Cloud 2024.x and 2025.x likewise.
- The code uses Java 16+ features heavily: `record` (DTOs, events, commands), `Stream.toList()`, text blocks (gateway `RateLimitFilter`), and `jakarta.*` namespaces.
- **Compiling to Java 11 is impossible without a downgrade to Spring Boot 2.7.x + Spring Cloud 2021.0.x.** That downgrade means:
  - rewriting all records to classes;
  - `javax.*` imports;
  - the `ProblemDetail` API does not exist in Boot 2.7;
  - the Security 5.x DSL;
  - the new gateway property prefix `spring.cloud.gateway.server.webflux` is 2025.x-only.
- Boot 2.7 is EOL (OSS) since Nov 2023. **Recommendation (inferred):** keep 17/21 and interpret the constraint as the Jenkins/Nexus host minimum. Otherwise this is a multi-day rewrite.
- **Versioning state**: all artifacts are `0.0.1-SNAPSHOT`. No `distributionManagement`, no release plugin or CI-friendly `${revision}`, no `settings.xml`, no git tags. Publishing to Nexus needs:
  - distributionManagement (snapshots and releases repos);
  - credentials in Jenkins;
  - consistent groupIds (`com.buy01` vs `com.buy-01-backend`);
  - a version strategy;
  - optionally, Docker images pushed to a Nexus Docker registry, plus an npm proxy for the frontend.

### 9.4 buy-02 features: where they fit
| Feature | Recommended placement | Rationale (from current code) |
|---|---|---|
| Cart | **New `order-service`** (cart as a pre-order aggregate), or a small cart collection in it | user-service is identity-only; product-service is the seller catalog. Cart needs price/quantity snapshots from product (REST via `@LoadBalanced WebClient`, as `ImageService` already does) |
| Orders | **New `order-service`** (Mongo, hexagonal, same template) | Needs its own lifecycle (PENDING→CONFIRMED→CANCELLED…) and per-buyer and per-seller views. Emits `order-created`/`order-status-changed` on Kafka; product-service consumes them to decrement `quantity` (exists today but is never mutated). The `CLIENT` role finally gets a purpose |
| Search / filters | **Extend product-service** | It already owns `products`. Add Mongo text index + price/name filters to `SpringDataProductRepository`. Current pagination is keyset by `createdAt` (top 10); replace it with `Pageable` or keep keyset with filters. Store `imagesIds`/`mainImage` on product to avoid N calls to media |
| Analytics (seller/buyer stats) | Inside order-service (aggregations) **or** a separate consumer of order events | Depends on order data; product has no sales data. Mongo aggregation pipeline is sufficient at this scale |
| Profile pages (buyer/seller) | user-service + aggregation calls | Add endpoints, keeping auth uniform |

Prerequisites before buy-02:
- fix the `user-deleted` cascade and the media deletion bug;
- add a shared `common-security` module so the new service does not become the 5th copy of `JwtUtil`;
- add the gateway route `/orders/**`;
- add a role check for SELLER-only order views.

### 9.5 Maven multi-module parent POM: feasibility
Feasible, and it is a prerequisite for clean Jenkins, Sonar and Nexus. It requires:
1. A root `backend/pom.xml` (`packaging pom`, groupId `com.buy01`) that inherits `spring-boot-starter-parent` and lists the 5 modules.
2. **Aligning versions**: one Boot version, one Spring Cloud BOM and one Java version. Gateway is on 3.5.11 / 2025.0.1, discovery on 4.0.0 / 2025.1.0 / Java 17, the rest on 3.4.12 / 2024.0.2. 3.5.x + 2025.0.x is the lowest-risk target (inferred); the gateway's `server.webflux` route prefix already requires 2025.0.
3. Moving jjwt, tika, bucket4j and springdoc versions into `<dependencyManagement>`/properties. jjwt and tika currently differ across modules (tika 2.9.1 vs 3.0.0).
4. Centralizing plugins in `<pluginManagement>`: compiler + lombok, spring-boot-maven-plugin, jacoco, surefire/failsafe, sonar.
5. Optionally a `common` module for `JwtUtil`, `JwtAuthenticationFilter` (servlet and reactive variants), the ProblemDetail handler and event DTOs (`UserDeletedEvent` etc., currently duplicated with mismatched types).
6. One `mvnw` at `backend/` (remove 5 copies), and updating the `Makefile`, the Dockerfiles (build context `backend/` with `-pl <module> -am`, or copy jars built by CI) and `docs/cicd.md`.
7. Normalizing groupId and artifactIds (`eurika-server` → `discovery-server`).

### 9.6 ARM64 (Oracle Ampere A1)
- **Inferred**, from public Docker Hub manifests; not verified because no network calls were made: every base image used publishes `linux/arm64`.
  - `maven:3.9-eclipse-temurin-{17,21}`, `eclipse-temurin:{17,21}-jre`
  - `node:20-alpine`, `nginx:alpine`
  - `mongo:7` (needs ARMv8.2-A; Ampere Altra qualifies)
  - `apache/kafka:latest`, `jenkins/jenkins:lts`
- No native libraries in Java dependencies; Tika detect is pure Java.
- No `platform:` pins in compose, so images build natively on ARM.
- **Exception**: `scripts/update-java-to-21.sh` downloads `jdk-21_linux-x64_bin.tar.gz`, which is x86-only. It must be adapted for an ARM Jenkins agent.
- `apache/kafka:latest` and `mongo:latest` (per-service compose) are unpinned, so builds are not reproducible.

---

## 10. Issues and technical debt

### Critical
1. **Secrets in git history**: root `.env` with the JWT private key and DB passwords (until `e606c48`), plus `gateway-keystore.p12`, `frontend/key.pem` and `frontend/cert.pem`. `backend/media-service/.env` is still tracked. Fix: rotate the keys and passwords, then purge history or accept the leak.
2. **Gateway user-existence check is dead** (`api-gateway/.../AuthenticationGatewayFilterFactory.java`): the WebClient is not load-balanced, the call is unauthenticated, and errors are swallowed. Deleted users keep access until their token expires.
3. **Media files are not persisted**: `MediaServiceImpl.UPLOAD_DIR="uploads/"` writes to `/app/uploads/`, while compose mounts `/app/uploads/media`. Images are lost on container recreation.
4. **Product deletion never deletes media**: `MediaStatusListener.onProductDeleted` calls `deleteById(media.getImagePath())` (a path instead of an ID) and never deletes the file. Records and files leak.
5. **Orphan cleanup race**: `OrphanedMediaCleanupJob` deletes PENDING media older than **1 minute**. A seller who uploads, then fills the form for more than 1 minute while the hourly job fires, loses the images.
6. **Zero automated tests**, and the `contextLoads` tests need live infrastructure. This blocks a meaningful CI/Sonar gate.
7. **Self-assigned SELLER role** at registration (`RegisterRequest.role`). Any user can become a seller.

### Important
8. **Product→media JSON bug**: `ImageService.getProductImages` uses `bodyToFlux(String.class)` on a JSON array, which yields one string holding the whole array. The frontend compensates with `JSON.parse(r.imagesIds[0])` (`product.service.ts`).
9. **Media linking has no ownership check**: any seller can attach another user's PENDING media IDs to their own product.
10. **`ProductWebMapper.toResponse` omits `quantity`**, so create and update responses always return 0.
11. **Product update never unlinks removed images.** `Product.Builder(Product)` does not copy `imagesIds`.
12. **`Product.build()` calls `validate()`**, so mapping a stored document with invalid data, or a `null` quantity Integer→int, throws during reads.
13. **Ownership violations return 400, not 403** (`IllegalArgumentException("Not your product")`). "Product not found" also returns 400 instead of 404.
14. **Gateway marks all `/media/**` as public** (any method). The service still enforces JWT on POST/DELETE, but the gateway layer is bypassed. `GET /products/user` is also public at the gateway.
15. **`GET /media/user` returns 500** for anonymous callers (public endpoint dereferencing a null `Authentication`). `POST /media/batch` creates a `HashSet` before its null check, so a null body causes an NPE.
16. **Bearer tokens are logged** at INFO (`LoggingFilter`), and gateway web logging is at DEBUG.
17. **Version drift**: three different Boot/Cloud/Java combinations (§2.1), and `tika` 2.9.1 vs 3.0.0.
18. **No unique index on `users.email`**, so a race can create duplicates. `updateUser` lets the email change without a uniqueness check.
19. **Kafka**: no DLQ or error handler, auto-commit, `.subscribe()` inside the listener (errors lost), `trustedPackages("*")`, auto-created topics, `latest` image tag.
20. **Frontend base-href regression** (`85b3436`): `/app/` deployment is broken per `docs/AI-CONTEXT.md` §9.1. The error interceptor logs users out on 429.
21. **Dev mode without Docker is broken**: no published infra ports, mismatched default ports, gateway keystore required.
22. **Actuator `include: "*"`** on every service, and the gateway's actuator is unauthenticated.
23. **Fake data in the UI**: rating, reviews and oldPrice are hardcoded in `ProductService.getProduct`.

### Nice to have
24. Remove dead code and dependencies:
    - empty classes (§9.2) and `GatewayConfig.java`;
    - `token-interceptor.ts`, `token.service.ts`, `auth.model.ts`;
    - `ProductCreatedEvent`, and `ImageLinkedEvent` (duplicate of `ImagesLinkedEvent`);
    - `Batch.ImageResponse`;
    - `media.service.url`;
    - Feign, Cloudinary, reactor-kafka, tika-parsers and oauth2-resource-server;
    - the `TestController` and the `GET /users` / `GET /media` health strings.
25. Extract a shared security/error/event module (four `JwtUtil` copies).
26. Migrate jjwt 0.11.5 → 0.12.x (the `parserBuilder` API is deprecated). Parse claims once instead of 3–4 times per request.
27. Add container HEALTHCHECKs, run as a non-root user, cache Maven dependency layers, and give the per-service Dockerfiles a consistent filename case (`dockerfile` vs `Dockerfile`).
28. Fix the README errors (ports, GridFS/WebFlux claims), and rewrite `frontend/README.md` and the media `note.md` example response.
29. Align frontend and backend validation rules. Remove the `int` field from `ProductCreateRequest` (TS).
30. Rate limiter: bounded cache (Caffeine) and a trusted-proxy-aware client IP.
31. Naming and typos: `EurikaServerApplication`, `eurika-server`, `Faileduploadedfile`, `getProdutImages`, `EVEREY_HOURE`, `SpringDataProductRepository` in media-service.
32. Delete the legacy per-service `compose.yaml` files and the unused `KAFKA_ZOOKEEPER` and `SPRING_PROFILES_ACTIVE=docker` (no profile file).
