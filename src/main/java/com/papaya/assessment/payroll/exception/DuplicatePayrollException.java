package com.papaya.assessment.payroll.exception;

public class DuplicatePayrollException extends RuntimeException {

    public DuplicatePayrollException(String employeeId, String payPeriod) {
        super("Payroll already exists for employeeId=%s payPeriod=%s".formatted(employeeId, payPeriod));
    }
}
