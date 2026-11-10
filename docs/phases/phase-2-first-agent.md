# Phase 2: First agent

**Goal.** assistant-service built with Spring AI: tool calling over accounts-service, structured output, chat memory in Redis, a switch between Claude, OpenAI and Ollama by Spring profile, and a first prompt chain (the spending summary).

**Builds on.** Phase 1: the accounts API, the gateway and the token.

**Candidate tasks**

1. assistant-service skeleton and a chat endpoint behind the gateway.
2. One hosted model working through Spring AI's chat client, with the API key read from the environment.
3. Provider profiles for Claude, OpenAI and Ollama, so one setting switches the model.
4. Tool methods that call accounts-service: balance, recent transactions, spending by category.
5. Structured output: one answer returned as a typed object.
6. Chat memory in Redis, keyed by conversation id.
7. Prompt chain for use case 2: fetch, categorise, summarise, then a code check of the totals, with a redo on mismatch.
8. A minimal web chat page or an HTTP request collection for talking to the assistant.
9. First tests that assert which tools were called, not the exact wording.

**To discuss**

- API access and spending limits for Claude and OpenAI (Decision log #7), and which models to use during development.
- State and memory design: a stateless service with the history in Redis keyed by conversation id (proposed). How much history is sent to the model (the last N messages, or summaries of older ones) and when a conversation expires.
- Identity: how the logged-in user's identity reaches the tools, so a tool always acts for the current user and never trusts an account id produced by the model.
- Secrets: how API keys are supplied locally and kept out of the repository.
- How to test non-deterministic output (assert on tool calls and structured fields).
- Streaming responses or not.
- Which Spring AI version to pin.

**Gate.** The balance question is answered by two different models, and the spending summary's totals match the database.

**Hands off to later phases.** The tool definitions (phase 3 moves them behind MCP, so keep them thin wrappers around a client for accounts-service); the provider switch (used for the fallback in phase 7); the memory design and conversation id (reused in phase 5).
