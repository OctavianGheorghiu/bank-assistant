# Project status

Update this table at the end of each phase chat. Outcome notes record what was actually built, deviations from the plan and anything the next phase depends on. Library versions are recorded only in [repository.md](repository.md), so the two files cannot disagree.

| Phase | Status | Brief |
| --- | --- | --- |
| 1. Foundation | Gate passed 2026-11-10; branch phase-1-foundation pushed, not yet merged | [phase-1-foundation.md](../phases/phase-1-foundation.md) |
| 2. First agent | Not started | [phase-2-first-agent.md](../phases/phase-2-first-agent.md) |
| 3. MCP | Not started | [phase-3-mcp.md](../phases/phase-3-mcp.md) |
| 4. RAG | Not started | [phase-4-rag.md](../phases/phase-4-rag.md) |
| 5. Multi-agent, approval | Not started | [phase-5-multi-agent-approval.md](../phases/phase-5-multi-agent-approval.md) |
| 6. Payment saga | Not started | [phase-6-payment-saga.md](../phases/phase-6-payment-saga.md) |
| 7. Resilience and tracing | Not started | [phase-7-resilience-tracing.md](../phases/phase-7-resilience-tracing.md) |
| 8. Guardrails and evals | Not started | [phase-8-guardrails-evals.md](../phases/phase-8-guardrails-evals.md) |
| 9. Kubernetes | Not started | [phase-9-kubernetes.md](../phases/phase-9-kubernetes.md) |

## Outcome notes

<!-- One subsection per phase, written at the end of that phase chat (or mid-phase, before moving to a new chat). Keep each to a few bullets; delete what the code now shows better. -->

### 1. Foundation

- Built: Gradle monorepo, PostgreSQL 18 with pgvector, accounts-service (Flyway schema and seed, three GET endpoints), api-gateway (route plus JWT validation), Keycloak with the `bank` realm, smoke-test script, GitHub Actions build. The gate passes from empty volumes in under two minutes.
- Deviation: Keycloak arrived in phase 1 instead of a self-issued JWT (decision #4), so the default stack uses about 1.1 GiB.
- For phase 2: tools call the three accounts endpoints with the `X-User-Id` header (phase 2 decides how the identity reaches tools, decision #11). There is no spending-summary endpoint; sum transactions filtered by category and dates, and check `truncated` before trusting a sum (decision #38). The `bank-cli` client has only the password grant; the web chat page needs a browser login flow added to the realm, and its client needs the `bank-api` audience mapper (decision #39).
- Review fixes (2026-11-10): audience check at the gateway, 401 for a token without a username, transactions `limit` and `truncated`, 400 for `from` after `to`, case-insensitive category, log lines cannot be forged, the redundant `hikari.schema` setting removed. The realm changed, so an existing volume needs `docker compose down -v` once.
- Known gaps: accounts-service port 8081 bypasses the gateway (decision #35); seed dates move with the day the database was created; a new schema or a realm change needs `docker compose down -v` (or the change made by hand).

### 2. First agent

None yet.

### 3. MCP

None yet.

### 4. RAG

None yet.

### 5. Multi-agent, approval

None yet.

### 6. Payment saga

None yet.

### 7. Resilience and tracing

None yet.

### 8. Guardrails and evals

None yet.

### 9. Kubernetes

None yet.
