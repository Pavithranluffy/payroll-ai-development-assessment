package com.papaya.assessment.payroll.service;

import com.papaya.assessment.payroll.client.ExternalPayrollClient;
import com.papaya.assessment.payroll.config.PayrollSyncProperties;
import com.papaya.assessment.payroll.dto.ExternalPayrollRecordDto;
import com.papaya.assessment.payroll.dto.SyncResultDto;
import com.papaya.assessment.payroll.mapper.PayrollMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PayrollSyncService {

    private static final Logger log = LoggerFactory.getLogger(PayrollSyncService.class);

    private final ExternalPayrollClient externalPayrollClient;
    private final PayrollRecordValidator validator;
    private final PayrollMapper payrollMapper;
    private final PayrollPersistenceService persistenceService;
    private final PayrollSyncProperties syncProperties;

    public PayrollSyncService(
            ExternalPayrollClient externalPayrollClient,
            PayrollRecordValidator validator,
            PayrollMapper payrollMapper,
            PayrollPersistenceService persistenceService,
            PayrollSyncProperties syncProperties
    ) {
        this.externalPayrollClient = externalPayrollClient;
        this.validator = validator;
        this.payrollMapper = payrollMapper;
        this.persistenceService = persistenceService;
        this.syncProperties = syncProperties;
    }

    public SyncResultDto synchronize() {
        log.info("Payroll synchronization started");
        List<ExternalPayrollRecordDto> fetched = externalPayrollClient.fetchPayrollRecords();
        log.info("Fetched {} payroll records from external API", fetched.size());

        int persisted = 0;
        int skippedDuplicate = 0;
        int skippedInvalid = 0;
        int failed = 0;
        List<String> invalidSummaries = new ArrayList<>();

        for (ExternalPayrollRecordDto record : fetched) {
            List<String> violations = validator.validate(record);
            if (!violations.isEmpty()) {
                skippedInvalid++;
                String summary = summarize(record, violations);
                invalidSummaries.add(summary);
                log.warn("Skipping invalid payroll record: {}", summary);
                if (!syncProperties.skipInvalidRecords()) {
                    throw new com.papaya.assessment.payroll.exception.PayrollValidationException(violations);
                }
                continue;
            }

            var entity = payrollMapper.toEntity(record);
            PayrollPersistenceService.PersistOutcome outcome = persistenceService.persistIfAbsent(entity);
            switch (outcome) {
                case INSERTED -> persisted++;
                case DUPLICATE -> skippedDuplicate++;
                case FAILED -> failed++;
            }
        }

        if (failed > 0) {
            log.warn("Synchronization completed with {} persistence failures", failed);
        }
        log.info("Payroll synchronization completed fetched={} persisted={} duplicates={} invalid={} failed={}",
                fetched.size(), persisted, skippedDuplicate, skippedInvalid, failed);

        return new SyncResultDto(
                fetched.size(),
                persisted,
                skippedDuplicate,
                skippedInvalid,
                failed,
                List.copyOf(invalidSummaries)
        );
    }

    private static String summarize(ExternalPayrollRecordDto record, List<String> violations) {
        String id = record != null && record.employeeId() != null ? record.employeeId() : "unknown";
        String period = record != null && record.payPeriod() != null ? record.payPeriod() : "unknown";
        return "employeeId=%s payPeriod=%s violations=%s".formatted(id, period, violations);
    }
}
