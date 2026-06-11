# BUY-01 Platform Context (Backend + Frontend + DevOps)

## 1. Project Overview

This project is a microservices-based e-commerce platform composed of:

- Angular frontend (served via Nginx inside Docker)
- Spring Boot microservices
- Spring Cloud Gateway (API Gateway)
- Eureka discovery server
- Kafka broker
- MongoDB databases (user, product, media)
- Jenkins CI/CD
- Host-based Nginx reverse proxy
- Laravel backend (legacy / must remain untouched at root `/`)

---

## 2. Architecture

### External entry point (single domain)
```

[https://sobol-pro.duckdns.org](https://sobol-pro.duckdns.org)

```

### Nginx (host-based reverse proxy)
Routes traffic to:

| Path        | Service                                                 |
| ----------- | ------------------------------------------------------- |
| `/`         | Laravel (PHP-FPM)                                       |
| `/api/`     | Spring Cloud Gateway (port 8080)                        |
| `/app/`     | Angular frontend (Docker container nginx on 4200 → 443) |
| `/jenkins/` | Jenkins (port 8090)                                     |

---

## 3. Docker Stack

### Core services

- discovery-server (Eureka)
- api-gateway (Spring Cloud Gateway)
- user-service
- product-service
- media-service
- kafka
- mongo-user
- mongo-product
- mongo-media
- frontend (Angular + Nginx)
- jenkins

### Networking
- All services use:
```

ecommerce-network (bridge)

```

### Exposure strategy

- Internal services: `expose` only (no public ports)
- External access only via:
  - api-gateway
  - frontend
  - jenkins
  - nginx host reverse proxy

---

## 4. API Gateway Configuration

Spring Cloud Gateway:

- Runs on port `8080`
- Uses Eureka service discovery
- Routes:

| Route           | Path           |
| --------------- | -------------- |
| user-service    | `/users/**`    |
| product-service | `/products/**` |
| media-service   | `/media/**`    |

### Security
- JWT-based authentication
- Public key injected via env
- Rate limiting enabled

### SSL
- SSL configured inside gateway (Spring Boot keystore)

---

## 5. Angular Frontend

### Build process
Multi-stage Docker build:

1. Node build stage
2. Nginx runtime stage with HTTPS

### Critical requirement

Frontend is served under:

```

/app/

```

So Angular must be built with:

```

--base-href /app/

```

Otherwise:
- JS chunks break
- assets resolve incorrectly
- blank page occurs

---

## 6. Jenkins

- Runs on port `8090`
- Exposed via nginx at `/jenkins/`

### Known limitation
Jenkins is NOT fully configured for subpath routing yet.

Likely requires:
- `--prefix=/jenkins`
or
- `JENKINS_PREFIX`

Without it:
- login redirects may break
- static resources may return 404

---

## 7. Laravel (Legacy System)

- Located on host filesystem:
```

/home/ubuntu/adherent-backend/public

```

- Served directly by Nginx
- Must remain untouched
- Root path `/` always reserved for Laravel

---

## 8. Nginx Role (Critical Layer)

Host nginx is the **single entry point**.

Responsibilities:

- TLS termination (Let's Encrypt)
- Path-based routing
- Reverse proxy to Docker services
- Separation between legacy + microservices

---

## 9. Current Known Issues

### 1. Angular frontend broken routing

**Symptom**
- `/app/` loads HTML
- JS chunks fail:
```

GET /chunk-xxxx.js 404

```

**Cause**
- Angular built with base href `/`
- Assets resolve to root instead of `/app/`

**Required fix**
- Rebuild frontend with:
```

--base-href /app/

```

---

### 2. Jenkins subpath routing broken

**Symptom**
- `/jenkins/` redirects to `/login`
- UI returns 404 or broken assets

**Cause**
- Jenkins not configured for reverse proxy prefix

**Required fix**
- Jenkins must run with:
```

--prefix=/jenkins

```
or equivalent environment configuration

---

### 3. API Gateway access

**Status**
- Working via nginx
- Accessible through `/api/`

**Note**
- Must ensure internal proxy uses HTTP (not HTTPS) unless explicitly configured

---

### 4. Nginx test confusion

Initial curl tests failed because:

- Requests were sent without proper Host header
- Default nginx server responded instead of configured vhost

Correct validation requires:
```

Host: sobol-pro.duckdns.org

```

---

## 10. Deployment Rules

### Mandatory constraints

- Laravel must remain at `/`
- Angular must live at `/app/`
- API Gateway must be isolated under `/api/`
- Jenkins must remain under `/jenkins/`
- No direct public exposure of internal microservices

---

## 11. What is already working

- Docker compose stack fully running
- Service discovery operational
- Kafka running
- MongoDB services healthy
- API Gateway reachable through nginx
- Jenkins container running
- Angular container serving content

---

## 12. Next Fix Priorities

1. Fix Angular base-href build
2. Fix Jenkins subpath routing
3. Validate nginx proxy behavior with HTTPS
4. Ensure API Gateway header forwarding consistency
