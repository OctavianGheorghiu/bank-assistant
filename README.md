# bank-assistant

A chat assistant for a fictional bank, built from small Spring Boot microservices. It is a learning project for agentic AI (tool calling, MCP, RAG, multi-agent flows, guardrails) and microservice patterns (gateway, saga, outbox, resilience, tracing, Kubernetes), at proof-of-concept depth.

All data is fake. No real bank, accounts or money are involved.

## What it does

You ask things like:

- "What is my balance?"
- "What did I spend on food last month?"
- "What are the fees for a SEPA transfer?"
- "Send 50 EUR to Maria."

Agents answer by calling banking services, searching bank documents and, before any money moves, asking you to confirm.

## Services

| Service | Responsibility |
| --- | --- |
| api-gateway | Single entry point, JWT validation, rate limiting |
| assistant-service | Agent orchestration, chat memory, model provider switch |
| mcp-banking-server | Banking operations exposed as MCP tools |
| knowledge-service | Bank documents and retrieval (RAG) |
| accounts-service | Accounts, balances, transactions |
| payments-service | Transfers as a saga with an outbox |
| notification-service | Reacts to payment events |

## Tech stack

Java, Spring Boot, Spring AI, Spring Cloud Gateway, PostgreSQL with pgvector, Redis, Kafka, Resilience4j, OpenTelemetry, Prometheus, Grafana, Docker Compose, Kubernetes. Models: Claude, OpenAI and a local Ollama model.

## Roadmap

1. Foundation
2. First agent
3. MCP
4. RAG
5. Multi-agent and approval
6. Payment saga
7. Resilience and tracing
8. Guardrails and evals
9. Kubernetes

Progress is tracked in [docs/state/status.md](docs/state/status.md).

## Getting started

You need Docker (Docker Desktop on Windows, about 8 GB of memory for it) and bash with curl (Git Bash on Windows). Java is optional: the Gradle wrapper downloads JDK 21 itself, and the Docker build needs no local Java at all.

1. Copy `.env.example` to `.env` and replace every `change-me` with a password of your own. `.env` is git-ignored.
2. Start everything and run the checks:

   ```bash
   bash scripts/smoke-test.sh
   ```

   The script runs `docker compose up -d --build --wait` (the first run takes a few minutes), then checks each phase's gate. Or start the stack yourself with `docker compose up -d --build --wait`.

3. Ask for a balance through the gateway as one of the demo customers (`alice`, `bob` or `carol`, all with `DEMO_USER_PASSWORD` from `.env`):

   ```bash
   set -a; . ./.env; set +a
   TOKEN=$(curl -s -d grant_type=password -d client_id=bank-cli -d username=alice --data-urlencode "password=$DEMO_USER_PASSWORD" http://localhost:8180/realms/bank/protocol/openid-connect/token | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p')
   curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/accounts
   ```

| URL | What |
| --- | --- |
| http://localhost:8080/api/accounts | api-gateway (needs a token) |
| http://localhost:8180 | Keycloak (admin console: user `admin`, password `KEYCLOAK_ADMIN_PASSWORD`) |

Build and test with `./gradlew build` (Docker must be running: the tests start PostgreSQL through Testcontainers). Stop with `docker compose down`; `docker compose down -v` also deletes the database, which is recreated with the same seed data on the next start.

## Documentation

- [Overview](docs/design/overview.md): aim, scope and use cases
- [Architecture](docs/design/architecture.md): components and who calls whom
- [Roadmap](docs/design/roadmap.md) and [phase briefs](docs/phases/)
- [Decision log](docs/state/decision-log.md)
- [Code conventions](docs/design/code-conventions.md): package layout, Lombok, JPA and logging rules for every service
