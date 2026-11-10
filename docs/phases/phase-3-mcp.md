# Phase 3: MCP

**Goal.** A new mcp-banking-server that exposes the banking operations as MCP tools, with assistant-service acting as an MCP client. A separate MCP client must also be able to call the tools.

**Builds on.** Phase 2: the tool definitions and the identity approach.

**Candidate tasks**

1. mcp-banking-server skeleton using Spring AI's MCP server support.
2. Move the balance, transactions and spending tools from assistant-service into it.
3. Choose and configure the transport: Streamable HTTP between services (proposed) and STDIO for local testing with a desktop client.
4. Configure assistant-service as an MCP client that discovers the tools.
5. Write clear tool names and descriptions, since the model picks tools from them.
6. Call the server from an external MCP client and record how it was done.
7. Pass the user's identity to the MCP server (see the discussion below).
8. Update the Compose file and the smoke-test script.

**To discuss**

- Stateless or stateful MCP sessions. A stateless mode exists and is aimed at microservice deployments, and the banking tools do not need a session (proposed: stateless).
- Securing the MCP server: passing the user's token through versus service credentials, and whether this is where Keycloak and OAuth2 get added. The Spring AI 1.1.0-M1 release post (September 2025) says MCP security documentation was still in progress, so check the current state.
- Tool granularity: a few broad tools or many narrow ones.
- Mark each tool as read-only or state-changing now, because phase 5 uses that for approval.
- Whether to expose MCP resources and prompts as well as tools, or tools only.
- What the assistant does when the MCP server is down (feeds phase 7).

**Gate.** The balance question works through MCP, and a separate MCP client can list and call the tools.

**Hands off to later phases.** The MCP tool catalogue and tool names; the transport and session mode; how identity is passed; the read-only or state-changing marking on each tool.
