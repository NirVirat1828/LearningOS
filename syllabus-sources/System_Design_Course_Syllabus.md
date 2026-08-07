# System Design Mega-Course — Syllabus

**Grounding project:** GeoIntel — a geopolitical intelligence platform
**Stack:** Nginx · FastAPI (replicas) · PostgreSQL + pgvector · Redis · Docker Compose · spaCy NER · Claude Haiku (summarisation / enrichment)
**Sources:** Alex Xu's *System Design Interview* (Vol. 1 & 2), an uploaded system-design course PDF (broad intro-level, ~170 pages), supplemented by Claude's own knowledge where sources run thin
**Format:** each module is a set of chapters, delivered as Markdown notes; every chapter follows the same 13-section template

> **Why this course exists.** It serves two purposes at once: interview prep for system design rounds, and a working design reference for actually building GeoIntel. Every module is anchored to GeoIntel's real architecture rather than abstract examples, so what you learn is immediately something you can point at in your own system.

---

## The 13-section chapter template

Every chapter in every module follows this exact structure, so you always know where you are and can revise predictably:

1. **Why it exists** — the problem that forces this concept into existence
2. **Intuition** — a plain-language analogy before any jargon
3. **Formal definition** — the precise, textbook-accurate definition
4. **Internal working** — step-by-step mechanics of how it actually works
5. **Diagram** — a visual (inline SVG or Mermaid) of the concept in action
6. **Real-world examples** — how actual companies / products use this
7. **Code** — a concrete implementation, anchored to GeoIntel's stack
8. **Advantages** — what you gain
9. **Trade-offs** — what you give up or must manage
10. **Common mistakes** — the pitfalls people actually fall into
11. **Graded interview questions** — one easy, one medium, one hard, each with a model answer
12. **Mini task** — a hands-on exercise to do inside GeoIntel itself
13. **Revision sheet** — one-line summary, must-know terms, and flashcards

---

## Module map

| # | Module | Status |
|---|---|---|
| 0 | Prerequisites | ✅ Complete |
| 1 | The Request Journey | ✅ Complete |
| 2 | Load Balancing | ✅ Complete |
| 3 | Caching Layers | ⏳ Planned |
| 4 | Message Queues & Async Processing | ⏳ Planned |
| 5 | Database Indexing & Query Optimization | ⏳ Planned |
| 6 | Read Replicas & Read/Write Scaling | ⏳ Planned |
| 7 | Backpressure & Rate Limiting | ⏳ Planned |
| 8 | Idempotency in Distributed Jobs | ⏳ Planned |
| 9 | CAP Theorem (Applied, In-Depth) | ⏳ Planned |
| 10 | Designing for Failure | ⏳ Planned |
| 11 | Case Study — Scaling GeoIntel | ⏳ Planned |

---

## Module 0 — Prerequisites ✅

*16 chapters, ~33 pages. The vocabulary and building blocks every later module assumes you already have.*

- Networking fundamentals (TCP/IP, DNS, how a request physically travels)
- HTTP fundamentals (methods, status codes, headers, statelessness)
- TLS/SSL basics (handshake, certificates, why HTTPS matters)
- REST API design principles
- Concurrency vs parallelism
- Linux fundamentals for backend engineers
- Docker fundamentals (images, containers, volumes, networking)
- Scalability vocabulary (vertical vs horizontal, throughput vs latency)
- Proxies and reverse proxies
- CDN fundamentals
- Authentication vs authorization
- Database fundamentals (relational vs non-relational, ACID)
- Introduction to CAP theorem and PACELC (revisited in depth in Module 9)
- *(remaining chapters cover the rest of the prerequisite vocabulary used throughout the course)*

---

## Module 1 — The Request Journey ✅

*7 chapters, ~16 pages. Traces one request end-to-end through the whole stack.*

- Client → DNS resolution
- DNS → Load balancer / reverse proxy
- Reverse proxy → Application server
- Application server → Business logic
- Business logic → Database query
- Database → Response assembly
- Response → Client (and what happens on the way back)

