package com.papaya.assessment.payroll.exception;

public class ExternalApiTransientException extends ExternalApiException {

    private final int httpStatus;

    public ExternalApiTransientException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
