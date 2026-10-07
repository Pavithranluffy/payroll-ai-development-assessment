package com.papaya.assessment.payroll.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payroll.security")
public record PayrollSecurityProperties(
        String username,
        String password,
        String role) {
}
