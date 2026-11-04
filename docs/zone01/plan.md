# Plan

| Phase | Dates | Status |
|---|---|---|
| 0 Foundation | Nov 5 to 15 | in progress |
| 1 mr-jenk | Nov 16 to 29 | todo |
| 2 safe-zone | Nov 30 to Dec 6 | todo |
| 3 buy-02 | Dec 7 to Jan 3 | todo |
| 4 nexus | Jan 4 to 10 | todo |

## Phase 0 tasks
- [ ] Rotate leaked secrets, untrack backend/media-service/.env
- [ ] Parent POM, align versions, rename eurika-server
- [ ] common-security module
- [ ] Unit tests for application services (user, product, media)
- [ ] Split infra/ci compose from infra/app compose

## Decisions log
- Java 21 kept despite nexus "Java 11" constraint (pending staff confirmation)