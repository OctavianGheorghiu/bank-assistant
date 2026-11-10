# Architecture and technology

The assistant-service is the centre: it keeps conversation memory in Redis, calls the model providers, and reaches banking data through MCP tools and bank documents through the knowledge-service.

## Architecture (text version of the diagram)

**Components**

- Web chat page (minimal front end) and, for testing, an external MCP client (for example a desktop client).
- Services: api-gateway, assistant-service, mcp-banking-server, knowledge-service, accounts-service, payments-service, notification-service.
- Infrastructure: PostgreSQL with pgvector, Redis, Kafka.
- Model providers: Claude and OpenAI (hosted), Ollama (local); an embedding model, hosted or local.
- Observability (phase 7): a trace backend, Prometheus, Grafana.

**Who calls whom**

| From | To | What | Phase |
| --- | --- | --- | --- |
| Web chat page | api-gateway | Chat requests with a JWT | 2 |
| api-gateway | accounts-service | Route for accounts, balance, transactions; passes on the user identity | 1 |
| api-gateway | assistant-service | Chat endpoint behind the gateway | 2 |
| assistant-service | Claude, OpenAI or Ollama | Model calls, provider chosen by Spring profile; Ollama is also the fallback | 2, 7 |
| assistant-service | Redis | Chat memory keyed by conversation id | 2 |
| assistant-service | accounts-service | Tool methods (balance, transactions, spending), until phase 3 moves them | 2 |
| assistant-service | mcp-banking-server | MCP client calls (Streamable HTTP, proposed) | 3 |
| External MCP client | mcp-banking-server | Lists and calls the tools (STDIO for local testing) | 3 |
| mcp-banking-server | accounts-service | Banking operations behind the tools | 3 |
| assistant-service | knowledge-service | Retrieval: best chunks with their sources | 4 |
| knowledge-service | Embedding model | Embeddings for ingestion and queries | 4 |
| knowledge-service | PostgreSQL (pgvector) | Stores and searches chunks | 4 |
| accounts-service | PostgreSQL | Accounts, balances, transactions | 1 |
| mcp-banking-server (payment tool, used only by the payments agent) | payments-service | Creates a pending transfer and gets its id (proposed, #29) | 5 |
| Web chat page (Confirm button) | api-gateway, then payments-service confirm endpoint | Authenticated confirmation, separate from the chat (route proposed, #30) | 5 |
| payments-service | accounts-service | Reserves and releases funds | 6 |
| payments-service | PostgreSQL | Transfer state and outbox rows in one transaction | 6 |
| payments-service outbox publisher | Kafka | Payment events (JSON, proposed) | 6 |
| notification-service | Kafka | Consumes payment events | 6 |
| Every service | Trace backend, Prometheus | Traces and metrics; a Grafana dashboard shows them | 7 |

Calls marked "proposed" are confirmed or changed in the phase named: see [Decision log](../state/decision-log.md) #28 to #31.

## Microservice technologies map

The four areas the user picked each get a PoC, and the agents are what make each one interesting: LLM calls are slow and flaky, which is exactly what resilience and tracing exist for.

| Technology | PoC | Why it fits an agentic system |
| --- | --- | --- |
| API gateway | Spring Cloud Gateway with JWT validation and per-user rate limits | Caps how often one user can trigger expensive model calls |
| Resilience | Resilience4j timeout, retry and circuit breaker around model calls; fallback to the Ollama model | A provider outage or slow answer must not take the system down |
| Observability | Micrometer and OpenTelemetry traces from gateway to model call, Prometheus metrics, a Grafana dashboard with token usage | Shows which agent called which tool, and what each answer cost |
| Saga and outbox | Transfer flow: reserve funds, execute, notify, with compensation on failure; events published through a transactional outbox to Kafka | An agent can start a multi-step action that must roll back cleanly |
| Event-driven consumers | notification-service reacting to payment events | Decouples side effects from the agent's request |
| Docker Compose | Whole stack, including PostgreSQL, Redis, Kafka and Ollama, in one file | Single command for local development |
| Kubernetes | Manifests or Helm charts on kind or minikube, with health probes and ConfigMaps | Same stack, run the way production would run it |
