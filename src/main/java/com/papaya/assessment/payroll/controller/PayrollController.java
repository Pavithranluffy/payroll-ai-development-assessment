package com.papaya.assessment.payroll.controller;

import com.papaya.assessment.payroll.dto.PayrollResponseDto;
import com.papaya.assessment.payroll.dto.SyncResultDto;
import com.papaya.assessment.payroll.service.PayrollQueryService;
import com.papaya.assessment.payroll.service.PayrollSyncService;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@Validated
public class PayrollController {

    private final PayrollSyncService payrollSyncService;
    private final PayrollQueryService payrollQueryService;

    public PayrollController(PayrollSyncService payrollSyncService, PayrollQueryService payrollQueryService) {
        this.payrollSyncService = payrollSyncService;
        this.payrollQueryService = payrollQueryService;
    }

    /**
     * Synchronous sync: returns 200 with a summary when complete.
     * 202 ACCEPTED would be preferable for long-running/async jobs (queue + worker); kept synchronous here for clarity.
     */
    @PostMapping("/sync")
    public ResponseEntity<SyncResultDto> syncPayroll() {
        SyncResultDto result = payrollSyncService.synchronize();
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public List<PayrollResponseDto> listPayroll(
            @RequestParam(required = false)
            @Pattern(regexp = "^$|^\\d{4}-(0[1-9]|1[0-2])$", message = "payPeriod must be YYYY-MM")
            String payPeriod
    ) {
        String normalized = payPeriod == null || payPeriod.isBlank() ? null : payPeriod;
        return payrollQueryService.findAll(normalized);
    }

    @GetMapping("/{employeeId}")
    public List<PayrollResponseDto> getByEmployee(@PathVariable String employeeId) {
        if (employeeId == null || employeeId.isBlank()) {
            throw new com.papaya.assessment.payroll.exception.PayrollValidationException(
                    List.of("employeeId must not be blank")
            );
        }
        return payrollQueryService.findByEmployeeId(employeeId.trim());
    }
}
