package com.papaya.assessment.payroll.exception;

import java.util.List;

public class PayrollValidationException extends RuntimeException {

    private final List<String> violations;

    public PayrollValidationException(List<String> violations) {
        super(String.join("; ", violations));
        this.violations = List.copyOf(violations);
    }

    public List<String> getViolations() {
        return violations;
    }
}
