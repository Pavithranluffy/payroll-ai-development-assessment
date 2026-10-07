package com.papaya.assessment.payroll.exception;

public class ExternalApiClientException extends ExternalApiException {

    private final int httpStatus;

    public ExternalApiClientException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
