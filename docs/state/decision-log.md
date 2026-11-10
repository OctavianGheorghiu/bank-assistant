# Decision log

Every open or proposed decision, with the phase chat that settles it. When a decision is made, set its status to Decided and write the outcome in the last column. Decisions the user has already settled are in [Key decisions](../design/key-decisions.md). Smaller choices from a phase's "To discuss" list get a row only when the phase chat settles them.

| # | Decision | Status | Settle in | Outcome |
| --- | --- | --- | --- | --- |
| 1 | Machine limits (RAM, GPU): what runs by default and what sits behind Compose profiles | Decided | 1 | Facts (user, 2026-11-09): Windows laptop, 16 GB RAM, NVIDIA GPU with 4 GB VRAM, about 2.3 GB usable, so local models must fit that (a small embedding model, a chat model of about 1.5 to 3B). Decided (user, 2026-11-10): give Docker about 8 GB; PostgreSQL, Keycloak and the services built so far start by default, Redis joins the default in phase 2; Kafka (phase 6, one KRaft broker) and the observability stack (phase 7) sit behind their own Compose profiles; Ollama runs natively on Windows for the GPU; each service's JVM heap capped at about 256 MB, Keycloak at about 512 MB; in phase 9, stop Compose before starting the cluster. |
| 2 | Maven or Gradle | Decided | 1 | Gradle with the Kotlin DSL and the Gradle wrapper (user, 2026-11-10). |
| 3 | Java and Spring Boot versions compatible with Spring AI, Spring Cloud Gateway and Resilience4j | Decided | 1 | Java 21 through a Gradle toolchain; Spring Boot 4.1.2, Spring Cloud 2025.1.4 (supports Boot 4.1), Spring AI 2.0.2 later (supports Boot 4.0 and 4.1) (user, 2026-11-10). A Resilience4j starter for Boot 4 was not confirmed; phase 7 checks it, with Spring Cloud CircuitBreaker as the fallback. |
| 4 | Authentication: self-issued JWT or Keycloak | Decided | 1 | Keycloak in Compose from phase 1, with a realm exported to a file and test users that match the seed customers; the gateway validates its tokens as an OAuth2 resource server (user, 2026-11-10). |
| 5 | Database layout: one PostgreSQL with a schema per service, or separate databases | Decided | 1 | One PostgreSQL container and one database; one schema and one login role per service (and one for Keycloak), each role limited to its own schema (user, 2026-11-10). |
| 6 | Schema migration tool | Decided | 1 | Flyway, run by each service at startup against its own schema (user, 2026-11-10). |
| 7 | API access and spending limits for Claude and OpenAI; development models | Open | 2 |  |
| 8 | Stateless services with history in Redis keyed by conversation id | Proposed | 2 |  |
| 9 | How much history is kept and when a conversation expires | Open | 2 |  |
| 10 | MCP in stateless mode over Streamable HTTP | Proposed | 3 |  |
| 11 | How the user's identity reaches tools and the MCP server | Open | 2 and 3 |  |
| 12 | RAG documents: invented or adapted from public fee schedules | Open | 4 |  |
| 13 | Embedding model, hosted or local | Proposed | 4 | Proposed 2026-11-09: local nomic-embed-text through Ollama (about 0.3 GB, 768 dimensions), so embeddings cost nothing and work offline; answers still come from the hosted model. Confirm in phase 4. |
| 14 | Retrieval added automatically or exposed as a tool | Open | 4 |  |
| 15 | Agent structure: supervisor with specialists, or planner, executor and evaluator | Open | 5 |  |
| 16 | Approval policy for payments: every payment or above an amount; expiry of a pending transfer | Decided (policy); expiry Open | 5 | Every payment needs the user's confirmation, whatever the amount (user, 2026-11-09). Expiry of a pending transfer is still open. |
| 17 | Saga style: choreography over Kafka | Proposed | 6 |  |
| 18 | Event format: JSON first | Proposed | 6 |  |
| 19 | Outbox publisher: polling or change data capture | Open | 6 |  |
| 20 | Trace backend | Open | 7 |  |
| 21 | Fallback behaviour: whether the assistant says so, and whether payments are allowed | Open | 7 |  |
| 22 | Policy engine: OPA or plain code, and where it runs | Open | 8 |  |
| 23 | Evaluation set, judge model and pass thresholds | Open | 8 |  |
| 24 | Plain manifests, Helm or Kustomize | Open | 9 |  |
| 25 | Infrastructure inside the cluster or outside it | Open | 9 |  |
| 26 | GitHub Actions for building the repository | Decided | 1 | One workflow that runs the Gradle build with tests on Java 21 for pushes and pull requests; no Docker or deploy steps (user, 2026-11-10). |
| 27 | AWS deployment as a stretch goal | Open | 9 |  |
| 28 | Service-to-service calls go directly over the internal network; api-gateway is only the entry point from outside | Proposed | 2 | Why: the gateway's job is the edge (JWT, rate limits), and routing internal calls through it adds a hop for no learning. Calls pass the user identity on. |
| 29 | The payment tool lives in mcp-banking-server, which calls payments-service to create a pending transfer | Proposed | 5 | Why: the assistant reaches banking data through MCP tools, and phase 3 already marks each tool read-only or state-changing for approval. The tool is marked money-moving. |
| 30 | The Confirm call goes through api-gateway, like every call from the browser | Proposed | 5 | Why: it comes from the browser, so it gets the same JWT check as every other outside call. |
| 31 | assistant-service serves the web chat page as a static page | Proposed | 2 | Why: no extra service, and the page and the chat endpoint share one origin behind the gateway. |
| 32 | Vector store: pgvector inside the existing PostgreSQL | Proposed | 4 |  |
| 33 | Repository layout: one monorepo, multi-module | Decided | 1 | Accepted with the phase 1 task list (user, 2026-11-10). |
| 37 | Account balance: stored or computed from transactions | Decided | 1 | Stored in `account.balance_minor` (migration V3, backfilled from the transactions) (user, 2026-11-10). The transactions stay as the ledger: any code that inserts a transaction must update the balance in the same database transaction, so the two cannot drift. Phase 6 adds the locking or conditional update that stops two payments from overdrawing an account. |
| 36 | Code structure and style for every service | Decided | 1 | Layered packages (controller, service, repository, model/entity, model/dto, events, exception, config, filter), one type per file with no nested classes, Lombok, Spring Data JPA with Hibernate (`ddl-auto=validate`, Flyway owns the schema) and no SQL strings in Java, an entry log line in every controller, service, filter and listener method (user, 2026-11-10). Details in [code-conventions.md](../design/code-conventions.md). |
| 35 | Whether accounts-service publishes its port to the host | Decided | 1 | Kept on 127.0.0.1:8081 for debugging (user, 2026-11-10). Known trade-off: it bypasses the gateway, and the service trusts X-User-Id as sent, so anyone on the machine can act as any customer there. Only the gateway path is authenticated. |
| 34 | Money and fake data model | Decided | 1 | Amounts stored as BIGINT minor units plus an ISO currency code (EUR only in the seed); the API returns the amount as a decimal string with its currency. Seed: 2 to 3 customers matching the Keycloak users, a current and a savings account each, about 12 months of transactions in a fixed category list, generated deterministically by a Flyway migration (user, 2026-11-10). |
