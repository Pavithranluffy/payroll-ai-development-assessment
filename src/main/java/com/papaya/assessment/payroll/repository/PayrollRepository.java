package com.papaya.assessment.payroll.repository;

import com.papaya.assessment.payroll.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    Optional<Payroll> findByEmployeeIdAndPayPeriod(String employeeId, String payPeriod);

    List<Payroll> findByEmployeeIdOrderByPayPeriodDesc(String employeeId);

    List<Payroll> findByPayPeriodOrderByEmployeeIdAsc(String payPeriod);
}
