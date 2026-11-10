# Overview: aim, what will be built, scope and use cases

## Aim

This project builds a banking assistant out of small Spring Boot microservices, so that every major agentic concept and every chosen microservice technology gets a working proof of concept (PoC). Breadth matters more than depth: each concept only has to run end to end once.

Success means:

- Every row of the agentic and microservice maps ([agentic-concepts.md](agentic-concepts.md), [architecture.md](architecture.md)) runs at least once.
- The whole system starts with one command in Docker Compose, and later on a local Kubernetes cluster.
- The same assistant runs on two hosted providers (Claude and OpenAI) and on one local model.
- The user can explain each design choice and its trade-off in their own words.

## What will be built

A chat assistant for a fictional bank. A user asks things like "what did I spend on food last month?", "what are the fees on a SEPA transfer?" or "send 50 EUR to Maria", and a team of agents answers by calling banking services, searching bank documents and, for money movement, asking the user to confirm. All data is fake and no real bank is involved.

Proposed services, each small enough to build in a few evenings:

| Service | Responsibility | Main learning value |
| --- | --- | --- |
| api-gateway | Single entry point, JWT validation, rate limiting | Spring Cloud Gateway |
| assistant-service | Agent orchestrator, chat memory, provider switch | Spring AI, multi-agent |
| mcp-banking-server | Exposes banking operations as MCP tools | MCP server and client |
| knowledge-service | Ingests bank documents, answers from them | RAG, vector store |
| accounts-service | Accounts, balances, transactions | Plain REST + PostgreSQL |
| payments-service | Transfers with a multi-step flow | Saga, outbox, Kafka |
| notification-service | Reacts to payment events | Event-driven consumer |

A minimal web chat page is enough as a front end; the project is not about UI.

## Scope and out of scope

In scope is anything needed to show a concept working with fake data on the user's own machine.

**In scope**

- The seven services above, in one monorepo.
- Local run in Docker Compose, then in a local Kubernetes cluster (kind or minikube).
- Three model setups: Claude, OpenAI and a local Ollama model.
- A small test set for the agents, so it shows when a change makes them worse.

**Out of scope**

- Real banking integrations, real money, regulatory compliance.
- Production hardening, high availability, load testing.
- A polished front end.
- Training or fine-tuning models.
- Cloud deployment on AWS. It is a possible stretch goal once the Kubernetes phase works.
- Eureka and Spring Cloud Config. Kubernetes DNS and ConfigMaps cover the same ground, so these are skipped on purpose.

## Users and use cases

The only user is the developer, playing a bank customer. Six scenarios are chosen so that each one forces a different concept to work.

| # | Scenario | What it forces to work |
| --- | --- | --- |
| 1 | "What is my balance?" | Tool calling, then the same call through MCP |
| 2 | "What did I spend on food last month?" | Multi-step tool use over transactions |
| 3 | "What are the fees for a SEPA transfer?" | RAG over bank documents, with a cited source |
| 4 | "Send 50 EUR to Maria." | Human confirmation, then the payment saga |
| 5 | "Send 50 EUR to Maria and tell me what it costs." | Several agents working on one request |
| 6 | A message that tries to trick the assistant into moving money | Guardrails and tool policy |
