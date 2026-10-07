# Papaya Global Requirement Checklist

| Requirement | Section / File |
|-------------|----------------|
| Java 17+ / Spring Boot 3.x | `pom.xml`, `PayrollSyncApplication.java` |
| Layered architecture | `src/main/java/com/papaya/assessment/payroll/**` |
| REST sync + query endpoints | `PayrollController.java` |
| External API abstraction | `ExternalPayrollClient.java`, `RestExternalPayrollClient.java` |
| Configurable URL/timeouts | `application.yml`, `PayrollApiProperties.java` |
| JPA + H2/PostgreSQL | `Payroll.java`, `application.yml`, `docker-compose.yml` |
| Unique business key idempotency | `Payroll` `@UniqueConstraint`, `PayrollPersistenceService` |
| Retry transient failures only | `RestExternalPayrollClient.java`, `application.yml` |
| Circuit breaker + fallback | `RestExternalPayrollClient.java`, resilience4j config |
| Validation | `PayrollRecordValidator.java` |
| Global exception handler | `GlobalExceptionHandler.java` |
| Logging (no sensitive data) | `PayrollSyncService.java`, `RestExternalPayrollClient.java` |
| Actuator | `application.yml` |
| Unit tests | `src/test/java/**/service/*`, `client/RestExternalPayrollClientTest` |
| Integration tests (MockMvc) | `PayrollControllerIntegrationTest.java` |
| AI code generation narrative | `docs/PAPAYA_GLOBAL_SUBMISSION.md` §5 |
| AI debugging scenario | `docs/PAPAYA_GLOBAL_SUBMISSION.md` §6, `docs/examples/PayrollSyncServiceFlawed.java` |
| AI testing narrative | `docs/PAPAYA_GLOBAL_SUBMISSION.md` §7, `docs/PAPAYA_GLOBAL_AI_PROMPTS.md` |
| Edge-case matrix | `docs/PAPAYA_GLOBAL_SUBMISSION.md` §7.3 |
| README | `README.md` |
| .gitignore / no secrets | `.gitignore`, `.env.example` |
| Docker (optional) | `Dockerfile`, `docker-compose.yml` |
| Senior review | `docs/PAPAYA_GLOBAL_SUBMISSION.md` §Senior Review |
