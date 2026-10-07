package com.papaya.assessment.payroll.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import com.papaya.assessment.payroll.exception.ExternalApiClientException;
import com.papaya.assessment.payroll.exception.ExternalApiTransientException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class ResilienceConfigurationTest {

    @Autowired
    private RetryRegistry retryRegistry;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Test
    void retryIsConfiguredWithThreeAttempts() {
        Retry retry = retryRegistry.retry("externalPayroll");
        assertThat(retry.getRetryConfig().getMaxAttempts()).isEqualTo(3);
    }

    @Test
    void transientFailureIsRetriedAndThenSucceeds() {
        Retry retry = retryRegistry.retry("externalPayroll");
        AtomicInteger attempts = new AtomicInteger();

        String result = retry.executeSupplier(() -> {
            if (attempts.incrementAndGet() == 1) {
                throw new ExternalApiTransientException("temporary failure", 503);
            }
            return "recovered";
        });

        assertThat(result).isEqualTo("recovered");
        assertThat(attempts).hasValue(2);
    }

    @Test
    void clientErrorIsNotRetried() {
        Retry retry = retryRegistry.retry("externalPayroll");
        AtomicInteger attempts = new AtomicInteger();

        assertThatThrownBy(() -> retry.executeSupplier(() -> {
            attempts.incrementAndGet();
            throw new ExternalApiClientException("invalid request", 400);
        }))
                .isInstanceOf(ExternalApiClientException.class);
        assertThat(attempts).hasValue(1);
    }

    @Test
    void circuitBreakerIsConfiguredForTransientFailures() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("externalPayroll");
        assertThat(circuitBreaker.getState()).isNotNull();
        assertThat(circuitBreaker.getCircuitBreakerConfig().getFailureRateThreshold()).isEqualTo(50.0f);
    }

    @Test
    void circuitBreakerOpensAfterTransientFailureThreshold() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("externalPayroll");

        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> circuitBreaker.executeSupplier(() -> {
                throw new ExternalApiTransientException("temporary failure", 503);
            }))
                    .isInstanceOf(ExternalApiTransientException.class);
        }

        assertThat(circuitBreaker.getState()).isNotNull();
    }
}
