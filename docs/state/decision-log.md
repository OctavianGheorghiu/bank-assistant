# Decision log

Every open or proposed decision, with the phase chat that settles it. When a decision is made, set its status to Decided and write the outcome in the last column. Decisions the user has already settled are in [Key decisions](../design/key-decisions.md). Smaller choices from a phase's "To discuss" list get a row only when the phase chat settles them.

| # | Decision | Status | Settle in | Outcome |
| --- | --- | --- | --- | --- |
| 1 | Machine limits (RAM, GPU): what runs by default and what sits behind Compose profiles | Decided | 1 | 16 GB RAM laptop, GPU with about 2.3 GB usable VRAM, so local models stay small. Docker gets about 8 GB; Redis joins the default stack in phase 2; Kafka (phase 6) and observability (phase 7) sit behind Compose profiles; Ollama runs natively for the GPU; JVM heaps capped (about 256 MB per service, 512 MB Keycloak); stop Compose before the phase 9 cluster (user, 2026-11-10). |
| 2 | Maven or Gradle | Decided | 1 | Gradle with the Kotlin DSL and the Gradle wrapper (user, 2026-11-10). |
| 3 | Java and Spring Boot versions compatible with Spring AI, Spring Cloud Gateway and Resilience4j | Decided | 1 | Java 21, Spring Boot 4.1.2, Spring Cloud 2025.1.4, Spring AI 2.0.2 (user, 2026-11-10). Phase 7 checks for a Boot 4 Resilience4j starter, with Spring Cloud CircuitBreaker as the fallback. |
| 4 | Authentication: self-issued JWT or Keycloak | Decided | 1 | Keycloak in Compose from phase 1, realm in a file, users matching the seed customers; the gateway validates tokens as a resource server (user, 2026-11-10). |
| 5 | Database layout: one PostgreSQL with a schema per service, or separate databases | Decided | 1 | One database; one schema and one login role per service, each role limited to its schema (user, 2026-11-10). |
| 6 | Schema migration tool | Decided | 1 | Flyway, run by each service at startup against its own schema (user, 2026-11-10). |
| 7 | API access and spending limits for Claude and OpenAI; development models | Open | 2 |  |
| 8 | Stateless services with history in Redis keyed by conversation id | Proposed | 2 |  |
| 9 | How much history is kept and when a conversation expires | Open | 2 |  |
| 10 | MCP in stateless mode over Streamable HTTP | Proposed | 3 |  |
| 11 | How the user's identity reaches tools and the MCP server | Open | 2 and 3 |  |
| 12 | RAG documents: invented or adapted from public fee schedules | Open | 4 |  |
| 13 | Embedding model, hosted or local | Proposed | 4 | Proposed: local nomic-embed-text through Ollama, free and offline. Confirm in phase 4. |
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
| 26 | GitHub Actions for building the repository | Decided | 1 | One workflow: Gradle build with tests on Java 21 for pushes and pull requests (user, 2026-11-10). |
| 27 | AWS deployment as a stretch goal | Open | 9 |  |
| 28 | Service-to-service calls go directly over the internal network; api-gateway is only the entry point from outside | Proposed | 2 | Why: the gateway guards the edge; routing internal calls through it adds a hop for no learning. Calls pass the user identity on. |
| 29 | The payment tool lives in mcp-banking-server, which calls payments-service to create a pending transfer | Proposed | 5 | Why: banking data is reached through MCP tools, and phase 3 marks each tool read-only or state-changing. The tool is marked money-moving. |
| 30 | The Confirm call goes through api-gateway, like every call from the browser | Proposed | 5 | Why: it comes from the browser, so it gets the same JWT check as every outside call. |
| 31 | assistant-service serves the web chat page as a static page | Proposed | 2 | Why: no extra service, and one origin for page and chat endpoint behind the gateway. |
| 32 | Vector store: pgvector inside the existing PostgreSQL | Proposed | 4 |  |
| 33 | Repository layout: one monorepo, multi-module | Decided | 1 | Accepted with the phase 1 task list (user, 2026-11-10). |
| 34 | Money and fake data model | Decided | 1 | BIGINT minor units plus an ISO currency code; the API returns a decimal string. Seed: deterministic Flyway migration, a few customers matching the Keycloak users, about 12 months of categorised transactions (user, 2026-11-10). |
| 35 | Whether accounts-service publishes its port to the host | Decided | 1 | Kept on 127.0.0.1:8081 for debugging (user, 2026-11-10). Trade-off: it bypasses the gateway and trusts `X-User-Id`, so only the gateway path is authenticated. To do: close the port (drop the host mapping in `compose.yaml`) so the gateway is the only way in. |
| 36 | Code structure and style for every service | Decided | 1 | Layered packages, one type per file, Lombok, JPA with Flyway owning the schema, entry logs (user, 2026-11-10). Rules in [code-conventions.md](../design/code-conventions.md). |
| 37 | Account balance: stored or computed from transactions | Decided | 1 | Stored in `account.balance_minor`; transactions stay the ledger (user, 2026-11-10). Any insert of a transaction updates the balance in the same database transaction; phase 6 adds the locking that stops overdrafts. |
| 38 | Size of the transactions list | Decided | 1 (review fixes) | `limit` (default 100, max 1000) and a `truncated` flag (user, 2026-11-10). Why: a year of transactions would flood the agent's context, and a silently cut list makes sums wrong. |
| 39 | Token audience and the identity claim | Decided | 1 (review fixes) | Gateway requires `aud` = `bank-api`, added by an audience mapper on each client that calls it; the customer id is `preferred_username`, because seed data is keyed by username, and usernames cannot be edited (user, 2026-11-10). |
