# Plan

| Phase | Dates | Status |
|---|---|---|
| 0 Foundation | Nov 5 to 15 | in progress |
| 1 mr-jenk | Nov 16 to 29 | todo |
| 2 safe-zone | Nov 30 to Dec 6 | todo |
| 3 buy-02 | Dec 7 to Jan 3 | todo |
| 4 nexus | Jan 4 to 10 | todo |
| Buffer and audit dry-runs | Jan 10 to leave | todo |

## Phase 0 tasks
- [x] Back up live media files
- [x] Truncate Docker logs, log rotation, 4 GB swap on app VM
- [x] Compose: memory limits, mongod --quiet, Kafka pinned, Jenkins removed
- [x] Fix media persistence (mount on /app/uploads)
- [ ] Ask Zone01 staff: multi-project audits in January, Java 17/21 for nexus, remote audits
- [ ] Create CI VM (A1 3 OCPU / 18 GB / 120 GB)
- [x] Rotate JWT keys, Mongo passwords and keystore password, untrack backend/media-service/.env (rotate, no history rewrite)
- [x] Parent POM, align Boot 3.5.x / Cloud 2025.0.x / Java 21, rename eurika-server (PR #57)
- [ ] Replace spring-cloud-starter-gateway with spring-cloud-starter-gateway-server-webflux
- [ ] Gateway: remove DEBUG logging and header logging in LoggingFilter
- [x] common-security module (PR #57, 17 unit tests)
- [ ] Add HEALTHCHECK and a non-root user to backend/Dockerfile and the gateway Dockerfile
- [ ] Delete merged branches phase0-parent-pom and claude/project-thread-n6ds0r (local and origin)
- [ ] Unit tests for user, product, media application services
- [ ] Split docs/zone01/audits.md into audits/<project>.md
- [ ] Add .githooks/commit-msg and set core.hooksPath
- [ ] Remove temporary log-truncate cron
- [ ] Resize app VM boot volume to 60 GB

## Phase 1 mr-jenk
- [ ] infra/ci compose: Jenkins (with Docker CLI), Caddy
- [ ] Jenkinsfile with all stages, Sonar and Nexus stages as stubs
- [ ] GitHub webhook trigger
- [ ] Karma specs and headless Chrome for frontend tests
- [ ] Image tagging, deploy via SSH agent on app VM
- [ ] Health check and rollback to previous tag
- [ ] Email or Slack notifications in post block
- [ ] Bonus: parameterized build (environment), distributed build

## Phase 2 safe-zone
- [ ] SonarQube + PostgreSQL in infra/ci, JaCoCo and lcov coverage
- [ ] Quality gate fails the pipeline
- [ ] Branch protection, PR scans, reviewer agent on every PR
- [ ] Fix Critical items from analysis.md section 10, document each fix
- [ ] Consolidate docs/

## Phase 3 buy-02
- [ ] order-service: cart and order models and APIs
- [ ] Order management: cancel, redo, status rules
- [ ] Search and filters in product-service
- [ ] Profiles and analytics (aggregations)
- [ ] Angular: cart, checkout wizard, orders, dashboards, search page
- [ ] e2e tests on critical flows

## Phase 4 nexus
- [ ] Nexus in infra/ci, run as non-root
- [ ] Maven hosted, proxy, group repos; settings.xml in Jenkins
- [ ] Docker hosted repo behind TLS, push and pull
- [ ] Versioning strategy, multiple versions shown
- [ ] Publish stage in pipeline
- [ ] Bonus: RBAC and repository permissions
- [ ] Documentation with screenshots

## Decisions log
See decisions.md.
