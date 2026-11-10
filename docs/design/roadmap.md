# Phased roadmap

The plan has nine phases, ordered so that each one builds on a running system and ends in a demo the user can show.

| Phase | Builds on | Brief |
| --- | --- | --- |
| 1. Foundation | Nothing | [phase-1-foundation.md](../phases/phase-1-foundation.md) |
| 2. First agent | 1 | [phase-2-first-agent.md](../phases/phase-2-first-agent.md) |
| 3. MCP | 2 | [phase-3-mcp.md](../phases/phase-3-mcp.md) |
| 4. RAG | 1, 2 | [phase-4-rag.md](../phases/phase-4-rag.md) |
| 5. Multi-agent and approval | 2, 3, 4 (and builds a minimal payments-service) | [phase-5-multi-agent-approval.md](../phases/phase-5-multi-agent-approval.md) |
| 6. Payment saga | 5, 1 | [phase-6-payment-saga.md](../phases/phase-6-payment-saga.md) |
| 7. Resilience and tracing | 1 to 6 | [phase-7-resilience-tracing.md](../phases/phase-7-resilience-tracing.md) |
| 8. Guardrails and evals | 2 to 7 | [phase-8-guardrails-evals.md](../phases/phase-8-guardrails-evals.md) |
| 9. Kubernetes | All earlier phases | [phase-9-kubernetes.md](../phases/phase-9-kubernetes.md) |

At a few hours a week, a rough estimate is two to three weeks per phase, so about five to six months in total. Phases 1 to 5 deliver the agentic core; phases 6 to 9 deliver the microservice depth. If time runs short, shorten phases from the end rather than skipping a gate, because every phase leaves the system working.

## Phase details

Each file in [docs/phases/](../phases/) is the brief for one phase chat. "Candidate tasks" are a starting point: the phase chat confirms, reorders or splits them. "To discuss" lists the open problems that must be settled in that phase, with the user, before building whatever they affect.
