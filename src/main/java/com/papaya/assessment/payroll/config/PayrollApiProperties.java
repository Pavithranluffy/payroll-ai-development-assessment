package com.papaya.assessment.payroll.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payroll.api")
public record PayrollApiProperties(
        String baseUrl,
        String recordsPath,
        int connectTimeoutMs,
        int readTimeoutMs,
        String apiKey
) {
    public String recordsUrl() {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String path = recordsPath.startsWith("/") ? recordsPath : "/" + recordsPath;
        return base + path;
    }
}
