package com.papaya.assessment.payroll.client;

import com.papaya.assessment.payroll.config.PayrollApiProperties;
import com.papaya.assessment.payroll.dto.ExternalPayrollRecordDto;
import com.papaya.assessment.payroll.exception.ExternalApiClientException;
import com.papaya.assessment.payroll.exception.ExternalApiException;
import com.papaya.assessment.payroll.exception.ExternalApiTransientException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Component
public class RestExternalPayrollClient implements ExternalPayrollClient {

    private static final Logger log = LoggerFactory.getLogger(RestExternalPayrollClient.class);

    private final RestClient restClient;
    private final PayrollApiProperties properties;

    public RestExternalPayrollClient(
            RestClient.Builder restClientBuilder,
            PayrollApiProperties properties
    ) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .build();
    }

    @Override
    @Retry(name = "externalPayroll")
    @CircuitBreaker(name = "externalPayroll", fallbackMethod = "fetchPayrollRecordsFallback")
    public List<ExternalPayrollRecordDto> fetchPayrollRecords() {
        log.info("Fetching payroll records from external API");
        try {
            ExternalPayrollRecordDto[] body = restClient.get()
                    .uri(properties.recordsPath())
                    .headers(headers -> {
                        if (properties.apiKey() != null && !properties.apiKey().isBlank()) {
                            headers.setBearerAuth(properties.apiKey());
                        }
                    })
                    .retrieve()
                    .onStatus(status -> status.value() == 429, (request, response) -> {
                        throw new ExternalApiTransientException("Rate limited by payroll provider", 429);
                    })
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        int status = response.getStatusCode().value();
                        throw new ExternalApiClientException("Client error from payroll provider", status);
                    })
                    .onStatus(status -> status.value() >= 500, (request, response) -> {
                        int statusCode = response.getStatusCode().value();
                        throw new ExternalApiTransientException("Transient error from payroll provider", statusCode);
                    })
                    .body(ExternalPayrollRecordDto[].class);

            if (body == null) {
                return List.of();
            }
            return Arrays.asList(body);
        } catch (ExternalApiException ex) {
            throw ex;
        } catch (ResourceAccessException ex) {
            log.warn("External API connection/read timeout or network failure");
            throw new ExternalApiTransientException("External API unreachable or timed out", 0);
        } catch (Exception ex) {
            log.error("Failed to parse external payroll response");
            throw new ExternalApiException("Malformed payroll response from provider", ex);
        }
    }

    @SuppressWarnings("unused")
    private List<ExternalPayrollRecordDto> fetchPayrollRecordsFallback(Throwable throwable) {
        log.error("Circuit breaker open or retries exhausted for external payroll API");
        throw new ExternalApiException("External payroll API unavailable after retries", throwable);
    }
}
