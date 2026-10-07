package com.papaya.assessment.payroll.client;

import com.papaya.assessment.payroll.dto.ExternalPayrollRecordDto;

import java.util.List;

public interface ExternalPayrollClient {

    List<ExternalPayrollRecordDto> fetchPayrollRecords();
}
