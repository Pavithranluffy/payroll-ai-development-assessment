package com.papaya.assessment.payroll.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final PayrollSecurityProperties securityProperties;

    public SecurityConfig(PayrollSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/api/payroll/sync").hasRole(securityProperties.role())
                .anyRequest().authenticated())
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.realmName("Payroll Sync"));
        return http.build();
    }

    @Bean
    InMemoryUserDetailsManager userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withUsername(securityProperties.username())
                        .password(securityProperties.password())
                        .roles(securityProperties.role())
                        .build());
    }
}
