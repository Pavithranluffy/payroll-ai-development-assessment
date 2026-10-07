# AI Engineering Iteration Log

## 1. Initial generation

Prompt: Build a Java 17 Spring Boot payroll synchronization service with REST external API integration, JPA persistence, retry, circuit breaker, validation, global error handling, tests, and idempotency.

AI output: Generated the project skeleton with layered controller, service, client, repository, DTO, mapper, and exception classes.

Human decision: Retained the layered structure and applied the external API abstraction, DTO records, and constructor injection.

## 2. Transaction-boundary correction

Prompt: Review the generated payroll sync flow for transaction boundaries and slow external calls.

AI output: Identified the risk of holding a database transaction while calling the external API.

Human decision: Moved external I/O outside the database transaction and used a dedicated `TransactionTemplate` boundary for each persisted record. This implementation performs a uniqueness-aware lookup, saves and flushes each record, and maps database duplicate conflicts to `DUPLICATE`.

Evidence: [PayrollSyncService.java](../src/main/java/com/papaya/assessment/payroll/service/PayrollSyncService.java) and [PayrollPersistenceService.java](../src/main/java/com/papaya/assessment/payroll/service/PayrollPersistenceService.java).

## 3. Duplicate prevention correction

Prompt: Review duplicate prevention and concurrent sync behavior.

AI output: Suggested a lookup plus database unique constraint.

Human decision: Added database-level uniqueness and a flush before reporting successful persistence.

Evidence: [Payroll.java](../src/main/java/com/papaya/assessment/payroll/entity/Payroll.java) and [PayrollPersistenceService.java](../src/main/java/com/papaya/assessment/payroll/service/PayrollPersistenceService.java).

## 4. Retry and error classification

Prompt: Classify transient versus permanent failures for the external API.

AI output: Suggested 429 and 5xx retries with no retry for client errors.

Human decision: Implemented typed transient and client exceptions, plus configured Resilience4j behavior.

Evidence: [RestExternalPayrollClient.java](../src/main/java/com/papaya/assessment/payroll/client/RestExternalPayrollClient.java) and [application.yml](../src/main/resources/application.yml).

## 5. Security hardening

Prompt: Review security issues in the generated service.

AI output: Identified unprotected endpoints and hardcoded credentials.

Human decision: Added environment-backed credentials and protected API endpoints; the local in-memory identity is explicitly a development mechanism, not a production identity provider.

Evidence: [SecurityConfig.java](../src/main/java/com/papaya/assessment/payroll/config/SecurityConfig.java), [PayrollSecurityProperties.java](../src/main/java/com/papaya/assessment/payroll/config/PayrollSecurityProperties.java), and [.env.example](../.env.example).

## 6. Testing and validation

Prompt: Generate behavior-focused tests and edge cases.

AI output: Produced unit, integration, and HTTP client tests.

Human decision: Added malformed JSON, security, concurrent persistence, and resilience registry tests. Every test was executed with Maven and reviewed for expected status and behavior.

Evidence: [RestExternalPayrollClientTest.java](../src/test/java/com/papaya/assessment/payroll/client/RestExternalPayrollClientTest.java), [PayrollConcurrencyIntegrationTest.java](../src/test/java/com/papaya/assessment/payroll/service/PayrollConcurrencyIntegrationTest.java), and [ResilienceConfigurationTest.java](../src/test/java/com/papaya/assessment/payroll/config/ResilienceConfigurationTest.java).

## 7. Remaining production gap

The repository now provides a strong assessment implementation, but it does not include a production identity provider, managed secret service, or production deployment controls. These are explicitly documented as remaining evidence gaps rather than being presented as completed production hardening.
