package com.papaya.assessment.payroll.service;

import com.papaya.assessment.payroll.dto.ExternalPayrollRecordDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PayrollRecordValidatorTest {

    private final PayrollRecordValidator validator = new PayrollRecordValidator();

    @Test
    void acceptsValidRecord() {
        var record = valid();
        assertThat(validator.validate(record)).isEmpty();
    }

    @Test
    void rejectsMissingEmployeeId() {
        var record = new ExternalPayrollRecordDto(null, "John", "2026-09", bd("100"), bd("10"), bd("90"), "USD");
        assertThat(validator.validate(record)).anyMatch(v -> v.contains("employeeId"));
    }

    @Test
    void rejectsInvalidPayPeriod() {
        var record = new ExternalPayrollRecordDto("EMP1", "John", "2026-13", bd("100"), bd("10"), bd("90"), "USD");
        assertThat(validator.validate(record)).anyMatch(v -> v.contains("payPeriod"));
    }

    @Test
    void rejectsNetSalaryGreaterThanGross() {
        var record = new ExternalPayrollRecordDto("EMP1", "John", "2026-09", bd("100"), bd("10"), bd("150"), "USD");
        assertThat(validator.validate(record)).anyMatch(v -> v.contains("netSalary"));
    }

    @Test
    void rejectsNegativeSalary() {
        var record = new ExternalPayrollRecordDto("EMP1", "John", "2026-09", bd("-1"), bd("0"), bd("0"), "USD");
        assertThat(validator.validate(record)).anyMatch(v -> v.contains("grossSalary"));
    }

    @Test
    void rejectsUnsupportedCurrency() {
        var record = new ExternalPayrollRecordDto("EMP1", "John", "2026-09", bd("100"), bd("10"), bd("90"), "XYZ");
        assertThat(validator.validate(record)).anyMatch(v -> v.contains("currency"));
    }

    private static ExternalPayrollRecordDto valid() {
        return new ExternalPayrollRecordDto("EMP001", "John Doe", "2026-09", bd("75000"), bd("12000"), bd("63000"),
                "USD");
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
