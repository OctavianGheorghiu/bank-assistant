# Phase 1: Foundation

**Goal.** A running skeleton with no AI: api-gateway and accounts-service on PostgreSQL in Docker Compose, with fake data and a balance returned through the gateway.

**Builds on.** Nothing.

**Candidate tasks**

1. Monorepo with a multi-module build and a shared parent configuration.
2. Docker Compose file with PostgreSQL; other infrastructure is added later through Compose profiles.
3. accounts-service: schema, seed data (a few accounts and about a year of fake categorised transactions) and REST endpoints for accounts, balance and transactions.
4. api-gateway: Spring Cloud Gateway with a route to accounts-service.
5. Authentication at the gateway: validate a JWT and pass the user identity on to the services.
6. A smoke-test script and a README with the one-command start, and fill in the Commands section of CLAUDE.md and docs/state/repository.md.
7. Optional: a GitHub Actions workflow that builds the repository.

**To discuss**

- Maven or Gradle (Decision log #2).
- Java and Spring Boot versions, chosen so that Spring AI, Spring Cloud Gateway and Resilience4j all work with them in later phases.
- Authentication: self-issued JWT or Keycloak (Decision log #4). The gateway validates tokens from this phase on, and securing MCP in phase 3 depends on the answer.
- Database layout: one PostgreSQL instance with a schema per service, or separate databases. This affects phases 4 and 6.
- Schema migrations: Flyway, Liquibase or plain SQL scripts.
- How money is represented (for example minor units plus a currency code) and the fake data model.
- Whether to set up GitHub Actions now (Decision log #26).
- Machine limits (RAM, GPU): which infrastructure runs by default and which sits behind Compose profiles.

**Gate.** `docker compose up` starts the system, and a request through the gateway with a valid token returns the balance of a seeded account.

**Hands off to later phases.** The accounts-service API (balance, transactions) that the agent tools call in phase 2; the token format and identity header; the seed data used in every later demo.