---

## Module 2 — Load Balancing ✅

*5 chapters. Deep dive delivered as `Module_2_Load_Balancing_Notes.md`.*

1. **Load Balancing Fundamentals** — the single-server problem, what an LB is, public VIP vs private backend network
2. **Load Balancing Algorithms** — Round Robin, Weighted RR, Least Connections, Least Time, IP Hash, Geo-based
3. **Layer 4 vs Layer 7 Load Balancing** — transport vs application layer, TLS termination, content-based routing
4. **Health Checks & Failure Detection** — active vs passive checks, interval/timeout/threshold, shallow vs deep checks
5. **Sticky Sessions & Session Affinity** — stateful vs stateless architecture, why GeoIntel externalises state to Redis

---

## Module 3 — Caching Layers ⏳

*Builds directly on GeoIntel's existing Redis deployment.*

1. **Caching Fundamentals** — why databases alone aren't fast enough, cache hit/miss, TTL
2. **Caching Strategies** — cache-aside, read-through, write-through, write-back (write-behind), write-around
3. **Cache Invalidation** — the "two hard things in computer science" problem; invalidation patterns for GeoIntel's enrichment cache
4. **Eviction Policies** — LRU, LFU, FIFO, LIFO, MRU — and which fits which access pattern
5. **Distributed Caching & Cache Coherence** — multi-replica cache consistency, cache stampede/thundering herd, and how GeoIntel's Redis layer is shared across FastAPI replicas

---

## Module 4 — Message Queues & Async Processing ⏳

*How GeoIntel can decouple slow enrichment work (spaCy NER + Claude Haiku calls) from the request/response cycle.*

1. **Why Async? The Synchronous Bottleneck** — what happens when enrichment blocks the request thread
2. **Queues vs Streams** — point-to-point queues (RabbitMQ/SQS-style) vs log-based streams (Kafka-style)
3. **Publish/Subscribe Patterns** — fan-out, topic-based routing, event-driven architecture
4. **Worker Pools & Consumer Patterns** — competing consumers, worker scaling, dead-letter queues (intro; full treatment in Module 10)
5. **Applying It to GeoIntel** — designing an async enrichment pipeline: ingest → queue → spaCy/Haiku workers → Postgres write-back

---

## Module 5 — Database Indexing & Query Optimization ⏳

*Flagged for Opus 4.8 — trade-off-heavy, and source material is weak here (needs supplementation).*

1. **Why Indexes Exist** — the full-table-scan problem
2. **B-Tree Indexes** — how they're structured, why they suit range queries and equality lookups
3. **Query Planners & EXPLAIN** — reading a query plan, spotting sequential scans, understanding cost estimates
4. **Composite & Partial Indexes** — multi-column indexing, index-only scans, when a partial index beats a full one
5. **pgvector-Specific Indexing** — HNSW vs IVFFlat for GeoIntel's vector similarity search, and the recall/speed trade-off

---

## Module 6 — Read Replicas & Read/Write Scaling ⏳

*Flagged for Opus 4.8 — deep trade-off reasoning around consistency.*

1. **Why Scale Reads Separately** — read-heavy vs write-heavy workload patterns
2. **Replication Mechanics** — leader/follower (primary/replica) replication, synchronous vs asynchronous
3. **Replication Lag** — what causes it, how to measure it, and its practical impact on GeoIntel's search results
4. **Read Routing Strategies** — read-your-writes consistency, sticky reads, routing by staleness tolerance
5. **Failover & Promotion** — what happens when the primary dies, promotion mechanics, split-brain risk

---

## Module 7 — Backpressure & Rate Limiting ⏳

*Flagged for Opus 4.8 — weak source coverage, needs supplementation.*

