# Phase 4: RAG

**Goal.** A knowledge-service that ingests fictional bank documents into a vector index in PostgreSQL (pgvector) and lets the assistant answer from them with a cited source (use case 3).

**Builds on.** Phase 1 (the PostgreSQL container) and phase 2 (assistant-service and the provider switch).

**Candidate tasks**

1. Write a handful of fictional documents: a fee schedule, account terms and a transfer-limits policy.
2. knowledge-service skeleton, with pgvector enabled in the PostgreSQL container.
3. Ingestion: split documents into chunks, create embeddings, and store them with the source title.
4. A retrieval endpoint that returns the best chunks together with their sources.
5. Connect it to assistant-service.
6. Answer format: every answer names its source.
7. A "nothing relevant found" path where the assistant says it does not know.
8. Make re-running the ingestion safe, with no duplicates.
9. Test with a planted document that states a different fee, to prove the answer follows the documents.

**To discuss**

- Documents: invented, or adapted from public bank fee schedules (Decision log #12).
- Embedding model, hosted or local. Vectors from different embedding models are not interchangeable, so changing the model means re-ingesting everything.
- Chunk size and overlap.
- Retrieval added automatically to every question, or exposed as a tool the model chooses to call.
- Where the documents live (files in the repository are the simplest) and what triggers ingestion (startup, an endpoint or an event).
- How to judge retrieval quality: a small list of questions with the expected source.
- Documents are untrusted input: an instruction hidden inside one is an injection risk, tested in phase 8.

**Gate.** The fee question is answered with a cited source, and the answer changes when the planted document changes.

**Hands off to later phases.** The retrieval interface; the embedding model and its settings; the document set, which phase 8 reuses for evals.
