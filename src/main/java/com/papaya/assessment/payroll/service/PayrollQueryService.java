package com.papaya.assessment.payroll.service;

import com.papaya.assessment.payroll.dto.PayrollResponseDto;
import com.papaya.assessment.payroll.exception.PayrollNotFoundException;
import com.papaya.assessment.payroll.mapper.PayrollMapper;
import com.papaya.assessment.payroll.repository.PayrollRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PayrollQueryService {

    private final PayrollRepository payrollRepository;
    private final PayrollMapper payrollMapper;

    public PayrollQueryService(PayrollRepository payrollRepository, PayrollMapper payrollMapper) {
        this.payrollRepository = payrollRepository;
        this.payrollMapper = payrollMapper;
    }

    public List<PayrollResponseDto> findAll(String payPeriod) {
        var records = payPeriod == null || payPeriod.isBlank()
                ? payrollRepository.findAll()
                : payrollRepository.findByPayPeriodOrderByEmployeeIdAsc(payPeriod.trim());
        return records.stream().map(payrollMapper::toResponse).toList();
    }

    public List<PayrollResponseDto> findByEmployeeId(String employeeId) {
        List<PayrollResponseDto> records = payrollRepository.findByEmployeeIdOrderByPayPeriodDesc(employeeId)
                .stream()
                .map(payrollMapper::toResponse)
                .toList();
        if (records.isEmpty()) {
            throw new PayrollNotFoundException("No payroll records for employeeId=" + employeeId);
        }
        return records;
    }
}
