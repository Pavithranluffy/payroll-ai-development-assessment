package com.papaya.assessment.payroll.service;

import com.papaya.assessment.payroll.entity.Payroll;
import com.papaya.assessment.payroll.repository.PayrollRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PayrollPersistenceServiceTest {

    @Autowired
    private PayrollPersistenceService persistenceService;

    @Autowired
    private PayrollRepository payrollRepository;

    @BeforeEach
    void clean() {
        payrollRepository.deleteAll();
    }

    @Test
    void persistIfAbsentInsertsOnce() {
        Payroll payroll = payroll("EMP010", "2026-09");

        assertThat(persistenceService.persistIfAbsent(payroll))
                .isEqualTo(PayrollPersistenceService.PersistOutcome.INSERTED);
        assertThat(persistenceService.persistIfAbsent(payroll("EMP010", "2026-09")))
                .isEqualTo(PayrollPersistenceService.PersistOutcome.DUPLICATE);
        assertThat(payrollRepository.count()).isEqualTo(1);
    }

    @Test
    void sameEmployeeDifferentPayPeriodAllowed() {
        assertThat(persistenceService.persistIfAbsent(payroll("EMP011", "2026-08")))
                .isEqualTo(PayrollPersistenceService.PersistOutcome.INSERTED);
        assertThat(persistenceService.persistIfAbsent(payroll("EMP011", "2026-09")))
                .isEqualTo(PayrollPersistenceService.PersistOutcome.INSERTED);
        assertThat(payrollRepository.count()).isEqualTo(2);
    }

    private static Payroll payroll(String employeeId, String payPeriod) {
        Payroll p = new Payroll();
        p.setEmployeeId(employeeId);
        p.setEmployeeName("Test");
        p.setPayPeriod(payPeriod);
        p.setGrossSalary(new BigDecimal("100"));
        p.setTax(new BigDecimal("10"));
        p.setNetSalary(new BigDecimal("90"));
        p.setCurrency("USD");
        return p;
    }
}
