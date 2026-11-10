# Key decisions and why

Four decisions are the user's; the rest are proposals the user can overrule. The status of each proposal, and every open decision, is tracked in the [Decision log](../state/decision-log.md).

| Decision | Choice | Why | Made by |
| --- | --- | --- | --- |
| Domain | Banking assistant | Maps naturally to separate services and has real risk to guard against | User |
| AI framework | Spring AI | Native to Spring Boot, with tool calling, RAG, memory and MCP client and server | User |
| Models | Claude and OpenAI hosted, Ollama as local fallback | Best tool-calling quality first, cost escape hatch second | User |
| Depth | Proof of concept for everything | Breadth over mastery | User |
| MCP transport between services | Streamable HTTP, with STDIO only for testing from a desktop client | [Spring AI 1.1](https://spring.io/blog/2025/09/09/spring-ai-1-1-0-M1-available-now) added it, including a stateless mode aimed at microservice deployments | Proposed |
| Vector store | pgvector inside the existing PostgreSQL | One less system to run, and the user already knows PostgreSQL | Proposed |
| Chat memory store | Redis | Fast, simple, already familiar | Proposed |
| Saga style | Choreography over Kafka first | Simplest to build; orchestration can be a later variation | Proposed |
| Message format | JSON first | Avro and a schema registry add no new learning for the user | Proposed |
| Repository layout | One monorepo, multi-module | One place to run and change everything | Proposed |