1. **Why Systems Need to Say No** — what happens without backpressure: cascading overload
2. **Token Bucket & Leaky Bucket** — the two classic rate-limiting algorithms, side by side
3. **Fixed Window, Sliding Window Log, Sliding Window Counter** — the finer-grained rate-limiting family
4. **Load Shedding** — dropping low-priority work under pressure, graceful degradation
5. **Applying It to GeoIntel** — rate-limiting the enrichment API per API key using a centralized Redis-backed limiter (not sticky sessions — ties back to Module 2, Ch. 5)

---

## Module 8 — Idempotency in Distributed Jobs ⏳

*Flagged for Opus 4.8 — weak source coverage, needs supplementation.*

1. **Why "At-Least-Once" Breaks Things** — retries, duplicate delivery, and the problem they cause
2. **Idempotency Keys** — how clients and servers agree on "already done this"
3. **Deduplication Strategies** — dedup windows, storage-backed dedup, dedup at the queue vs the consumer
4. **Exactly-Once vs At-Least-Once vs At-Most-Once** — what each delivery guarantee actually promises (and doesn't)
5. **Applying It to GeoIntel** — making the enrichment worker safe to retry without double-billing Claude Haiku calls or duplicating NER results

---

## Module 9 — CAP Theorem (Applied, In-Depth) ⏳

*Flagged for Opus 4.8. Revisits the Module 0 introduction with real applied trade-off analysis.*

1. **Recap: Consistency, Availability, Partition Tolerance** — precise definitions, why you can't have all three under a partition
2. **PACELC Extended** — what you trade even when there's no partition (latency vs consistency)
3. **CP Systems in Practice** — examples, and when GeoIntel would need CP behavior
4. **AP Systems in Practice** — examples, and when GeoIntel would tolerate eventual consistency
5. **Choosing for GeoIntel** — walking through which of GeoIntel's components (Postgres primary, Redis cache, search index) sit where on the CAP spectrum, and why

---

## Module 10 — Designing for Failure ⏳

1. **Retries with Backoff** — exponential backoff, jitter, retry storms
2. **Circuit Breakers** — closed/open/half-open states, tripping thresholds, fallback behavior
3. **Dead-Letter Queues** — what happens to messages that can never succeed
4. **Timeouts & Bulkheads** — isolating failure domains so one slow dependency doesn't sink the whole system
5. **Chaos Engineering Basics** — deliberately injecting failure to verify these patterns actually work in GeoIntel

---

## Module 11 — Case Study: Scaling GeoIntel ⏳

*The synthesis module. No new concepts — every prior module's ideas applied to one end-to-end architecture.*

1. **Requirements & Constraints** — defining GeoIntel's real scale targets (ingestion rate, query volume, latency SLOs)
2. **High-Level Architecture** — the full system diagram: Nginx → FastAPI replicas → queue → enrichment workers → Postgres/pgvector + Redis
3. **Scaling the Ingestion Pipeline** — applying Modules 2–4 and 7–8 to the news-feed ingestion path
4. **Scaling Search & Storage** — applying Modules 5–6 and 9 to pgvector search and read scaling
5. **Failure Scenarios Walkthrough** — applying Module 10 to specific GeoIntel failure modes (Redis down, Postgres primary down, Claude Haiku rate-limited, a FastAPI replica crash-looping)

---

## Notes on sequencing & flexibility

- **Order is not fixed.** Module 3 (Caching) could move earlier since GeoIntel's Redis cache already exists and is a natural anchor. Modules 7–8 (Backpressure, Idempotency) could be merged with Module 10 into a single "resilience patterns" module if a lower module count is preferred.
- **Model recommendation:** Modules 5, 6, 7, 8, and 9 are flagged for **Opus 4.8** given trade-off-heavy reasoning and weaker source coverage; the rest are well-suited to **Sonnet 5** as the daily driver.
- **Source coverage gaps:** indexing, backpressure, idempotency, and circuit breakers are all thin or absent in the uploaded course PDF and will lean more heavily on Alex Xu's book and supplemented knowledge.
- **Every module stays anchored to GeoIntel** — no abstract "Server A / Server B" examples where a real GeoIntel component can be used instead.
