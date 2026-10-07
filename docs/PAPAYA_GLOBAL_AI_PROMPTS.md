# AI Prompts Used

## 1. Initial code generation
```
You are a senior Java engineer. Build a Spring Boot 3.3 / Java 17 payroll sync service with layered architecture (controller, service, client, repository, entity, dto, mapper, exception, config). Requirements: RestClient external client with configurable base URL/timeouts; Resilience4j retry (429/5xx/timeout only, exponential backoff max 3); circuit breaker; JPA entity with unique (employee_id, pay_period); idempotent sync; validate external records (YYYY-MM pay period, non-negative amounts, net <= gross); skip invalid records and return summary; @RestControllerAdvice error envelope; SLF4J logs without PII; Actuator health; JUnit 5 + Mockito + MockMvc integration tests. Use H2 locally, constructor injection, no Lombok. Explain transaction boundaries: never hold DB tx during external call.
```

## 2. Architecture review
```
Review this payroll sync design for clean architecture and transaction boundaries. Identify where external I/O and database persistence should be separated, and whether idempotency should be enforced in service logic, DB constraints, or both. List top 3 race conditions for concurrent POST /sync.
```

## 3. Debugging
```
I have Spring Boot payroll sync code where POST /sync sometimes creates duplicate rows and sometimes saves only part of a batch. External API latency is 5–8s and returns HTTP 503. Code uses @Transactional on the whole sync method, RestTemplate without timeouts, and retries Exception including HttpClientErrorException 400. Analyze root causes vs symptoms, consistency issues, and retry anti-patterns. Recommend minimal fixes with trade-offs.
```

## 4. Performance optimization
```
Review PayrollSyncService and PayrollPersistenceService for performance pitfalls: N+1 queries, transaction scope, batch insert vs per-record TransactionTemplate boundaries, connection pool exhaustion under slow external API. Suggest pragmatic improvements without claiming benchmark numbers unless I provide measurements.
```

## 5. Retry strategy
```
Design Resilience4j retry + circuit breaker config for an external payroll REST client. Classify HTTP status codes and ResourceAccessException into retry vs fail-fast. Provide application.yml snippet with maxAttempts=3, waitDuration=500ms, multiplier=2. Explain why retrying HTTP 400 is dangerous.
```

## 6. Data consistency
```
Given a slow external payroll API and idempotent business key (employeeId + payPeriod), propose a sync algorithm that avoids holding DB transactions during HTTP calls, handles partial batch failures, and uses a unique DB constraint as final deduplication guard. Include concurrent sync scenario.
```

## 7. Unit test generation
```
Review PayrollSyncService and generate JUnit 5 tests with Mockito focusing on behavior: successful sync, empty API response, invalid record skipped, duplicate skipped, external API failure propagation, fail-fast when skip-invalid=false. Avoid testing implementation details or private methods.
```

## 8. Edge-case discovery
```
Create an edge-case matrix for payroll synchronization from an external API (malformed JSON, null fields, rate limits, repeated sync, DB unavailable, large batches, concurrent sync). For each case specify expected HTTP status and sync summary fields.
```

## 9. Security review
```
Review this Spring Boot payroll service for security issues: secrets in config files, sensitive logging, missing auth on REST endpoints, SSRF via configurable base URL. Provide production hardening checklist without implementing full OAuth.
```

## 10. Code refactoring
```
Refactor this service to improve SRP and testability: extract validation, separate query vs sync services, reduce method size, keep behavior identical. Show before/after for one method only.
```

## 11. Code review (PR)
```
Review this PR for payroll sync: check transaction boundaries, idempotency under concurrency, exception mapping, and whether tests assert behavior vs mocks. Flag blocking issues only.
```

## 12. Documentation
```
Write a concise README for a Spring Boot payroll sync assessment project: setup, configuration env vars, API examples, retry/idempotency behavior, testing commands, and how AI was used responsibly during development.
```
