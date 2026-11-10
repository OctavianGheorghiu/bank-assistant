# Constraints, risks and assumptions

## Constraints and risks

The main constraint is time: a few hours a week alongside the user's work and Master's. The plan answers it by keeping every phase small and independently runnable.

| Risk | Response |
| --- | --- |
| Scope creep: more services and features than the time allows | Cap the system at seven services; a phase is done when its demo runs, not when it is polished |
| Model cost grows during experiments | Set a spend limit at each provider, develop on cheaper models, track token usage in metrics, fall back to Ollama |
| Non-deterministic answers make tests flaky | Assert on which tools were called and on structured fields, not on exact wording |
| Local models handle tools and multi-agent flows worse | Treat Ollama as a fallback to compare, and expect weaker results |
| Prompt injection moves money | Money-moving tools need explicit user confirmation, and all data is fake |
| A laptop cannot run Kafka, PostgreSQL, Ollama, monitoring and Kubernetes at once | Use Compose profiles to start only what the current phase needs |
| Spring AI and MCP change quickly | Pin library versions per phase and upgrade deliberately |

## Assumptions

- The user builds alone, one phase at a time, with a few hours a week.
- All data and documents are fictional, and no real bank data is ever used.
- The user's existing Java, Kafka, PostgreSQL, Redis and CI/CD knowledge is reused, so the learning effort goes into the agentic and Kubernetes parts.

Open decisions are tracked in the [Decision log](../state/decision-log.md).
