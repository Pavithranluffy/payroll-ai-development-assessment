package com.papaya.assessment.payroll.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PayrollResponseDto(
        Long id,
        String employeeId,
        String employeeName,
        String payPeriod,
        BigDecimal grossSalary,
        BigDecimal tax,
        BigDecimal netSalary,
        String currency,
        Instant createdAt,
        Instant updatedAt
) {
}
