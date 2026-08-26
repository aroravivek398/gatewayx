# GatewayX

**API rate-limiting and developer platform** — built as a backend engineering portfolio project demonstrating distributed rate limiting, idempotency, async event processing, and reliable webhook delivery.

🔗 **Live deployment:** http://13.127.199.47:8081
📖 **Interactive API docs:** http://13.127.199.47:8081/swagger-ui/index.html

---

## What GatewayX Does

GatewayX is infrastructure that sits in front of any API a developer wants to expose. Developers register, receive API keys, and every request through GatewayX is:

- **Authenticated** via API key (hashed, never stored in plain text)
- **Rate-limited** per key, using a Redis-backed distributed algorithm — correct even across multiple app instances
- **Metered asynchronously** via Kafka, tracked per key per month
- **Protected against duplicate processing** via an atomic idempotency layer
- **Monitored via webhooks** — subscribers are notified when usage crosses quota thresholds, with HMAC-signed, retried delivery

This mirrors the core engineering challenges behind real API platforms like Stripe or Twilio: correctness under concurrency, reliability under failure, and observability without adding latency to the hot path.

---

## Core Features

### Authentication & API Key Management
- Developer registration and JWT-based login (BCrypt password hashing)
- API key creation with per-plan limits (e.g. FREE tier capped at N active keys)
- Keys shown once at creation, stored only as SHA-256 hashes
- Soft-delete revocation (audit trail preserved)
- Ownership checks — a developer can only manage their own keys (403 on violation, not just 401)

### Rate Limiting
- Two interchangeable algorithms — **Token Bucket** and **Sliding Window Counter** — swappable via a config property, no code changes
- Backed by Redis, using an atomic Lua script for the check-and-decrement operation, eliminating race conditions
- **Proven correct under real concurrency** via a Testcontainers-based test firing 30 concurrent threads against a limit of 10 — exactly 10 succeed, every time
- **Proven correct across multiple app instances** — a live Docker Compose test with two separate app containers confirmed a shared Redis-backed limit holds even while alternating requests between instances

### Idempotency
- Redis `SET NX`-based atomic claim mechanism for safe request retries
- Correctly distinguishes a genuine retry (same key, already completed → cached response returned) from a true concurrent race (same key, still in progress → `409 Conflict`)
- **Proven correct under real concurrency** via a Testcontainers test firing 20 concurrent identical requests — exactly 1 succeeds, the other 19 correctly receive `IN_PROGRESS`

### Async Usage Metering
- Every request publishes a usage event to Kafka, decoupling metering from the request's hot path
- A consumer aggregates usage into per-key, per-month running totals
- Threshold-crossing detection (80% of quota) fires exactly once per crossing, not repeatedly

### Webhooks
- Developers subscribe a target URL to be notified on `QUOTA_THRESHOLD_REACHED` events
- Deliveries are HMAC-SHA256 signed, so subscribers can verify authenticity
- Delivery attempts happen asynchronously (via a second Kafka topic), so a slow or unreachable subscriber never blocks the usage-metering pipeline
- Failed deliveries retry automatically with exponential backoff (1, 4, 16, 60, 60 minutes) via a scheduled task, up to 5 attempts before being marked permanently failed

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language / Framework | Java 25, Spring Boot 4.1 |
| Database | PostgreSQL |
| Caching / Rate Limiting / Idempotency | Redis |
| Async Messaging | Apache Kafka |
| Auth | JWT (developer login), API key + SHA-256 hashing (machine-to-machine) |
| Containerization | Docker, Docker Compose |
| CI | GitHub Actions (test suite + Docker build on every push) |
| Cloud | AWS EC2 |
| Testing | JUnit 5, Mockito, Testcontainers |
| Load Testing | k6 |
| API Documentation | SpringDoc OpenAPI (Swagger UI) |

---

## Architecture

```
                         ┌─────────────┐
   Client Request  ───▶  │ API Key /   │
                         │ JWT Filter  │
                         └──────┬──────┘
                                │
                         ┌──────▼──────┐
                         │Rate Limiter │──── Redis (Lua script, atomic)
                         │(Token Bucket│
                         │/ Sliding Win)│
                         └──────┬──────┘
                                │
                    ┌───────────┼────────────┐
                    ▼                        ▼
             ┌─────────────┐          ┌─────────────┐
             │ Controller  │          │Usage Event  │──▶ Kafka ──▶ Consumer
             │ (business   │          │Producer     │              │
             │  logic)     │          └─────────────┘              ▼
             └─────────────┘                               usage_events +
                                                          usage_aggregates
                                                          (Postgres)
                                                                    │
                                                                    ▼
                                                        Threshold crossing?
                                                                    │
                                                                    ▼
                                                        Webhook Event ──▶ Kafka
                                                                    │
                                                                    ▼
                                                        Webhook Delivery
                                                        (HMAC signed, retried
                                                         with backoff)
```

