package com.papaya.assessment.payroll.service;

import com.papaya.assessment.payroll.entity.Payroll;
import com.papaya.assessment.payroll.repository.PayrollRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PayrollConcurrencyIntegrationTest {

    @Autowired
    private PayrollPersistenceService persistenceService;

    @Autowired
    private PayrollRepository payrollRepository;

    @BeforeEach
    void cleanDatabase() {
        payrollRepository.deleteAll();
    }

    @Test
    void concurrentIdenticalRecordsProduceOneDatabaseRow() throws Exception {
        int workers = 16;
        ExecutorService executor = Executors.newFixedThreadPool(workers);
        try {
            List<Callable<PayrollPersistenceService.PersistOutcome>> calls = new ArrayList<>();
            for (int i = 0; i < workers; i++) {
                calls.add(() -> persistenceService.persistIfAbsent(
                        payroll("CONCURRENT-001", "2026-09")));
            }

            List<Future<PayrollPersistenceService.PersistOutcome>> results = executor.invokeAll(calls);
            List<PayrollPersistenceService.PersistOutcome> outcomes = new ArrayList<>();
            for (Future<PayrollPersistenceService.PersistOutcome> result : results) {
                outcomes.add(result.get());
            }

            assertThat(outcomes).filteredOn(outcome -> outcome == PayrollPersistenceService.PersistOutcome.INSERTED)
                    .hasSizeLessThanOrEqualTo(1);
            assertThat(outcomes).filteredOn(outcome -> outcome == PayrollPersistenceService.PersistOutcome.DUPLICATE)
                    .isNotEmpty();
            assertThat(payrollRepository.count()).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    private static Payroll payroll(String employeeId, String payPeriod) {
        Payroll record = new Payroll();
        record.setEmployeeId(employeeId);
        record.setEmployeeName("Concurrent Test");
        record.setPayPeriod(payPeriod);
        record.setGrossSalary(new BigDecimal("100"));
        record.setTax(new BigDecimal("10"));
        record.setNetSalary(new BigDecimal("90"));
        record.setCurrency("USD");
        return record;
    }
}
