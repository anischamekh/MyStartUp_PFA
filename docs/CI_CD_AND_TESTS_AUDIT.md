# CI/CD and tests audit

**Date:** 2026-06-03  
**Build:** `mvn -B clean verify` in `microservices/` — **SUCCESS**

## Summary

| Item | Status |
|------|--------|
| Unit tests (JUnit 5 + Mockito) | Present — 17 test classes, 38+ test methods |
| Integration tests | Present — REST (MockMvc), Kafka (`@EmbeddedKafka`) |
| BDD (Cucumber) | Present — 1 feature, 1 scenario |
| TDD documentation | Present — `docs/TDD_EXAMPLE.md` |
| JaCoCo XML reports | Generated per module under `target/site/jacoco/jacoco.xml` |
| SonarCloud coverage wiring | **Configured** (`sonar.coverage.jacoco.xmlReportPaths` + inclusions) |
| Coverage ≥ 70% (full codebase) | **Not met** — see below |
| Jenkins | `Jenkinsfile` at repo root; local setup — `docs/JENKINS_SETUP.md` |
| Session expirée / 401 fix | Hybrid auth: HttpOnly cookies + in-memory `accessToken` in API response |
| Kafka `http://localhost:9092` | Documented — not HTTP; use Kafka UI on port **8090** |

## Coverage

| Metric | Before (audit start) | After (this work) |
|--------|----------------------|-------------------|
| Auth-service (all classes, lines) | ~3% | ~19% (full module ~19%) |
| Aggregate (all modules, lines) | ~3% | **19.4%** |
| Business scope (`service`, `security`, `common/security`) | ~3% | **~22%** (chatbot 44%, common-lib 94%) |
| Target | ≥ 70% | **Not reached** — more unit tests required on HRM/project/auth services |

JaCoCo reports: open `microservices/<module>/target/site/jacoco/index.html` after `mvn verify`.

SonarCloud will import coverage on the next analysis when CI runs:

```text
sonar.coverage.jacoco.xmlReportPaths=
  microservices/common-lib/target/site/jacoco/jacoco.xml,
  microservices/auth-service/target/site/jacoco/jacoco.xml,
  microservices/hrm-service/target/site/jacoco/jacoco.xml,
  microservices/project-service/target/site/jacoco/jacoco.xml,
  microservices/chatbot-service/target/site/jacoco/jacoco.xml
```

Sonar coverage inclusions: `**/service/**`, `**/security/**`, `**/common/security/**`.

## Test inventory

| Type | Count | Location / notes |
|------|-------|------------------|
| Unit test classes | 17 | `microservices/**/src/test/java/**/*Test.java` (excl. integration) |
| Integration test classes | 2 | `AuthApiIntegrationTest`, `UserEventPublisherKafkaIntegrationTest` |
| BDD scenarios | 1 | `chatbot-service/src/test/resources/features/chatbot.feature` |
| TDD examples (documented) | 1 | `docs/TDD_EXAMPLE.md` — Auth login Red/Green/Refactor |
| Ollama mocked tests | 3 | `OllamaClientTest` (MockRestServiceServer) |

## SonarCloud

- **Project:** [anischamekh_MyStartUp_PFA](https://sonarcloud.io/project/overview?id=anischamekh_MyStartUp_PFA)
- **Coverage message on dashboard:** Should clear after the next pipeline publishes JaCoCo XML paths above.
- **Local scanner (with token):** from `microservices/`:  
  `mvn -B verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.token=<token>`

## Jenkins

| Item | Value |
|------|--------|
| Dashboard URL (local Docker) | http://localhost:8085 |
| Port | 8085 (map `8085:8080`) |
| Container name (recommended) | `mystartup-jenkins` |
| Setup guide | `docs/JENKINS_SETUP.md` |
| Pipeline | Root `Jenkinsfile` — checkout → `mvn verify` → Sonar → frontend build → Docker push on `main` |

Jenkins is **not** started automatically by this repo; run Docker commands from the setup guide.

## Kafka

- **Broker (host):** `localhost:9092` — Kafka protocol only, not a web URL.
- **Web UI:** http://localhost:8090 (`kafka-ui` service in `docker-compose.yml`).
- Details: `docs/KAFKA.md`.

## Session expirée / 401

**Cause:** Gateway and services expected JWT via `Authorization` or cookie; SPA had no in-memory token after login-only cookies.

**Fix:**

1. `SessionResponse` includes `accessToken` for SPA memory (not `localStorage`).
2. Angular `AuthService` stores token in memory; `jwtInterceptor` sends `Bearer` + `withCredentials`.
3. `http-error.interceptor` retries with refreshed token and credentials.

**Deploy:** Rebuild and restart frontend + `api-gateway` + `auth-service`:

```powershell
docker compose build api-gateway auth-service
docker compose up -d api-gateway auth-service
```

## Actions performed

- JaCoCo + Sonar XML paths in `microservices/pom.xml`, `sonar-project.properties`, `Jenkinsfile`.
- New/updated tests: common-lib JWT, chatbot Ollama/sanitizer/ChatService, auth REST + Kafka IT, Cucumber BDD.
- Docs: `TDD_EXAMPLE.md`, `JENKINS_SETUP.md`, `KAFKA.md`, this audit.
- `kafka-ui` on port 8090 in `docker-compose.yml`.
- Session/auth hybrid token fix (backend + Angular).

## Missing / follow-up

- Raise line coverage to **≥ 70%** on `service`/`security` packages (HRM, project, auth business services).
- Optional: RestAssured-based IT on API gateway with Testcontainers.
- Run SonarCloud analysis in CI to confirm coverage widget updates.
- Install Jenkins locally if not already done.
