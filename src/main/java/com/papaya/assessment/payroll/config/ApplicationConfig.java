package com.papaya.assessment.payroll.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        PayrollApiProperties.class,
        PayrollSyncProperties.class,
        PayrollSecurityProperties.class
})
public class ApplicationConfig {
}
