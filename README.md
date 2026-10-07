# Payroll Sync Service

Production-oriented Spring Boot backend for synchronizing payroll data from an external provider into a relational database. Built for the Papaya Global SDE-1 AI Application Task.

## Business problem

Payroll data lives in an external system that is **slow** and **occasionally unavailable**. Repeated sync runs must not create duplicates, invalid provider records must not corrupt stored data, and operators need REST APIs to trigger sync and query results.

## Architecture

```
POST/GET /api/payroll  →  Controller  →  Sync / Query Services
                              ↓
                    ExternalPayrollClient (RestClient + Resilience4j)
                              ↓
                         External API

Sync path (no DB transaction during HTTP):
  fetch → validate → map → persist (dedicated TransactionTemplate boundary per record)
```

## Technology stack

- Java 17, Spring Boot 3.3, Maven
- Spring Web, Data JPA, Validation, Actuator
- RestClient (HTTP), Resilience4j (retry + circuit breaker)
- H2 (local/test), PostgreSQL (optional via `postgres` profile)
- JUnit 5, Mockito, MockMvc, MockRestServiceServer

## Setup

**Prerequisites:** JDK 17+, Maven 3.9+

```bash
cd /Users/pavithranr/Documents/Papaya
mvn test
mvn spring-boot:run
```

Default external API target is the in-app mock at `http://localhost:8080/mock-external`.

## Configuration

| Property / env | Description |
|----------------|-------------|
| `payroll.api.base-url` / `PAYROLL_API_BASE_URL` | External payroll API base URL |
| `payroll.api.read-timeout-ms` / `PAYROLL_API_READ_TIMEOUT_MS` | Read timeout (default 8000 ms) |
| `payroll.api.api-key` / `PAYROLL_API_KEY` | Bearer token (env/secrets only) |
| `payroll.sync.skip-invalid-records` | Skip bad records vs fail entire sync (default `true`) |

See `.env.example` — do not commit secrets.

## Database

- **H2 in-memory** by default (`ddl-auto: update`)
- **PostgreSQL:** provide `POSTGRES_DB`, `POSTGRES_USER`, and `POSTGRES_PASSWORD` through a local `.env` file or your environment, then run with `SPRING_PROFILES_ACTIVE=postgres`

The PostgreSQL Compose configuration rejects missing credentials rather than using defaults. Do not commit real values; use a `.env` file ignored by Git or a managed secret service.

Unique constraint: `(employee_id, pay_period)` — final idempotency guard under concurrency.

## API endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/payroll/sync` | Run synchronization (returns summary) |
| `GET` | `/api/payroll` | List stored records |
| `GET` | `/api/payroll?payPeriod=2026-09` | Filter by pay period |
| `GET` | `/api/payroll/{employeeId}` | Records for one employee |
| `GET` | `/actuator/health` | Health |

### Sync response (`200 OK`)

Synchronous sync returns **200 OK** with a summary when complete. **202 ACCEPTED** is preferable when sync is enqueued (message queue + worker) so clients poll status without holding HTTP connections; this project uses synchronous sync for clarity at SDE-1 scope.

```json
{
  "fetched": 2,
  "persisted": 2,
  "skippedDuplicate": 0,
  "skippedInvalid": 0,
  "failed": 0,
  "invalidRecordSummaries": []
}
```

### Example

```bash
curl -X POST http://localhost:8080/api/payroll/sync
curl http://localhost:8080/api/payroll
curl "http://localhost:8080/api/payroll?payPeriod=2026-09"
curl http://localhost:8080/api/payroll/EMP001
```

### Mock external API modes (local)

`GET /mock-external/api/v1/payroll-records?mode=503&delayMs=6000`

Modes: `ok`, `empty`, `429`, `400`, `500`, `503`, `malformed`

## Failure handling

- **Retry (Resilience4j):** transient errors — 429, 502, 503, 504, timeouts (`maxAttempts=3`, 500 ms × 2 backoff)
- **No retry:** 4xx client errors, validation failures, malformed business data
- **Circuit breaker:** opens after repeated failures; fallback returns `502 EXTERNAL_API_ERROR`
- **Errors:** consistent JSON via `@RestControllerAdvice` (no stack traces)

## Idempotency

1. Pre-insert lookup by `employeeId + payPeriod`
2. Unique DB constraint catches races between concurrent syncs
3. Re-sync increments `skippedDuplicate`, not row count

## Testing

```bash
mvn test
```

| Test class | Validates |
|------------|-----------|
| `PayrollRecordValidatorTest` | Field and business rules |
| `PayrollSyncServiceTest` | Sync behavior, empty API, invalid skip, duplicates |
| `RestExternalPayrollClientTest` | HTTP mapping 400/503 |
| `PayrollControllerIntegrationTest` | MockMvc end-to-end, idempotent double sync |
| `PayrollPersistenceServiceTest` | DB uniqueness behavior |

**Integration strategy:** `@SpringBootTest` + `@ActiveProfiles("test")` + H2; external client mocked with `@MockBean`; HTTP client tested with `MockRestServiceServer`.

## Performance notes (illustrative, not benchmarked)

- Timeouts prevent thread pool exhaustion under slow API
- HikariCP pools DB connections
- Indexes on `employee_id`, `pay_period`
- The persistence service uses a dedicated `TransactionTemplate` boundary for each record and maps duplicate conflicts to a safe idempotent result
- `saveAll()` batching is available in `PayrollPersistenceService.persistBatch()` for large valid batches when all-or-nothing persistence is acceptable

## AI-assisted development

AI accelerated scaffolding and test ideas; **all behavior was validated** with `mvn test`, log review, and Spring/Resilience4j docs. See:

- `docs/PAPAYA_GLOBAL_SUBMISSION.md` — full assessment narrative
- `docs/PAPAYA_GLOBAL_AI_PROMPTS.md` — reusable prompts
- `docs/examples/PayrollSyncServiceFlawed.java` — debugging exercise

## Design decisions

- **Skip invalid records** (configurable): payroll feeds are often partially bad; failing the whole batch blocks valid employees
- **Short DB transactions:** external I/O outside transaction boundary
- **No Lombok:** records + explicit entities keep dependencies minimal

## Future improvements

- Async sync (`202`) with job status table
- Production identity provider / OAuth2-JWT for `/api/payroll/*`
- Flyway migrations for PostgreSQL
- Micrometer custom metrics (retry count, records processed)
- Pagination on `GET /api/payroll`

This assessment intentionally does not claim production identity or managed-secret implementation. The current in-memory application credential is a development mechanism only.

## Candidate

Pavithran Rajendran — Papaya Global SDE-1 AI Application Task
