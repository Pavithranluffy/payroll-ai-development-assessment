package com.papaya.assessment.payroll.mapper;

import com.papaya.assessment.payroll.dto.ExternalPayrollRecordDto;
import com.papaya.assessment.payroll.dto.PayrollResponseDto;
import com.papaya.assessment.payroll.entity.Payroll;
import org.springframework.stereotype.Component;

@Component
public class PayrollMapper {

    public Payroll toEntity(ExternalPayrollRecordDto source) {
        Payroll payroll = new Payroll();
        payroll.setEmployeeId(source.employeeId().trim());
        payroll.setEmployeeName(source.employeeName().trim());
        payroll.setPayPeriod(source.payPeriod().trim());
        payroll.setGrossSalary(source.grossSalary());
        payroll.setTax(source.tax());
        payroll.setNetSalary(source.netSalary());
        payroll.setCurrency(source.currency().trim().toUpperCase());
        return payroll;
    }

    public PayrollResponseDto toResponse(Payroll payroll) {
        return new PayrollResponseDto(
                payroll.getId(),
                payroll.getEmployeeId(),
                payroll.getEmployeeName(),
                payroll.getPayPeriod(),
                payroll.getGrossSalary(),
                payroll.getTax(),
                payroll.getNetSalary(),
                payroll.getCurrency(),
                payroll.getCreatedAt(),
                payroll.getUpdatedAt()
        );
    }
}
