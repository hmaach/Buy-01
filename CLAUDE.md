# Buy platform (Zone01 Java track)

Solo project toward RNCP 7. Five Zone01 projects on one codebase:
buy-01 (validated), mr-jenk, safe-zone, buy-02, nexus.
Current phase and status: @docs/zone01/plan.md

## References (read when relevant, not by default)
- Subjects: docs/zone01/subjects.md
- Audit criteria (acceptance tests): docs/zone01/audits.md (one section per project, to be split into audits/<project>.md)
- Codebase snapshot: docs/zone01/analysis.md
- Decisions and context: docs/zone01/decisions.md

## Source of truth
- docs/zone01/analysis.md and the code win over any other file in docs/.
- Files in docs/ outside zone01 may be outdated. Do not follow them without checking the code.
- Deployment topology is in the Deployment section below (docs/AI-CONTEXT.md was deleted).

## Stack
- backend/: Spring Boot microservices, hexagonal layout (domain, application, infrastructure)
- Services: api-gateway, discovery-server (Eureka), user, product, media, order (buy-02)
- MongoDB per service, Kafka for events, Angular 20 standalone frontend in frontend/
- Spring Boot 3.5.16, Spring Cloud 2025.0.3, Java 21. Versions live only in backend/pom.xml (parent and aggregator). Modules never declare their own versions.
- backend/common-security: JwtIssuer (user-service only), JwtVerifier, servlet and reactive filters, auto-configured from security.jwt.*. Services fail at startup without a valid JWT public key.
- Docker: backend/Dockerfile builds every service except the gateway (build arg SERVICE=<module>, context backend/). The gateway has its own Dockerfile for the TLS keystore.
- A new service is one <module> line in backend/pom.xml plus one compose entry using backend/Dockerfile.
- App VM: Oracle Cloud ARM64, 1 OCPU, 6 GB RAM. Keep memory and disk usage low. Every compose service has a mem_limit.
- CI VM (separate): Jenkins, SonarQube, Nexus in infra/ci/. Never add CI tools to the app compose.
- All images must build for arm64.

## Deployment
- Live at https://buy-01.duckdns.org behind host nginx
- Repo on the VM at ~/buy-01, app stack via docker compose
- Host nginx routes /api/ to the gateway (127.0.0.1:8080) and /app/ to the frontend (127.0.0.1:4200). Both containers serve self-signed HTTPS.
- Treat it as production: every deploy needs a health check and a rollback path
- On the app VM, build images one service at a time (docker compose build <service>). Parallel Maven builds run out of memory.
- Spring services need 60 to 120 s to start and register in Eureka. A 502 from host nginx right after a deploy usually means "still starting".
- Kafka cannot be downgraded on an existing volume. Pin a version at least as new as the one that wrote kafka-data, or delete that volume (it only holds transient events).
- Never run docker compose down -v or delete Mongo volumes without asking me first.

## Commands
- Backend build: cd backend && ./mvnw -B clean verify
- Single module: ./mvnw -B -pl <module> -am verify
- Until the service contextLoads tests are replaced, verify fails without live infra. The build that passes today is ./mvnw -B clean install -DskipTests. Do not add -DskipTests to CI.
- Frontend: cd frontend && npm ci && npx ng test --watch=false --browsers=ChromeHeadless && npx ng build

## Code style
I explain every line to my peers during audits. Write code a junior can read on first pass.
- Prefer the simplest working solution. No clever abstractions, no premature generics.
- Small classes and methods with clear names. Readable over short.
- Use standard Spring and Angular patterns, not exotic libraries.
- Configs (Jenkinsfile, compose, pom, Dockerfile) stay flat and explicit. No heavy templating.
- If a simple version and an "enterprise" version both meet the audit, pick the simple one and mention the other in one line.
- When you add something non-obvious, tell me in chat why, so I can explain it.

## Comments and documentation
- No boilerplate: no Javadoc on getters, setters, or obvious methods, no license headers, no "this method does X" restating the code.
- Comment only to explain the architecture or the flow: why this service calls that one, why an event is published here, what happens next in the chain.
- Docs in docs/ describe how parts connect: request flows, event flows, pipeline stages, deployment. Diagrams welcome (Mermaid).
- No generic sections (installation boilerplate, contribution guidelines, badges) unless I ask.

## Git
- Work on a feature branch, never on main. Feature branches start from jenkins and merge back into it by PR (jenkins is the integration branch for now).
- Commit only when I ask. Use my git identity as configured, never a Claude identity.
- When I say "commit", commit all pending changes split into logical commits: one commit per concern (for example config, feature code, tests, docs), each one building on its own. Never bundle unrelated changes in one commit.
- Commit messages: conventional prefix (feat, fix, chore, test, docs, refactor, ci), then a short natural sentence written like a developer would, lowercase, no period. Example: "fix: media files now saved in the mounted folder".
- Keep messages specific to what changed. No marketing words, no lists of every file touched.
- Never add Co-Authored-By, Claude-Session, or "Generated with" lines to commits or PRs.
- Never push. I push myself after reviewing.

## Rules
- Every change ships with tests. Unit tests mock ports; no live infra in unit tests.
- Never read, print, or commit .env files, keys, or keystores. Secrets come from env vars.
- Containers run as non-root. Pin image versions, no :latest.
- Reuse common-security for JWT logic; do not copy JwtUtil into new services.
- Before coding a feature, state which audit criterion it satisfies.
- If a task touches a later phase, stop and say so.
- No em dash character. No emojis.