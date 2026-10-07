package com.papaya.assessment.payroll.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payroll.sync")
public record PayrollSyncProperties(boolean skipInvalidRecords) {
}
