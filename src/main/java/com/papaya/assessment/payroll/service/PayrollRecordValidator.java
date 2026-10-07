package com.papaya.assessment.payroll.service;

import com.papaya.assessment.payroll.dto.ExternalPayrollRecordDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class PayrollRecordValidator {

    private static final Pattern PAY_PERIOD = Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])$");
    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("USD", "EUR", "GBP", "ILS");

    public List<String> validate(ExternalPayrollRecordDto record) {
        List<String> violations = new ArrayList<>();
        if (record == null) {
            violations.add("record must not be null");
            return violations;
        }
        if (isBlank(record.employeeId())) {
            violations.add("employeeId is required");
        }
        if (isBlank(record.employeeName())) {
            violations.add("employeeName is required");
        }
        if (isBlank(record.payPeriod()) || !PAY_PERIOD.matcher(record.payPeriod().trim()).matches()) {
            violations.add("payPeriod must match YYYY-MM");
        }
        if (record.grossSalary() == null || record.grossSalary().compareTo(BigDecimal.ZERO) < 0) {
            violations.add("grossSalary must be non-negative");
        }
        if (record.tax() == null || record.tax().compareTo(BigDecimal.ZERO) < 0) {
            violations.add("tax must be non-negative");
        }
        if (record.netSalary() == null || record.netSalary().compareTo(BigDecimal.ZERO) < 0) {
            violations.add("netSalary must be non-negative");
        }
        if (isBlank(record.currency())) {
            violations.add("currency is required");
        } else if (!SUPPORTED_CURRENCIES.contains(record.currency().trim().toUpperCase())) {
            violations.add("currency is not supported");
        }

        if (record.grossSalary() != null && record.netSalary() != null
                && record.netSalary().compareTo(record.grossSalary()) > 0) {
            violations.add("netSalary must not exceed grossSalary");
        }
        if (record.grossSalary() != null && record.tax() != null
                && record.tax().compareTo(record.grossSalary()) > 0) {
            violations.add("tax must not exceed grossSalary");
        }
        return violations;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