Idempotency (Redis `SET NX`) sits inside the same request-handling path as the rate limiter, guarding specific endpoints that require exactly-once processing (e.g. order creation).

---

## Running Locally

### Prerequisites
- Docker and Docker Compose
- Java 25 (only needed if running outside Docker, e.g. via IDE)

### Setup

```bash
git clone https://github.com/aroravivek398/gatewayx.git
cd gatewayx
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Edit `application.properties` with your local values (the defaults work out of the box for `docker compose up`).

```bash
docker compose up -d
```

This starts Postgres, Redis, Kafka, Zookeeper, and the app itself. Wait ~30 seconds for Kafka to stabilize before making requests.

### Seed a plan

```bash
docker exec -it gatewayx-postgres-1 psql -U gatewayx_user -d gatewayx
```
```sql
INSERT INTO plans (name, rate_limit_per_minute, monthly_quota, max_api_keys_per_developer, created_at)
VALUES ('FREE', 100, 10000, 2, NOW());
```

### Try it

```bash
# Register
curl -X POST http://localhost:8081/api/v1/developers/register \
  -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","name":"Your Name","password":"yourpassword"}'

# Login
curl -X POST http://localhost:8081/api/v1/developers/login \
  -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","password":"yourpassword"}'

# Create an API key (use the token from login)
curl -X POST http://localhost:8081/api/v1/api-keys/create \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"planId": 1}'

# Hit a rate-limited endpoint (use the rawKey from the response above)
curl http://localhost:8081/api/v1/demo/ping -H "X-API-Key: <rawKey>"
```

Or skip the curl commands entirely and explore everything interactively via Swagger at `http://localhost:8081/swagger-ui/index.html`.

---

## Testing

```bash
mvn test
```

The test suite includes:
- **Unit tests** for pure functions (API key generation, HMAC signing)
- **Mockito-based service tests** for business logic (plan limits, retry/backoff decisions)
- **Testcontainers-based integration tests** spinning up real Redis, Postgres, Kafka, and Zookeeper containers — including two genuine concurrency tests (30 threads against the rate limiter, 20 threads against idempotency) that directly prove atomicity under real parallel load, not just sequential calls

All tests run automatically on every push via GitHub Actions.

---

## Load Testing

Two k6 load tests were run against the live AWS deployment (scripts included: `baseline-test.js`, `rate-limit-test.js`):

**Raw throughput** (rate limit temporarily raised to isolate capacity from limiting):
- ~54 requests/second sustained, 10 concurrent virtual users
- p50: 56ms, p90: 127ms, p95: 135ms
- 100% success rate over 1625 requests

**Rate limiter under sustained load** (60 req/min limit, ~25 req/s attempted traffic):
- 90 requests correctly allowed, 478 correctly rejected with `429` — the 90 (rather than a naive 30) is expected and confirms the Token Bucket algorithm's deliberate burst-allowing design
- p95 latency stayed under 90ms even while the limiter was actively rejecting the majority of traffic
- 100% of responses were either `200` or `429` — zero unexpected errors under load

---

## Deployment

Deployed on a single AWS EC2 instance (`t3.small`) running the full stack via Docker Compose — Postgres, Redis, Kafka, Zookeeper, and the app itself, all containerized.

- **Elastic IP** ensures the public address doesn't change across instance restarts
- **Persistent Docker volume** for Postgres ensures data survives container restarts
- Secrets (`jwt.secret`, database credentials) are supplied via environment variables, never committed to source control
- A [`docker-compose.yml`](./docker-compose.yml) at the project root fully describes the deployed topology

---

## CI Pipeline

GitHub Actions runs on every push to `main`:
1. Spins up Postgres, Redis, Kafka, and Zookeeper containers (via the same Testcontainers setup used in local testing)
2. Runs the full test suite
3. Confirms the Docker image builds successfully

Automated deployment to EC2 is intentionally out of scope for this project — a deliberate decision to keep the pipeline's credential-handling surface small, documented as a natural next step rather than an oversight.

---

## Project Structure

```
src/main/java/com/gatewayx/
├── controller/       REST endpoints
├── service/          Business logic
├── entity/           JPA entities
├── repository/       Spring Data repositories
├── dto/              Request/response objects
├── security/         JWT and API key auth filters
├── ratelimit/         Token Bucket / Sliding Window strategies
├── idempotency/       Redis-based idempotency service
├── kafka/             Producers and consumers
├── util/              API key generation, HMAC signing
├── exception/          Custom exceptions and global handler
└── config/            Spring configuration (scheduling, OpenAPI)

src/test/java/com/gatewayx/    Unit, Mockito, and Testcontainers tests
```

---

## Notes on Scope

This project deliberately has no frontend — a Postman collection and this Swagger UI are sufficient to demonstrate and exercise the API, keeping the project's focus entirely on backend engineering depth rather than splitting effort across a UI layer.