# Post-Fix AI Audit Evidence

## Security

The application now uses environment-backed credentials and requires authentication for API routes. The sync endpoint additionally requires the configured `SYNC_ADMIN` role. Health and info endpoints are the only public management routes. The H2 console is disabled by default and is available only in the dedicated development profile.

## Persistence

`PayrollPersistenceService.persistIfAbsent` now uses a dedicated `TransactionTemplate` boundary, performs an existence check, and saves and flushes each record. Database uniqueness failures are mapped to `DUPLICATE`.

## Resilience

`RestExternalPayrollClient` maps 429 and 5xx responses to transient exceptions and 4xx responses to client exceptions. Runtime tests verify that a transient exception is retried and then succeeds, a client exception is attempted once, and the circuit breaker is configured with an operational state transition path.

## Human validation

The final fixes were validated by:

1. Full Maven clean compilation.
2. JUnit 5 integration tests.
3. Spring Security endpoint tests.
4. MockRestServiceServer request and exception mapping tests.
5. Runtime Resilience4j retry and non-retry behavior tests.
6. VS Code diagnostics and source review.

The remaining production-readiness items are identity-provider authentication, production secret management, operational metrics, migration strategy, and a production performance benchmark.
