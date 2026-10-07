package com.papaya.assessment.payroll.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalPayrollRecordDto(
        String employeeId,
        String employeeName,
        String payPeriod,
        BigDecimal grossSalary,
        BigDecimal tax,
        BigDecimal netSalary,
        String currency
) {
}
