# Repository, versions and interfaces

The build, test, start and smoke-test commands and the repository location and branch convention live in the Commands section of [CLAUDE.md](../../CLAUDE.md). Keep entries exact, and write environment variable names, never values.

## Pinned versions

| Item | Version | Pinned in |
| --- | --- | --- |
| Java | 21 (Gradle toolchain; downloaded by the foojay resolver if missing) | `build.gradle.kts` |
| Gradle | 9.9.0 (wrapper) | `gradle/wrapper/gradle-wrapper.properties` |
| foojay toolchain resolver plugin | 1.0.0 | `settings.gradle.kts` |
| Spring Boot | 4.1.2 (manages Flyway 12.4.0, PostgreSQL JDBC 42.7.14, Testcontainers 2.0.5) | `build.gradle.kts` |
| Spring Cloud | 2025.1.4 (Gateway 5.0.4, WebFlux variant) | `build.gradle.kts` |
| Spring AI | 2.0.2, from phase 2 (supports Boot 4.0 and 4.1) | not yet used |
| PostgreSQL image | `pgvector/pgvector:0.8.7-pg18-trixie` (PostgreSQL 18, pgvector 0.8.7) | `compose.yaml`, `AccountsApiTests` |
| Keycloak image | `quay.io/keycloak/keycloak:26.8.1` | `compose.yaml` |
| Java images | `eclipse-temurin:21.0.13_9-jdk-noble` (build), `eclipse-temurin:21.0.13_9-jre-noble` (run) | `Dockerfile` |
| GitHub Actions | `actions/checkout@v7`, `actions/setup-java@v6`, `gradle/actions/setup-gradle@v6` | `.github/workflows/build.yml` |

## Ports and URLs

All host ports are bound to 127.0.0.1 only.

| Service | Host URL | Inside Compose | Notes |
| --- | --- | --- | --- |
| api-gateway | http://localhost:8080 | http://api-gateway:8080 | The only authenticated entry point |
| accounts-service | http://localhost:8081 | http://accounts-service:8081 | Host port kept for debugging; bypasses the gateway (decision #35) |
| Keycloak | http://localhost:8180 | http://keycloak:8080 (health on 9000) | Realm `bank` |
| PostgreSQL | localhost:5432 | postgres:5432 | Database `bank` |

Every Spring service exposes `/actuator/health`, used by the Compose health checks.

## Environment variables (names only)

| Name | Used by | Where it is set |
| --- | --- | --- |
| `POSTGRES_PASSWORD` | PostgreSQL superuser | `.env` |
| `ACCOUNTS_DB_PASSWORD` | `accounts` role; accounts-service | `.env` |
| `KEYCLOAK_DB_PASSWORD` | `keycloak` role; Keycloak | `.env` |
| `KEYCLOAK_ADMIN_PASSWORD` | Keycloak admin console user `admin` | `.env` |
| `DEMO_USER_PASSWORD` | Password of the demo users alice, bob and carol (realm file placeholder) | `.env` |
| `SPRING_DATASOURCE_URL` | accounts-service database URL | `compose.yaml` (default: localhost) |
| `ACCOUNTS_SERVICE_URL` | api-gateway route target | `compose.yaml` (default: http://localhost:8081) |
| `KEYCLOAK_ISSUER_URI` | api-gateway: required `iss` claim | default http://localhost:8180/realms/bank |
| `KEYCLOAK_JWK_SET_URI` | api-gateway: where signing keys are fetched | `compose.yaml` (default: localhost:8180) |

`.env` is git-ignored; `.env.example` lists the names. The smoke script reads `.env` too, and honours `GATEWAY`, `KEYCLOAK` and `COMPOSE_PROJECT_NAME`.

## Code layout

| Path | What |
| --- | --- |
| `settings.gradle.kts`, `build.gradle.kts` | Module list; shared Java toolchain, BOMs and test setup for every module |
| `<service>/build.gradle.kts` | That service's dependencies only |
| `<service>/src/main/java/com/bankassistant/<name>/` | Package per service: `accounts`, `gateway` |
| `accounts-service/src/main/resources/db/migration/` | Flyway migrations (`V1__schema.sql`, `V2__seed.sql`) |
| `Dockerfile` | One multi-stage Dockerfile for every service: `--build-arg MODULE=<service>` |
| `compose.yaml` | The whole stack; later infrastructure joins through Compose profiles (decision #1) |
| `infra/postgres/init/01-init.sh` | Creates one schema and one login role per service, on the first start only |
| `infra/keycloak/bank-realm.json` | Realm imported on Keycloak's first start |
| `scripts/smoke-test.sh` | Gate checks of every phase so far |

Adding a service: add it to `settings.gradle.kts`, give it a `build.gradle.kts`, add a Compose entry with `build.args.MODULE`, and, if it needs a database, a schema and role in the init script (an existing volume then needs `docker compose down -v`, or the SQL run by hand).

## Interfaces other phases rely on

| Interface | Kind | Defined in phase | Used by phases |
| --- | --- | --- | --- |
| `GET /api/accounts` (gateway) → `GET /accounts` (accounts-service): `[{id, name, type, currency, balance}]` | API path | 1 | 2, 3 |
| `GET /api/accounts/{accountId}/balance` → `{accountId, amount, currency}` | API path | 1 | 2, 3 |
| `GET /api/accounts/{accountId}/transactions?from=&to=&category=` → `[{id, bookedAt, amount, currency, category, description, counterparty}]`, newest first; dates inclusive ISO `yyyy-MM-dd`, all filters optional | API path | 1 | 2, 3 |
| Money in JSON: `amount` is a decimal string with two places (`"-44.90"`), negative means money out; stored as BIGINT cents | Format | 1 | all |
| `X-User-Id` header: the customer id; set only by the gateway from the token, any incoming value is dropped. accounts-service answers 404 for an account of another customer and 400 without the header | Header | 1 | 2, 3, 5, 6 |
| Token: Keycloak realm `bank`, issuer `http://localhost:8180/realms/bank`, customer id in claim `preferred_username`; client `bank-cli` (public, password grant, for scripts only) | Token format | 1 | 2, 3, 8 |
| Seed data: customers `alice`, `bob`, `carol`; accounts `<customer>-current` and `<customer>-savings` in EUR; transaction categories `opening`, `salary`, `rent`, `utilities`, `transfer`, `groceries`, `restaurants`, `transport`, `shopping`, `entertainment`; dates relative to the day the database was created | Seed data | 1 | all demos |
| Database schemas and roles: `accounts`, `keycloak`, one login role per schema, each limited to its own schema | Configuration | 1 | 4, 6 |
