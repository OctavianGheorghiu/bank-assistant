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

No code yet. Build and run instructions arrive with phase 1.

## Documentation

- [Overview](docs/design/overview.md): aim, scope and use cases
- [Architecture](docs/design/architecture.md): components and who calls whom
- [Roadmap](docs/design/roadmap.md) and [phase briefs](docs/phases/)
- [Decision log](docs/state/decision-log.md)
