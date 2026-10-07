package com.papaya.assessment.payroll.exception;

public class PayrollNotFoundException extends RuntimeException {

    public PayrollNotFoundException(String message) {
        super(message);
    }
}
