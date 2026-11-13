# Decisions and context

Handoff from the planning chat of 2026-11-04. Read with plan.md and analysis.md.

## Goal
- Finish the Zone01 Full-Stack Java specialty (10 projects, 3 validated) to apply for RNCP 7.
- Next 4 projects on one codebase: mr-jenk, safe-zone, buy-02, nexus.
- All 4 finished and audit-ready by 2027-01-10. January leave in Oujda is for audits only, not building.
- DevOps specialty postponed to the mid term. The Java branch already contains the DevOps work (Jenkins, SonarQube, Nexus).

## Capacity
- About 19 h/week: Mon, Wed, Thu 19:00 to 21:30, Saturday 7 h, Sunday 4 h.
- CFG Bank work comes first. If a week slips, cut bonuses first, never Phase 0.

## Key decisions
| Decision | Reason |
|---|---|
| Keep Java 21 and Spring Boot 3.x for nexus | Boot 3 requires Java 17+. Downgrading to Boot 2.7 (EOL) is a multi-day rewrite. Pending written staff confirmation |
| Align all services to Boot 3.5.x, Cloud 2025.0.x, Java 21 | Today there are 3 different combinations. Gateway route prefix already needs 2025.0 |
| Maven multi-module with a parent POM at backend/ | One mvn verify, one Sonar analysis, one version for Nexus |
| common-security module | 4 copies of JwtUtil today. Avoid a 5th in order-service |
| New order-service for cart, orders, analytics | user is identity only, product is the catalog. Analytics are Mongo aggregations on orders |
| Search and filters in product-service | It owns the products collection |
| Two VMs | App VM is 1 OCPU / 5.8 GB. SonarQube and Nexus do not fit. Builds must never starve the live site |
| CI stack in infra/ci/, never in the app compose | A deploy must not restart the Jenkins running it. Separate blast radius |
| Jenkins controller on CI VM, SSH agent on app VM | Agent runs deploy, health check, rollback. Covers the distributed builds bonus |
| Deploy only images pulled from Nexus by tag | What runs is exactly what was tested. Rollback = redeploy previous tag |
| SonarQube remote, docker-compose fallback for local | Subject says "locally" but the constraint is "via Docker" |
| Simple, explainable code | Every line is explained to peers during audits |
| Reviewer subagent writes docs/reviews/<branch>.md | Solo project still shows a PR review process for buy-02 |

## Infrastructure state
- App VM: Oracle Cloud Ampere A1 (aarch64), 1 OCPU, 5.8 GB RAM, 4 GB swap added, 45 GB disk (27 GB free after cleanup).
- Live at https://buy-01.duckdns.org behind host nginx. Repo at ~/buy-01.
- Free A1 quota left: 3 OCPU / 18 GB. Enough for the CI VM.
- Compose now has: log rotation (5m x 2), memory limits, JVM flags via JAVA_TOOL_OPTIONS, mongod --quiet with 0.25 GB cache, healthcheck every 60 s, Kafka pinned, Jenkins removed.
- Media files now persist: mount changed to ./uploads/media:/app/uploads. Backup in ~/backups/.

## Incidents (useful for the jury)
1. Disk 76% full: 18.3 GB of logs from the 3 MongoDB containers. Root cause: healthcheck opened a connection every 10 s and Mongo logged each one. Fixed in code: --quiet, 60 s interval, log rotation.
2. Kafka crash loop after pinning 3.9.1: volume written by a newer :latest (metadata feature level 29). Pinning 4.1.0 did not fix it: on 2026-11-11, after the multi-module rebuild, 4.1.0 crashed the same way because it only supports levels 7 to 27. Fix: delete the kafka-data volume (Kafka only holds transient events here). Reason :latest is banned, and the version must never go below the one that wrote the volume.
3. Media images lived only inside the container (wrong mount path). Backed up, then fixed with the mount.
4. 502 from host nginx right after the 2026-11-11 rebuild: the gateway container was "Started" but Spring Boot needs 60 to 120 s on 1 OCPU before it listens. Lesson: a deploy health check must poll /actuator/health, not trust container state.
5. Secrets committed early in buy-01: a root .env with the JWT private key and Mongo passwords, the gateway keystore, frontend TLS keys, backend/media-service/.env, and Mongo passwords as fallback values in application.yaml. Rotated on 2026-11-12 (JWT key pair, 3 Mongo passwords, gateway keystore password), tracked files untracked, fallbacks removed. History kept on purpose: a rewrite changes every commit id and breaks the PR evidence the audits rely on, and GitHub keeps old commits reachable through PR refs anyway, so a rewrite cannot guarantee removal. Rotation is what makes the leaked values useless.

## Open questions for Zone01 staff
- Can several Java projects be audited during the same January visit (unlock order)?
- Is Java 17/21 accepted for nexus?
- Can audits happen remotely?

## Known risks from analysis.md (fix in the phase noted)
- Secrets in git history (JWT private key, Mongo passwords, keystores): Phase 0
- Zero real tests, contextLoads needs live infra: Phase 0 and 1
- Gateway user-exists check is dead, logs bearer tokens, DEBUG level: Phase 0
- Self-assigned SELLER role, media linking without ownership check: Phase 2
- Product deletion never deletes media, orphan cleanup deletes after 1 minute: Phase 2
- Product to media JSON bug (frontend parses imagesIds[0]): Phase 2

## CI VM target
| Setting | Value |
|---|---|
| Shape | VM.Standard.A1.Flex, 3 OCPU, 18 GB |
| Image | Ubuntu 24.04 aarch64 |
| Boot volume | 120 GB |
| Network | Same VCN and subnet as app VM, private IPs between them |
| Exposure | New DuckDNS subdomain, Caddy or nginx with Let's Encrypt for Jenkins, SonarQube, Nexus and the Docker registry. SSH only from my IP |
| RAM budget | OS 1, Jenkins 2, SonarQube 3 to 4, Nexus 3, builds 4 to 6 GB |

## Pipeline design (one Jenkinsfile, grows per project)
checkout, build, test, sonar analysis, quality gate, package (versioned images), publish to Nexus, deploy (compose pull and up), health check (rollback on failure), notify in post block.
Image tag: build number plus git short SHA.
