# Phase 9: Kubernetes

**Goal.** Run the whole system on a local Kubernetes cluster (kind or minikube), the way it would run in production.

**Builds on.** All earlier phases.

**Candidate tasks**

1. Container images for every service, with a repeatable build.
2. Pick plain manifests, Helm or Kustomize, and set up that layout.
3. Configuration through ConfigMaps, and secrets for API keys and passwords.
4. Liveness and readiness probes for every service.
5. Infrastructure on the cluster (PostgreSQL, Redis, Kafka), or kept outside it; see the discussion below.
6. Expose the gateway to the host machine.
7. The observability stack and, optionally, Ollama on the cluster.
8. A script that brings the whole cluster up from nothing, and a smoke test that runs use cases 1 to 6.
9. Optional stretch: deploy to AWS.

**To discuss**

- Plain manifests, Helm or Kustomize.
- Infrastructure inside the cluster or outside it, depending on the machine's RAM.
- How API keys are handled locally as Kubernetes secrets.
- Getting locally built images into the cluster (a local registry or loading the images).
- What "ready" means for services that depend on Kafka or the database.
- Scaling check: because history lives in Redis, assistant-service should work with two replicas; test it.
- Whether the AWS stretch goal is wanted.

**Gate.** The whole stack runs on the local cluster, and the demo for use cases 1 to 6 works there.

**Hands off.** Nothing: this is the last phase.
