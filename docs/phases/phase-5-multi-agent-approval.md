# Phase 5: Multi-agent and approval

**Goal.** Several cooperating agents behind assistant-service, and a human confirmation step for payments (use cases 4 and 5), using the patterns in the scenarios table.

**Builds on.** Phases 2, 3 and 4. It also needs a minimal payments-service: the full saga arrives in phase 6, so this phase builds only a pending transfer and a confirm endpoint.

**Candidate tasks**

1. Define the specialist agents (accounts, knowledge, payments), each with its own prompt and tool set. Only the payments agent holds the payment tool.
2. Routing: a classifier call that sends each message to the right specialist (use cases 1, 3, 4).
3. A supervisor that splits a request into subtasks and delegates them (use case 5).
4. Minimal payments-service: create a pending transfer and return its id, plus a confirm endpoint that is authenticated and separate from the chat.
5. The confirmation flow in the chat page: show the pending transfer and a Confirm button that calls the endpoint.
6. An evaluator step for knowledge answers: check that every figure appears in the retrieved text, with at most two revision rounds (proposed).
7. Optional: parallel calls for the three-month spending comparison.
8. A log or trace showing which agent did what.
9. Tests: a routing test set, and an injection attempt that tries to confirm a transfer through the chat text.

**To discuss**

- Structure: supervisor with specialists, planner, executor and evaluator as separate agents, or a mix. Raised in conversation and still undecided.
- Whether a specialist is a tool of the supervisor inside one service, or a separate service.
- Which model each agent uses (a stronger one for planning, a cheaper one for routine steps).
- Limits: maximum steps and loops per request, to control cost and avoid endless loops.
- Evaluator criteria: concrete checks against data rather than a vague "does this look right".
- Approval policy: every payment needs confirmation (decided, Decision log #16); still to discuss how long a pending transfer lives before it expires.
- What the confirmation screen shows (recipient, amount, fee).
- What happens when one specialist fails in the middle of a request.
- Which state the agents share (conversation id, user id) and which they must not.
- How much of payments-service to build now and how much in phase 6.

**Gate.** Use case 5 works end to end, and typing "yes, confirmed" in the chat does not execute a transfer.

**Hands off to later phases.** The agent structure and prompts; the pending-transfer API; the read-only or state-changing marking used for approval; the routing test set, which phase 8 reuses.
