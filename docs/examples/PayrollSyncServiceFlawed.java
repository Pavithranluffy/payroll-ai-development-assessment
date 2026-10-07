package docs.examples;

/**
 * INTENTIONALLY FLAWED — debugging exercise only (not compiled in main app).
 * Demonstrates symptoms: duplicates, partial batches, retry storms, long-held DB locks.
 */
public class PayrollSyncServiceFlawed {

    // @Transactional  // BUG: wraps external HTTP call + DB writes in one transaction
    public void synchronize() {
        // RestTemplate with no connect/read timeout — can hang indefinitely
        // var records = restTemplate.getForObject(url, List.class);

        // Retries all exceptions including HTTP 400
        // for (int i = 0; i < 100; i++) { try { fetch(); break; } catch (Exception e) {} }

        // No validation — persists negative salaries and null employee IDs

        // No unique constraint assumed — always insert
        // repository.save(entity); // duplicate rows on repeated sync

        // Logs full payload including employeeName and salary (PII/financial detail)

        // Single large transaction: if record 500 fails, entire batch rolls back
        // but external API may have been called successfully → inconsistent operator view
    }
}
