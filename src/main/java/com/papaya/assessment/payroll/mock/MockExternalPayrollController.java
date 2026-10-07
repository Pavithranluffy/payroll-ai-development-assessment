package com.papaya.assessment.payroll.mock;

import com.papaya.assessment.payroll.dto.ExternalPayrollRecordDto;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * Local stand-in for the external payroll provider (default profile).
 * Configure payroll.api.base-url to point at this app when running locally.
 */
@RestController
@RequestMapping("/mock-external/api/v1")
@Profile("!test")
public class MockExternalPayrollController {

    @GetMapping("/payroll-records")
    public ResponseEntity<List<ExternalPayrollRecordDto>> records(
            @RequestParam(defaultValue = "ok") String mode,
            @RequestParam(defaultValue = "0") long delayMs
    ) throws InterruptedException {
        if (delayMs > 0) {
            Thread.sleep(Math.min(delayMs, 15_000));
        }
        return switch (mode) {
            case "empty" -> ResponseEntity.ok(List.of());
            case "503" -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            case "429" -> ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
            case "400" -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            case "500" -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            case "malformed" -> ResponseEntity.ok(List.of(
                    new ExternalPayrollRecordDto(null, "Bad", "2026-09", BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, "USD")
            ));
            default -> ResponseEntity.ok(sampleRecords());
        };
    }

    private static List<ExternalPayrollRecordDto> sampleRecords() {
        return List.of(
                new ExternalPayrollRecordDto(
                        "EMP001", "John Doe", "2026-09",
                        new BigDecimal("75000.00"), new BigDecimal("12000.00"),
                        new BigDecimal("63000.00"), "USD"
                ),
                new ExternalPayrollRecordDto(
                        "EMP002", "Jane Smith", "2026-09",
                        new BigDecimal("82000.00"), new BigDecimal("15000.00"),
                        new BigDecimal("67000.00"), "USD"
                )
        );
    }
}
