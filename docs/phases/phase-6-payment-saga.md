# Phase 6: Payment saga

**Goal.** payments-service runs a transfer as a multi-step flow with compensation, publishes events through a transactional outbox to Kafka, and notification-service reacts to them.

**Builds on.** Phase 5 (the pending transfer and the confirm endpoint) and phase 1 (accounts-service).

**Candidate tasks**

1. Transfer state model: pending, confirmed, reserved, completed, failed, compensated.
2. The steps: reserve funds in accounts-service, execute the transfer, mark it complete. Each step has a compensating action.
3. An outbox table written in the same database transaction as the state change, and a publisher that sends its rows to Kafka.
4. Kafka in Compose (behind a profile), topics, and a JSON event format.
5. notification-service: consume payment events and record a notification (a log line or a stored message is enough).
6. Idempotency: confirming the same transfer twice must not move money twice.
7. Failure injection: a switch that makes a chosen step fail, to demonstrate compensation.
8. Connect it to the confirm flow from phase 5, so that confirming starts the saga.
9. Tests for the success path, each failure point and a duplicate event.

**To discuss**

- Choreography over Kafka (proposed) or an orchestrator inside payments-service.
- Outbox publisher: polling the table (simplest) or change data capture.
- Event format and versioning: JSON first (proposed), Avro later only if wanted.
- Idempotency keys, and what to do with duplicate or out-of-order events.
- Failed message handling: retries and a dead-letter topic.
- How accounts-service takes part: reserve and release funds as explicit operations.
- Topic naming and the number of topics.
- Kafka's resource use on the development machine.
- Where the chat gets the transfer's status from, and what the assistant tells the user while the saga runs or after it fails.

**Gate.** A failed step rolls the transfer back, and a successful transfer produces an event that notification-service receives.

**Hands off to later phases.** Event names and schemas; the transfer state model; the failure-injection switch, which phase 7 uses in demos.
