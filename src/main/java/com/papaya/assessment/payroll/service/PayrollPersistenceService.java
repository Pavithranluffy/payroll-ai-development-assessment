package com.papaya.assessment.payroll.service;

import com.papaya.assessment.payroll.entity.Payroll;
import com.papaya.assessment.payroll.repository.PayrollRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class PayrollPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(PayrollPersistenceService.class);

    private final PayrollRepository payrollRepository;
    private final TransactionTemplate transactionTemplate;

    public PayrollPersistenceService(
            PayrollRepository payrollRepository,
            PlatformTransactionManager transactionManager) {
        this.payrollRepository = payrollRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public enum PersistOutcome {
        INSERTED,
        DUPLICATE,
        FAILED
    }

    public PersistOutcome persistIfAbsent(Payroll payroll) {
        try {
            return transactionTemplate.execute(status -> {
                if (payrollRepository.findByEmployeeIdAndPayPeriod(
                        payroll.getEmployeeId(), payroll.getPayPeriod()).isPresent()) {
                    return PersistOutcome.DUPLICATE;
                }
                payrollRepository.saveAndFlush(payroll);
                return PersistOutcome.INSERTED;
            });
        } catch (DataIntegrityViolationException ex) {
            log.debug("Duplicate payroll detected via DB constraint employeeId={} payPeriod={}",
                    payroll.getEmployeeId(), payroll.getPayPeriod(), ex);
            return PersistOutcome.DUPLICATE;
        } catch (Exception ex) {
            log.error("Failed to persist payroll employeeId={} payPeriod={}",
                    payroll.getEmployeeId(), payroll.getPayPeriod(), ex);
            return PersistOutcome.FAILED;
        }
    }

    @Transactional
    public void persistBatch(java.util.List<Payroll> payrolls) {
        payrollRepository.saveAll(payrolls);
    }
}
