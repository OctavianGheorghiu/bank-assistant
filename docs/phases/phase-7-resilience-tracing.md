# Phase 7: Resilience and tracing

**Goal.** The system survives slow or failing model calls and shows what happened: Resilience4j around model and service calls, a fallback to the local Ollama model, OpenTelemetry traces across all services, Prometheus metrics and a Grafana dashboard that includes token usage, and per-user rate limits at the gateway.

**Builds on.** Phases 1 to 6. Tracing needs every service to exist, which is why it comes late.

**Candidate tasks**

1. Micrometer and OpenTelemetry in every service.
2. Trace propagation through the gateway, assistant-service, MCP server, accounts-service and payments-service, so that one request is one trace.
3. Observability stack in Compose behind a profile: a trace backend, Prometheus and Grafana.
4. Metrics for model calls: latency, errors and token usage per request.
5. Timeouts, retries and a circuit breaker around model calls and around calls between services.
6. Fallback: when the hosted model fails, switch to the Ollama model.
7. A per-user rate limit at the gateway.
8. An outage drill: block the hosted provider and show the fallback in the trace.
9. A Grafana dashboard with latency, error rate, token usage and circuit breaker state.

**To discuss**

- Which trace backend to use.
- What may be recorded: prompts and answers can contain personal data, so decide what goes into traces and logs. The fake data is harmless, but the habit matters.
- What the fallback means for quality: the local model is weaker at tool calling, so should the assistant tell the user it is in fallback mode, and should payments be allowed while it is?
- Retries and state-changing calls: never retry a payment call blindly; rely on the idempotency from phase 6.
- Token budget: a per-user limit in tokens, or requests per minute.
- Timeout values for slow model calls.
- Running Ollama and the observability stack on the development machine (RAM).

**Gate.** With the hosted provider blocked, the assistant still answers through the local model, and the trace shows the switch.

**Hands off to later phases.** The metric and trace conventions, the fallback settings and the dashboards.
