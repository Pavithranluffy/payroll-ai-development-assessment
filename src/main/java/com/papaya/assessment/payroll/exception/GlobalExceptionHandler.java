package com.papaya.assessment.payroll.exception;

import com.papaya.assessment.payroll.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(PayrollNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(PayrollNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "PAYROLL_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(PayrollValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(PayrollValidationException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "PAYROLL_VALIDATION_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicatePayrollException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicate(DuplicatePayrollException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "DUPLICATE_PAYROLL", ex.getMessage(), request);
    }

    @ExceptionHandler(ExternalApiClientException.class)
    public ResponseEntity<ApiErrorResponse> handleExternalClient(ExternalApiClientException ex, HttpServletRequest request) {
        log.warn("External API client error: status={} path={}", ex.getHttpStatus(), request.getRequestURI());
        return build(HttpStatus.BAD_GATEWAY, "EXTERNAL_API_CLIENT_ERROR", "Unable to fetch payroll data from provider", request);
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ApiErrorResponse> handleExternal(ExternalApiException ex, HttpServletRequest request) {
        log.error("External API failure on path={}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_GATEWAY, "EXTERNAL_API_ERROR", "Unable to fetch payroll data", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled error on path={}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred", request);
    }

    private static ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request
    ) {
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                error,
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}
