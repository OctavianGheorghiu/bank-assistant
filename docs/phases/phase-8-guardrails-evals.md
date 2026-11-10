# Phase 8: Guardrails and evals

**Goal.** Make the assistant safe and measurable: authorisation on every tool call, an injection test set, output checks, personal-data handling in logs, and automated evaluations that give a score per model.

**Builds on.** Phases 2 to 7: the tool marking, the routing test set, the document set and the traces.

**Candidate tasks**

1. Inventory every tool with its risk level: read-only, state-changing or money-moving.
2. A policy check before each tool call: is this user allowed to do this to this account? Written in code or with OPA.
3. An injection test set: direct attempts in the chat, and instructions hidden in retrieved documents (indirect).
4. Output checks: amounts, account ids and recipients in answers must match the data.
5. Keep personal data out of logs and prompts where it is not needed.
6. Automated evaluations: relevance and correctness checks over a fixed question set, run on demand.
7. Baseline scores per model (Claude, OpenAI, Ollama) and a record of the results.
8. Optional: add Keycloak, if the authentication decision in phase 1 postponed it.
9. Optional: run the evaluations in GitHub Actions.

**To discuss**

- OPA (the user already works with it) or plain code for the policy.
- Where the policy runs: the gateway, assistant-service or the MCP server.
- Indirect injection through RAG documents and tool results: how to treat retrieved text as untrusted.
- Size and source of the evaluation set, and which model acts as judge, if one is used.
- What counts as a pass: thresholds per metric.
- Whether the evaluations run on every change or only on demand, because they cost money.
- What the user sees when a guardrail blocks a request.

**Gate.** An injection attempt is blocked, and one evaluation run produces a score for each model.

**Hands off to later phases.** The policy rules, the test sets and the evaluation baseline, which phase 9 uses to check that nothing broke.
