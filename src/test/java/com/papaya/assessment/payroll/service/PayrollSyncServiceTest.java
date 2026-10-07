package com.papaya.assessment.payroll.service;

import com.papaya.assessment.payroll.client.ExternalPayrollClient;
import com.papaya.assessment.payroll.config.PayrollSyncProperties;
import com.papaya.assessment.payroll.dto.ExternalPayrollRecordDto;
import com.papaya.assessment.payroll.dto.SyncResultDto;
import com.papaya.assessment.payroll.entity.Payroll;
import com.papaya.assessment.payroll.exception.ExternalApiTransientException;
import com.papaya.assessment.payroll.exception.PayrollValidationException;
import com.papaya.assessment.payroll.mapper.PayrollMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayrollSyncServiceTest {

    @Mock
    private ExternalPayrollClient externalPayrollClient;
    @Mock
    private PayrollPersistenceService persistenceService;

    private PayrollSyncService payrollSyncService;

    @BeforeEach
    void setUp() {
        payrollSyncService = new PayrollSyncService(
                externalPayrollClient,
                new PayrollRecordValidator(),
                new PayrollMapper(),
                persistenceService,
                new PayrollSyncProperties(true)
        );
    }

    @Test
    void synchronizesValidRecords() {
        ExternalPayrollRecordDto dto = sample();
        when(externalPayrollClient.fetchPayrollRecords()).thenReturn(List.of(dto));
        when(persistenceService.persistIfAbsent(any(Payroll.class)))
                .thenReturn(PayrollPersistenceService.PersistOutcome.INSERTED);

        SyncResultDto result = payrollSyncService.synchronize();

        assertThat(result.fetched()).isEqualTo(1);
        assertThat(result.persisted()).isEqualTo(1);
        assertThat(result.skippedDuplicate()).isZero();
        assertThat(result.skippedInvalid()).isZero();
    }

    @Test
    void handlesEmptyApiResponse() {
        when(externalPayrollClient.fetchPayrollRecords()).thenReturn(List.of());

        SyncResultDto result = payrollSyncService.synchronize();

        assertThat(result.fetched()).isZero();
        assertThat(result.persisted()).isZero();
        verify(persistenceService, never()).persistIfAbsent(any());
    }

    @Test
    void skipsInvalidRecordsWhenConfigured() {
        ExternalPayrollRecordDto invalid = new ExternalPayrollRecordDto(
                null, "X", "bad", BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, "USD");
        when(externalPayrollClient.fetchPayrollRecords()).thenReturn(List.of(invalid));

        SyncResultDto result = payrollSyncService.synchronize();

        assertThat(result.skippedInvalid()).isEqualTo(1);
        assertThat(result.persisted()).isZero();
    }

    @Test
    void countsDuplicates() {
        when(externalPayrollClient.fetchPayrollRecords()).thenReturn(List.of(sample()));
        when(persistenceService.persistIfAbsent(any(Payroll.class)))
                .thenReturn(PayrollPersistenceService.PersistOutcome.DUPLICATE);

        SyncResultDto result = payrollSyncService.synchronize();

        assertThat(result.skippedDuplicate()).isEqualTo(1);
    }

    @Test
    void propagatesExternalApiFailure() {
        when(externalPayrollClient.fetchPayrollRecords())
                .thenThrow(new ExternalApiTransientException("503", 503));

        assertThatThrownBy(() -> payrollSyncService.synchronize())
                .isInstanceOf(ExternalApiTransientException.class);
    }

    @Test
    void failsEntireBatchWhenSkipInvalidDisabled() {
        payrollSyncService = new PayrollSyncService(
                externalPayrollClient,
                new PayrollRecordValidator(),
                new PayrollMapper(),
                persistenceService,
                new PayrollSyncProperties(false)
        );
        ExternalPayrollRecordDto invalid = new ExternalPayrollRecordDto(
                "", "X", "2026-09", BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ONE, "USD");
        when(externalPayrollClient.fetchPayrollRecords()).thenReturn(List.of(invalid));

        assertThatThrownBy(() -> payrollSyncService.synchronize())
                .isInstanceOf(PayrollValidationException.class);
    }

    private static ExternalPayrollRecordDto sample() {
        return new ExternalPayrollRecordDto(
                "EMP001", "John", "2026-09",
                new BigDecimal("100"), new BigDecimal("10"), new BigDecimal("90"), "USD");
    }
}
