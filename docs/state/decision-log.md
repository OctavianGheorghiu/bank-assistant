# Decision log

Every open or proposed decision, with the phase chat that settles it. When a decision is made, set its status to Decided and write the outcome in the last column. Decisions the user has already settled are in [Key decisions](../design/key-decisions.md). Smaller choices from a phase's "To discuss" list get a row only when the phase chat settles them.

| # | Decision | Status | Settle in | Outcome |
| --- | --- | --- | --- | --- |
| 1 | Machine limits (RAM, GPU): what runs by default and what sits behind Compose profiles | Proposed | Before phase 1 | Facts (user, 2026-11-09): Windows laptop, 16 GB RAM, NVIDIA GPU with 4 GB VRAM, about 2.3 GB usable, so local models must fit that (a small embedding model, a chat model of about 1.5 to 3B). Proposed: give Docker about 8 GB; PostgreSQL, Redis and the services built so far start by default; Kafka (phase 6) and the observability stack (phase 7) sit behind their own Compose profiles; Ollama runs natively on Windows for the GPU; cap each service's JVM heap (about 256 MB) and run Kafka as one KRaft broker; in phase 9, stop Compose before starting the cluster. Confirm in phase 1. |
| 2 | Maven or Gradle | Open | 1 |  |
| 3 | Java and Spring Boot versions compatible with Spring AI, Spring Cloud Gateway and Resilience4j | Open | 1 |  |
| 4 | Authentication: self-issued JWT or Keycloak | Open | 1 (Keycloak could come in 8) |  |
| 5 | Database layout: one PostgreSQL with a schema per service, or separate databases | Open | 1 |  |
| 6 | Schema migration tool | Open | 1 |  |
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
| 26 | GitHub Actions for building the repository | Open | 1 |  |
| 27 | AWS deployment as a stretch goal | Open | 9 |  |
| 28 | Service-to-service calls go directly over the internal network; api-gateway is only the entry point from outside | Proposed | 2 | Why: the gateway's job is the edge (JWT, rate limits), and routing internal calls through it adds a hop for no learning. Calls pass the user identity on. |
| 29 | The payment tool lives in mcp-banking-server, which calls payments-service to create a pending transfer | Proposed | 5 | Why: the assistant reaches banking data through MCP tools, and phase 3 already marks each tool read-only or state-changing for approval. The tool is marked money-moving. |
| 30 | The Confirm call goes through api-gateway, like every call from the browser | Proposed | 5 | Why: it comes from the browser, so it gets the same JWT check as every other outside call. |
| 31 | assistant-service serves the web chat page as a static page | Proposed | 2 | Why: no extra service, and the page and the chat endpoint share one origin behind the gateway. |
| 32 | Vector store: pgvector inside the existing PostgreSQL | Proposed | 4 |  |
| 33 | Repository layout: one monorepo, multi-module | Proposed | 1 |  |
