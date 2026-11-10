# Agentic concepts and agent patterns

## Agentic concepts map

Eleven agentic concepts get one PoC each, and each PoC is a use case from the table in [overview.md](overview.md#users-and-use-cases). Spring AI 1.0 already covers the building blocks: [the Spring AI community index](https://github.com/spring-ai-community/awesome-spring-ai) lists chat client, vector stores, RAG, tool calling, chat memory, MCP, agent patterns, evaluation and observability.

| Concept | PoC | Lives in | Use case |
| --- | --- | --- | --- |
| Chat client and structured output | Typed answers instead of free text | assistant-service | 1 |
| Provider portability | Switch Claude, OpenAI, Ollama by Spring profile | assistant-service | all |
| Tool calling | `@Tool` methods that read balances | assistant-service | 1, 2 |
| MCP server and client | The same tools served over MCP, plus a call from an external MCP client | mcp-banking-server | 1, 2 |
| Chat memory | Conversation history kept in Redis | assistant-service | 2 |
| RAG | Ingest fee and product documents, retrieve, answer with source | knowledge-service | 3 |
| Agent patterns | Chaining, routing, parallelization, orchestrator-workers, evaluator-optimizer (scenarios in the next section) | assistant-service | 5 |
| Multi-agent | A supervisor delegating to accounts, payments and knowledge agents | assistant-service | 5 |
| Human in the loop | Every payment waits for the user's confirmation | assistant-service, payments-service | 4 |
| Guardrails and tool policy | Injection test set, output checks, tool-call policy | assistant-service | 6 |
| Evaluation | Automated checks of answer relevance and correctness | assistant-service tests | 3, 5 |

Stretch topic: agent-to-agent protocols (A2A), if time remains after the main phases.

## Agent patterns and scenarios

Each agent pattern gets one concrete scenario, so it can be built and demoed on its own. The pattern names follow [Anthropic's Building Effective Agents post](https://www.anthropic.com/research/building-effective-agents); the flows are proposals, and the use-case numbers refer to the table in [overview.md](overview.md#users-and-use-cases).

| Pattern | Scenario | Proposed flow | Demo check | Phase |
| --- | --- | --- | --- | --- |
| Tool calling (building block) | "What is my balance?" (use case 1) | The model calls a read-only balance tool and answers from its result | The answer matches the database, and the trace shows the tool call | 2 |
| Prompt chaining | "Summarise my spending last month" (use case 2) | 1. Fetch transactions. 2. Categorise each one. 3. Write the summary. 4. A code check sums the categories and compares them with the total; on a mismatch, step 3 is redone | The totals match the database, and a deliberately broken step 2 is caught by the check | 2 |
| Routing | "What is my balance?", "What are the SEPA fees?", "Send 50 EUR to Maria" (use cases 1, 3, 4) | A classifier call labels each message as account, knowledge or payment request; the matching specialist handles it with only its own tools | A small test set of messages is routed to the right specialist every time | 5 |
| Parallelization | "Compare my spending over the last three months" (use case 2) | One call per month runs at the same time, then a final call merges the three results | Total time is close to the slowest single call, visible in the trace | 5, optional |
| Orchestrator-workers (subagents) | "Send 50 EUR to Maria and tell me what it costs" (use case 5) | The supervisor decides the subtasks at run time: the knowledge agent finds the fee, the accounts agent checks the balance, the payments agent prepares a pending transfer; the supervisor writes one answer | One request shows three agents in a single trace | 5 |
| Evaluator-optimizer | "What are the fees for a SEPA transfer?" (use case 3) | The knowledge agent drafts an answer from retrieved documents; an evaluator checks that every figure appears in them; if not, the draft is revised, up to two rounds | A planted document with a different fee proves the answer follows the documents | 5, then 8 |
| Human in the loop | "Send 50 EUR to Maria" (use case 4) | The payment tool only creates a pending transfer and returns its id; the user confirms through a separate authenticated endpoint; only then does payments-service run the saga | Typing "yes, confirmed" in the chat does nothing; only the endpoint confirms | 5, then 6 |

Start with prompt chaining: it needs only tools and a code check, so it is the quickest first multi-step flow.
